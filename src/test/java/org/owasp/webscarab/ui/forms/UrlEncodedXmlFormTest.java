/**
 * 
 */
package org.owasp.webscarab.ui.forms;

import java.net.URI;

import org.owasp.webscarab.domain.Conversation;
import org.owasp.webscarab.domain.NamedValue;
import org.owasp.webscarab.ui.WebScarabUITestCase;
import org.owasp.webscarab.ui.rcp.forms.ResponseForm;
import org.springframework.binding.form.ValidatingFormModel;
import org.springframework.richclient.dialog.DialogPage;
import org.springframework.richclient.dialog.FormBackedDialogPage;
import org.springframework.richclient.dialog.TitledPageApplicationDialog;
import org.springframework.richclient.form.Form;
import org.springframework.richclient.form.FormModelHelper;

/**
 * @author rdawes
 *
 */
public class UrlEncodedXmlFormTest extends WebScarabUITestCase {

	private Conversation c;
	
	private ValidatingFormModel model;
	
	private Form form;
	
	private DialogPage page;
	
	private TitledPageApplicationDialog dialog;
	
    private String content = "data=%3C%3Fxml%20version%3D%221%2E0%22%20encoding%3D%22utf%2D8%22%3F%3E%3CRequest%20SessionID%3D%223873f7e2%2Deab7%2D42f9%2D9344%2Db8e5d4529977%22%20ReturnID%3D%2280000140%22%3E%0A%3CCommand%20Action%3D%22set%22%20DataKey%3D%22%2FTax%2FPersonal%2FFilingStatus%5B1%5D%22%3E%3CFilingStatus%20FilingStatus%3D%223%22%20FilingStatus%5FPrevious%3D%221%22%3E%3C%2FFilingStatus%3E%3C%2FCommand%3E%0A%3C%2FRequest%3E&%5F%5Flzbc%5F%5F=1197560007957";

	public static void main(String[] args) {
		junit.textui.TestRunner.run(UrlEncodedXmlFormTest.class);
	}

	protected void doSetUp() throws Exception {
		c = new Conversation();
		c.setRequestMethod("POST");
		c.setRequestUri(new URI("http://localhost/"));
		c.setRequestVersion("HTTP/1.0");
		c.setRequestHeader(new NamedValue("Host", "localhost"));
		c.setResponseVersion("HTTP/1.0");
		c.setResponseStatus("200");
		c.setResponseMessage("Ok");
        c.setResponseHeader(new NamedValue("Content-Type", "application/x-www-form-urlencoded"));
        c.setResponseHeader(new NamedValue("Content-Length", "1117"));
		c.setResponseContent(content.getBytes());
	}

	private void showDialog() {
		model = FormModelHelper.createFormModel(new Conversation(), true);
		form = new ResponseForm(model);
		page = new FormBackedDialogPage(form);
		dialog = new TitledPageApplicationDialog(page, null) {
			protected void onAboutToShow() {
				setEnabled(page.isPageComplete());
			}
			
            protected void onInitialized() {
                model.setFormObject(c);
            }

            protected void onWindowGainedFocus() {
                model.setFormObject(c);
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
	
	public void testXml() throws Exception {
		showDialog();
	}

}
