/**
 * 
 */
package org.owasp.webscarab.plugins.proxy.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.prefs.Preferences;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JWindow;

import org.owasp.webscarab.domain.Annotation;
import org.owasp.webscarab.plugins.proxy.Annotator;
import org.owasp.webscarab.plugins.proxy.Proxy;
import org.owasp.webscarab.ui.forms.AnnotationForm;
import org.owasp.webscarab.util.swing.HeapMonitor;
import org.springframework.richclient.command.ToggleCommand;

/**
 * @author rdawes
 * 
 */
public class ProxyControlBar implements Annotator {

	private static final long serialVersionUID = -9094317210840512267L;

	private Proxy proxy;

	private AnnotationForm annotationForm;

	private Preferences prefs;

	private ToggleCommand interceptRequests = null;
	
	private ToggleCommand interceptResponses = null;
	
	private JWindow window;
	
	public ProxyControlBar() {
		prefs = Preferences.userNodeForPackage(getClass());
	}

	public JWindow getControl() {
		if (window == null) {
			window = new JWindow(new JFrame() {
				private static final long serialVersionUID = 8058984312174557361L;

				public boolean isShowing() {
					return true;
				}
			});
			window.setAlwaysOnTop(true);
			window.setFocusableWindowState(true);
			window.getContentPane().setLayout(new FlowLayout());
			if (interceptRequests != null)
				window.getContentPane().add(interceptRequests.createCheckBox());
			if (interceptResponses != null)
				window.getContentPane().add(interceptResponses.createCheckBox());
			JComponent component = annotationForm.getControl();
			component.setPreferredSize(new Dimension(600, (int) component
					.getPreferredSize().getHeight()));
			component.setMinimumSize(component.getPreferredSize());
			window.getContentPane().add(component, BorderLayout.CENTER);
			window.getContentPane().add(new HeapMonitor(), BorderLayout.EAST);
			window.pack();
			
			final Point origin = new Point();
			window.addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					origin.x = e.getX();
					origin.y = e.getY();
				}
			});
			window.addMouseMotionListener(new MouseMotionAdapter() {
				public void mouseDragged(MouseEvent e) {
					Point p = window.getLocation();
					window.setLocation(p.x + e.getX() - origin.x, p.y + e.getY()
							- origin.y);
					prefs.putInt("x", (int) window.getLocation().getX());
					prefs.putInt("y", (int) window.getLocation().getY());
				}
			});
			int x = prefs.getInt("x", Integer.MIN_VALUE);
			int y = prefs.getInt("y", Integer.MIN_VALUE);
			if (x != Integer.MIN_VALUE && y != Integer.MIN_VALUE)
				window.setLocation(x, y);

		}
		return window;
	}
	
	public Proxy getProxy() {
		return this.proxy;
	}

	public void setProxy(Proxy proxy) {
		this.proxy = proxy;
		if (proxy != null)
			proxy.setAnnotator(this);
	}

	public Annotation getAnnotation() {
		if (getAnnotationForm() != null) {
			Annotation annotation = (Annotation) getAnnotationForm()
					.getFormObject();
			getAnnotationForm().setFormObject(new Annotation());
			return annotation;
		}
		return null;
	}

	public AnnotationForm getAnnotationForm() {
		return this.annotationForm;
	}

	public void setAnnotationForm(AnnotationForm annotationForm) {
		this.annotationForm = annotationForm;
	}

	public void setInterceptRequests(ToggleCommand interceptRequests) {
		this.interceptRequests = interceptRequests;
	}

	public void setInterceptResponses(ToggleCommand interceptResponses) {
		this.interceptResponses = interceptResponses;
	}

}
