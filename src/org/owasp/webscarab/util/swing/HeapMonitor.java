/**
 * 
 */
package org.owasp.webscarab.util.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * @author rdawes
 * 
 */
public class HeapMonitor extends JButton implements ActionListener {

	private long free;

	private long max;

	private static final long MB = 1024l * 1024l;

	private Timer timer;

	private static final long serialVersionUID = 2794077275357170746L;

	public HeapMonitor() {
		this(5000);
	}
	
	public HeapMonitor(int interval) {
		max = Runtime.getRuntime().maxMemory();
		setOpaque(true);
		update();
		setMinimumSize(new Dimension(120, 22));
		setPreferredSize(getMinimumSize());
		setAction(new AbstractAction() {
			private static final long serialVersionUID = 8625117663082769802L;
			public void actionPerformed(ActionEvent e) {
				gc();
			}
		});
		
		timer = new Timer(interval, this);
		timer.start();
	}

	private void gc() {
		new Thread("Garbage collector") {
			public void run() {
				System.gc();
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						update();
					}
				});
			}
		}.start();
	}

	private void update() {
		free = max + Runtime.getRuntime().freeMemory()
				- Runtime.getRuntime().totalMemory();
		setText(toMB(max - free) + " / " + toMB(max));
		double ratio = (double) (max - free) / (double) max;
		if (ratio > 0.9f) {
			setBackground(Color.RED);
		} else if (ratio > 0.75f) {
			setBackground(Color.YELLOW);
		} else {
			setBackground(Color.GREEN);
		}
	}

	public void actionPerformed(ActionEvent e) {
		update();
	}

	public static String toMB(long bytes) {
		String s = Double.toString((double) bytes / MB);
		int dot = s.indexOf(".");
		if (dot > 0 && dot < s.length() - 2)
			s = s.substring(0, dot + 3);
		return s;
	}

	public static void main(String[] args) throws Exception {
		javax.swing.JWindow frame = new javax.swing.JWindow();
		frame.getContentPane().add(new HeapMonitor(1000));
		frame.pack();
		frame.setVisible(true);
		// do something to allocate memory
		byte[][] bytes = new byte[1024][];
		for (int i = 0; i < 1024; i++) {
			bytes[i] = new byte[65535];
			java.util.Arrays.fill(bytes[i], (byte) 1);
			Thread.sleep(10);
		}
		Thread.sleep(10000);
		System.exit(0);
	}

}
