/**
 *
 */
package org.owasp.webscarab.plugins.proxy.swing;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.prefs.Preferences;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JWindow;

import org.owasp.webscarab.domain.Annotation;
import org.owasp.webscarab.plugins.proxy.Annotator;
import org.owasp.webscarab.plugins.proxy.Proxy;
import org.owasp.webscarab.ui.rcp.forms.AnnotationForm;
import org.owasp.webscarab.util.swing.HeapMonitor;
import org.springframework.richclient.command.ExclusiveCommandGroup;
import org.springframework.richclient.command.ToggleCommand;

/**
 * @author rdawes
 *
 */
public class ProxyControlBar implements Annotator {

	private static final long serialVersionUID = -9094317210840512267L;

	private SwingInterceptor swingInterceptor;

	private AnnotationForm annotationForm = new AnnotationForm();

	private Preferences prefs;

    private ExclusiveCommandGroup interceptRequestCommandGroup;

	private ToggleCommand interceptResponseCommand = null;

	private JWindow window;

	public ProxyControlBar() {
		prefs = Preferences.userNodeForPackage(getClass());
	}

	public JWindow getControl() {
		if (window == null && swingInterceptor != null) {
			window = new JWindow(new JFrame() {
				private static final long serialVersionUID = 8058984312174557361L;

				public boolean isShowing() {
					return true;
				}
			});
			window.setAlwaysOnTop(true);
			window.setFocusableWindowState(true);
			Container pane = window.getContentPane();
			pane.setLayout(new FlowLayout());
            JComboBox combo = interceptRequestCommandGroup.createComboBox();
			pane.add(combo);
			if (interceptResponseCommand != null)
				pane.add(interceptResponseCommand.createCheckBox());
			JComponent component = annotationForm.getControl();
			component.setPreferredSize(new Dimension(600, (int) component
					.getPreferredSize().getHeight()));
			component.setMinimumSize(component.getPreferredSize());
			pane.add(component, BorderLayout.CENTER);
			pane.add(new HeapMonitor(), BorderLayout.EAST);
			window.pack();
			window.setSize(window.getSize().width, 28);

			final Point origin = new Point();
			window.addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					origin.x = e.getX();
					origin.y = e.getY();
				}
			});
			window.addMouseMotionListener(new MouseMotionAdapter() {
				public void mouseDragged(MouseEvent e) {
					int x = window.getX();
					int y = window.getY();
					window.setLocation(x + e.getX() - origin.x, y
							+ e.getY() - origin.y);
					prefs.putInt("x", x);
					prefs.putInt("y", y);
				}
			});
			int x = prefs.getInt("x", Integer.MIN_VALUE);
			int y = prefs.getInt("y", Integer.MIN_VALUE);
			if (x != Integer.MIN_VALUE && y != Integer.MIN_VALUE)
				window.setLocation(x, y);

		}
		return window;
	}

	public void setProxy(Proxy proxy) {
		if (proxy != null)
			proxy.setAnnotator(this);
	}

	public Annotation getAnnotation() {
		Annotation annotation = (Annotation) annotationForm.getFormObject();
		annotationForm.setFormObject(new Annotation());
		return annotation;
	}

	public SwingInterceptor getSwingInterceptor() {
		return this.swingInterceptor;
	}

	public void setSwingInterceptor(SwingInterceptor swingInterceptor) {
		this.swingInterceptor = swingInterceptor;
	}

    /**
     * @param interceptRequestCommandGroup the interceptRequestCommandGroup to set
     */
    public void setInterceptRequestCommandGroup(
            ExclusiveCommandGroup interceptRequestCommandGroup) {
        this.interceptRequestCommandGroup = interceptRequestCommandGroup;
    }

    /**
     * @param interceptResponseCommand the interceptResponseCommand to set
     */
    public void setInterceptResponseCommand(ToggleCommand interceptResponseCommand) {
        this.interceptResponseCommand = interceptResponseCommand;
    }
}
