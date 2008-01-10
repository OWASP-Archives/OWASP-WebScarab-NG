/**
 * 
 */
package org.owasp.webscarab.ui.rcp.forms;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.text.BadLocationException;
import javax.swing.text.ChangedCharSetException;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.springframework.binding.form.FormModel;

/**
 * @author rdawes
 * 
 */
public class HtmlForm extends AbstractContentForm {

	private static String FORM_ID = "htmlForm";

	private JEditorPane editorPane;

	public HtmlForm(FormModel model, String headerPropertyName, String contentPropertyName) {
		super(model, FORM_ID, headerPropertyName, contentPropertyName);
	}

	@Override
	protected JComponent createContentFormControl() {
		editorPane = new NoNetEditorPane();
		editorPane.setEditable(false);
		editorPane.setEditorKit(new MyHTMLEditorKit());
		editorPane.addHyperlinkListener(new LinkToolTipListener());
		return getComponentFactory().createScrollPane(editorPane);
	}

	public boolean canHandle(String contentType) {
		return contentType != null && contentType.matches("text/html.*");
	}

	protected void clearContentFormControl() {
		editorPane.setContentType("text/html");
		editorPane.setDocument(JEditorPane.createEditorKitForContentType(
				"text/html").createDefaultDocument());
	}
	
	protected void updateContentFormControl() {
		HTMLEditorKit kit = new HTMLEditorKit();
		HTMLDocument doc = (HTMLDocument)kit.createDefaultDocument();

		try {
			try {
				kit.read(getContentAsReader(), doc, 0);
			} catch (ChangedCharSetException ccse) {
				doc.putProperty("IgnoreCharsetDirective",
						Boolean.TRUE);
				kit.read(getContentAsReader(ccse.getCharSetSpec()), doc, 0);
			}
		} catch (Exception e) {
			editorPane.setText("invalid HTML");
		}
		editorPane.setEditorKit(kit);
		editorPane.setDocument(doc);
	}
	
	private static class MyHTMLEditorKit extends HTMLEditorKit {

		private static final long serialVersionUID = 4540043911244942634L;

		private static final ViewFactory defaultFactory = new MyHTMLFactory();

		public ViewFactory getViewFactory() {
			return defaultFactory;
		}

		private static class MyHTMLFactory extends HTMLEditorKit.HTMLFactory {
			public View create(Element elem) {

				Object o = elem.getAttributes().getAttribute(
						StyleConstants.NameAttribute);
				if (o instanceof HTML.Tag) {
					HTML.Tag kind = (HTML.Tag) o;
					if (kind == HTML.Tag.FRAME || kind == HTML.Tag.FRAMESET
							|| kind == HTML.Tag.OBJECT || kind == HTML.Tag.IMG
							|| kind == HTML.Tag.APPLET) {
						return new NoView(elem);
					}
				}
				return super.create(elem);
			}
		}

		private static class NoView extends View {
			public NoView(Element elem) {
				super(elem);
				setSize(0.0f, 0.0f);
			}

			public int viewToModel(float fx, float fy, Shape a,
					Position.Bias[] bias) {
				return 0;
			}

			public Shape modelToView(int pos, Shape a, Position.Bias b)
					throws BadLocationException {
				return new Rectangle(0, 0);
			}

			public float getPreferredSpan(int axis) {
				return 0.0f;
			}

			public void paint(Graphics g, Shape allocation) {
			}
		}
	}

	private class NoNetEditorPane extends JEditorPane {

		private static final long serialVersionUID = 3954553074162913078L;

		protected InputStream getStream(URL page) throws IOException {
			throw new IOException("We do not support network traffic");
		}
	}

	private class LinkToolTipListener implements HyperlinkListener {
		public LinkToolTipListener() {
		}

		public void hyperlinkUpdate(HyperlinkEvent he) {
			EventType type = he.getEventType();
			if (type == EventType.ENTERED) {
				JEditorPane jep = (JEditorPane) he.getSource();
				URL url = he.getURL();
				if (url != null) {
					jep.setToolTipText(url.toString());
				} else {
					jep.setToolTipText(he.getDescription());
				}
			} else if (type == EventType.EXITED) {
				JEditorPane jep = (JEditorPane) he.getSource();
				jep.setToolTipText("");
			}
		}
	}

}
