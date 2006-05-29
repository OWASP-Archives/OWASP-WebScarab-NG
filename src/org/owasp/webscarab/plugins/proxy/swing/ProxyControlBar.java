/**
 * 
 */
package org.owasp.webscarab.plugins.proxy.swing;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.prefs.Preferences;

import javax.swing.JFrame;
import javax.swing.JWindow;

import org.owasp.webscarab.domain.Annotation;
import org.owasp.webscarab.plugins.proxy.Annotator;
import org.owasp.webscarab.plugins.proxy.Proxy;
import org.owasp.webscarab.ui.forms.AnnotationForm;

/**
 * @author rdawes
 * 
 */
public class ProxyControlBar extends JWindow implements Annotator {

	private static final long serialVersionUID = -9094317210840512267L;

	private Proxy proxy;

	private AnnotationForm annotationForm;

	private ShowProxyControlBarCommand showProxyControlBarCommand;

	private Preferences prefs;
	
	public ProxyControlBar() {
		super(new JFrame() {
			private static final long serialVersionUID = 8058984312174557361L;

			public boolean isShowing() { return true; }
		});
		setAlwaysOnTop(true);
		setFocusableWindowState(true);
		getContentPane().setLayout(new BorderLayout());

		prefs = Preferences.userNodeForPackage(getClass());
		final Point origin = new Point();
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				origin.x = e.getX();
				origin.y = e.getY();
			}
		});
		addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseDragged(MouseEvent e) {
				Point p = getLocation();
				setLocation(p.x + e.getX() - origin.x, p.y + e.getY()
						- origin.y);
				prefs.putInt("x", (int) getLocation().getX());
				prefs.putInt("y", (int) getLocation().getY());
			}
		});
		int x = prefs.getInt("x", Integer.MIN_VALUE);
		int y = prefs.getInt("y", Integer.MIN_VALUE);
		if (x != Integer.MIN_VALUE && y != Integer.MIN_VALUE)
			setLocation(x,y);
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
			if (annotation.getAnnotation() == null || annotation.getAnnotation().equals(""))
				return null;
			return annotation;
		}
		return null;
	}

	public AnnotationForm getAnnotationForm() {
		return this.annotationForm;
	}

	public void setAnnotationForm(AnnotationForm annotationForm) {
		if (this.annotationForm != null)
			getContentPane().remove(annotationForm.getControl());
		this.annotationForm = annotationForm;
		if (annotationForm != null) {
			getContentPane().add(annotationForm.getControl(),
					BorderLayout.CENTER);
			pack();
		}
	}

	public ShowProxyControlBarCommand getShowProxyControlBarCommand() {
		return this.showProxyControlBarCommand;
	}

	public void setShowProxyControlBarCommand(
			ShowProxyControlBarCommand showProxyControlBarCommand) {
		this.showProxyControlBarCommand = showProxyControlBarCommand;
	}
}
