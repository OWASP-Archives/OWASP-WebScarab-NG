/**
 * 
 */
package org.owasp.webscarab.ui.forms;

import java.awt.Image;
import java.io.IOException;
import java.net.URI;

import org.owasp.webscarab.domain.Conversation;
import org.owasp.webscarab.domain.NamedValue;
import org.owasp.webscarab.ui.rcp.forms.ResponseForm;
import org.owasp.webscarab.ui.rcp.forms.support.ArrayChangeDetector;
import org.springframework.binding.form.ValidatingFormModel;
import org.springframework.core.io.ClassPathResource;
import org.springframework.richclient.application.support.DefaultApplicationServices;
import org.springframework.richclient.dialog.DialogPage;
import org.springframework.richclient.dialog.FormBackedDialogPage;
import org.springframework.richclient.dialog.TitledPageApplicationDialog;
import org.springframework.richclient.form.Form;
import org.springframework.richclient.form.FormModelHelper;
import org.springframework.richclient.image.AwtImageResource;
import org.springframework.richclient.image.DefaultIconSource;
import org.springframework.richclient.image.ImageSource;
import org.springframework.richclient.test.SpringRichTestCase;

/**
 * @author rdawes
 *
 */
public class ParsedResponseFormTest extends SpringRichTestCase {

	private Conversation c;
	
	private ValidatingFormModel model;
	
	private Form form;
	
	private DialogPage page;
	
	private TitledPageApplicationDialog dialog;
	
	public static void main(String[] args) {
		junit.textui.TestRunner.run(ParsedResponseFormTest.class);
	}

	protected void doSetUp() throws Exception {
		c = new Conversation();
		c.setRequestMethod("GET");
		c.setRequestUri(new URI("http://localhost/"));
		c.setRequestVersion("HTTP/1.0");
		c.setRequestHeader(new NamedValue("Host", "localhost"));
		c.setResponseVersion("HTTP/1.0");
		c.setResponseStatus("302");
		c.setResponseMessage("Moved");
		c.setResponseHeader(new NamedValue("Location",
				"http://localhost/index.html"));
		c.setResponseContent("blah blah".getBytes());
	}

	protected void registerAdditionalServices(
			DefaultApplicationServices defaultapplicationservices) {
		try {
			ImageSource imageSource = new ImageSource() {
				AwtImageResource brokenImageIndicatorResource = new AwtImageResource(
						new ClassPathResource("images/alert/error_obj.gif"));

				Image brokenImageIndicator = brokenImageIndicatorResource
						.getImage();

				public Image getImage(String key) {
					return brokenImageIndicator;
				}

				public AwtImageResource getImageResource(String key) {
					return brokenImageIndicatorResource;
				}
			};

			defaultapplicationservices.setImageSource(imageSource);
			defaultapplicationservices.setIconSource(new DefaultIconSource(
					imageSource));
			defaultapplicationservices.setValueChangeDetector(new ArrayChangeDetector());
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	private void showDialog() {
		model = FormModelHelper.createFormModel(c, true);
		form = new ResponseForm(model);
		page = new FormBackedDialogPage(form);
		dialog = new TitledPageApplicationDialog(page, null) {
			protected void onAboutToShow() {
				setEnabled(page.isPageComplete());
			}
			
			protected boolean onFinish() {
				System.out.println("Commit!");
				form.commit();
				return true;
			}
		};
		dialog.getDialog().setSize(800, 600);
		dialog.showDialog();
	}
	
	public void testHex() throws Exception {
		showDialog();
	}

	public void testText() throws Exception {
		c.setResponseHeader(new NamedValue("Content-Type", "text/html"));
		c.setResponseContent("<html><body>text</body></html>".getBytes());
		showDialog();
	}

}
