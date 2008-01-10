/**
 * 
 */
package org.owasp.webscarab.plugins.proxy.swing;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.springframework.richclient.command.ActionCommand;
import org.springframework.richclient.command.ActionCommandInterceptor;
import org.springframework.richclient.dialog.DialogPage;
import org.springframework.richclient.dialog.support.DialogPageUtils;

/**
 * @author rdawes
 * 
 */
public class DialogPageFrame {

	private Object lock = new Object();

	private JFrame frame;

	private Preferences prefs;

	public DialogPageFrame(DialogPage page, ActionCommand okCommand,
			final ActionCommand cancelCommand) {
		prefs = Preferences.userNodeForPackage(getClass());

		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		Container pane = frame.getContentPane();

		pane.add(DialogPageUtils.createStandardView(page, okCommand, cancelCommand));

		ActionCommandInterceptor interceptor = new ActionCommandInterceptor() {
			public void postExecution(ActionCommand command) {
				synchronized (lock) {
					lock.notify();
				}
			}
			public boolean preExecution(ActionCommand command) {
				return true;
			}
		};
		okCommand.addCommandInterceptor(interceptor);
		cancelCommand.addCommandInterceptor(interceptor);

		frame.getRootPane().setDefaultButton(
				(JButton) okCommand.getButtonIn(pane));
		JLayeredPane layeredPane = frame.getLayeredPane();
		layeredPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close-it");
		layeredPane.getActionMap().put("close-it",
				cancelCommand.getActionAdapter());
		frame.setTitle(page.getTitle());
		frame.setIconImage(page.getImage());
		setFrameBounds();
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosed(WindowEvent e) {
			    e.getWindow().removeWindowListener(this);
				cancelCommand.execute();
			}
		});
		frame.addComponentListener(new ComponentAdapter() {
			public void componentMoved(ComponentEvent e) {
				if (!frame.isShowing()) return;
				prefs.putInt("x", frame.getX());
				prefs.putInt("y", frame.getY());
			}
			public void componentResized(ComponentEvent e) {
				if (!frame.isShowing()) return;
				prefs.putInt("w", frame.getWidth());
				prefs.putInt("h", frame.getHeight());
			}
		});
	}

	public void showAsDialog() {
		if (SwingUtilities.isEventDispatchThread())
			throw new IllegalStateException(
					"Cannot show as a dialog on the EDT!");
		frame.setVisible(true);
		frame.toFront();
		try {
			synchronized (lock) {
				lock.wait();
			}
		} catch (InterruptedException ie) {
		}
		frame.setVisible(false);
		frame.dispose();
	}

	private void setFrameBounds() {
		int w = prefs.getInt("w", 800);
		int h = prefs.getInt("h", 600);
		Dimension ss = Toolkit.getDefaultToolkit().getScreenSize();
		int x = prefs.getInt("x", (ss.width-w)/2);
		int y = prefs.getInt("y", (ss.height-h)/2);
		frame.setBounds(x,y,w,h);
	}
}
