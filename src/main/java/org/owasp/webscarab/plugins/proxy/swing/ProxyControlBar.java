/**
 * 
 */
package org.owasp.webscarab.plugins.proxy.swing;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JWindow;

import org.owasp.webscarab.domain.Annotation;
import org.owasp.webscarab.plugins.proxy.Annotator;
import org.owasp.webscarab.plugins.proxy.Proxy;
import org.owasp.webscarab.ui.forms.AnnotationForm;
import org.owasp.webscarab.util.swing.HeapMonitor;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.core.enums.LabeledEnumResolver;
import org.springframework.core.enums.ShortCodedLabeledEnum;
import org.springframework.richclient.application.ApplicationServicesLocator;
import org.springframework.richclient.command.ToggleCommand;
import org.springframework.richclient.factory.AbstractControlFactory;

/**
 * @author rdawes
 * 
 */
public class ProxyControlBar implements Annotator {

	private static final long serialVersionUID = -9094317210840512267L;

	private SwingInterceptor swingInterceptor;

	private AnnotationForm annotationForm = new AnnotationForm();

	private Preferences prefs;

	private InterceptRequestController requestController;

	private ToggleCommand interceptResponses = null;

	private JWindow window;

	public ProxyControlBar() {
		prefs = Preferences.userNodeForPackage(getClass());
	}

	public JWindow getControl() {
		if (window == null && swingInterceptor != null) {
			requestController = new InterceptRequestController();
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
			pane.add(requestController.getControl());
			if (interceptResponses != null)
				pane.add(interceptResponses.createCheckBox());
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

	private class InterceptRequestController extends AbstractControlFactory
			implements ActionListener {

		private List<String> GET;

		private List<String> POST;

		private List<String> custom;

		private List<String> ALL;

		public InterceptRequestController() {
			GET = Arrays.asList("GET");
			POST = Arrays.asList("POST");
			custom = null;
			ALL = Arrays.asList("GET", "POST", "HEAD", "PUT", "DELETE",
					"OPTIONS", "TRACE");
		}

		public void actionPerformed(ActionEvent e) {
			InterceptRequestOption selection = (InterceptRequestOption) ((JComboBox) e
					.getSource()).getSelectedItem();
			if (selection == InterceptRequestOption.interceptNone) {
				swingInterceptor.setInterceptRequestMethods(null);
			} else if (selection == InterceptRequestOption.interceptGet) {
				swingInterceptor.setInterceptRequestMethods(GET);
			} else if (selection == InterceptRequestOption.interceptPost) {
				swingInterceptor.setInterceptRequestMethods(POST);
			} else if (selection == InterceptRequestOption.interceptCustom) {
				swingInterceptor.setInterceptRequestMethods(custom);
			} else if (selection == InterceptRequestOption.interceptAll) {
				swingInterceptor.setInterceptRequestMethods(ALL);
			}
			prefs.put("interceptRequest", selection.getLabel());
		}

		protected LabeledEnumResolver getEnumResolver() {
			return (LabeledEnumResolver) ApplicationServicesLocator.services()
					.getService(LabeledEnumResolver.class);
		}

		public JComponent createControl() {
			JComboBox comboBox = getComponentFactory().createComboBox(
					InterceptRequestOption.class);
			comboBox.addActionListener(this);
			ComboBoxModel model = comboBox.getModel();
			String selected = prefs.get("interceptRequest", "interceptNone");
			for (int i = 0; i < model.getSize(); i++) {
				if (((InterceptRequestOption) model.getElementAt(i)).getLabel()
						.equals(selected)) {
					model.setSelectedItem(model.getElementAt(i));
				}
			}
			return comboBox;
		}

	}

	public static class InterceptRequestOption extends ShortCodedLabeledEnum
			implements MessageSourceResolvable {

		private static final long serialVersionUID = 7382148038528775564L;

		public static final InterceptRequestOption interceptNone = new InterceptRequestOption(
				0, "interceptNone");

		public static final InterceptRequestOption interceptGet = new InterceptRequestOption(
				1, "interceptGet");

		public static final InterceptRequestOption interceptPost = new InterceptRequestOption(
				2, "interceptPost");

		public static final InterceptRequestOption interceptCustom = new InterceptRequestOption(
				3, "interceptCustom");

		public static final InterceptRequestOption interceptAll = new InterceptRequestOption(
				4, "interceptAll");

		private InterceptRequestOption(int code, String label) {
			super(code, label);
		}

		public Object[] getArguments() {
			return null;
		}

		public String[] getCodes() {
			return new String[] { getLabel() };
		}

		public String getDefaultMessage() {
			return getLabel();
		}

	}

	public SwingInterceptor getSwingInterceptor() {
		return this.swingInterceptor;
	}

	public void setSwingInterceptor(SwingInterceptor swingInterceptor) {
		this.swingInterceptor = swingInterceptor;
	}
}
