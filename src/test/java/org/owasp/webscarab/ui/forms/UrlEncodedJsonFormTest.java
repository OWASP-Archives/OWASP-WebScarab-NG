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
public class UrlEncodedJsonFormTest extends WebScarabUITestCase {

	private Conversation c;
	
	private ValidatingFormModel model;
	
	private Form form;
	
	private DialogPage page;
	
	private TitledPageApplicationDialog dialog;
	
    private String content = "js={" +
    "\"data\" : {" +
    "\"2647\" : {" +
    "\"1\" : 34," +
    "\"2\" : 5," +
    "\"-1\" : 47.415401458740234," +
    "\"-2\" : 0.0028453764971345663," +
    "\"-3\" : 0.0003528574015945196," +
    "\"-4\" : 47.58454132080078" +
    "}" +
    ",\"2648\" : {" +
    "\"1\" : 56," +
    "\"2\" : 4," +
    "\"-1\" : 43.44365692138672," +
    "\"-2\" : 0.002811904065310955," +
    "\"-3\" : 0.0002513712097425014," +
    "\"-4\" : 43.580631256103516" +
    "}" +
    ",\"2649\" : {" +
    "\"1\" : 444," +
    "\"2\" : 54," +
    "\"-1\" : 559.5677490234375," +
    "\"-2\" : 0.03179256245493889," +
    "\"-3\" : 0.004500608425587416," +
    "\"-4\" : 561.6305541992188" +
    "}" +
    ",\"2651\" : {" +
    "\"1\" : 56," +
    "\"2\" : 5," +
    "\"-1\" : 61.01683807373047," +
    "\"-2\" : 0.0031497376039624214," +
    "\"-3\" : 0.0005504416185431182," +
    "\"-4\" : 61.25362014770508" +
    "}" +
    ",\"2652\" : {" +
    "\"1\" : 56," +
    "\"2\" : 5," +
    "\"-1\" : 61.01683807373047," +
    "\"-2\" : 0.0031497376039624214," +
    "\"-3\" : 0.0005504416185431182," +
    "\"-4\" : 61.25362014770508" +
    "}" +
    ",\"2653\" : {" +
    "\"1\" : 56," +
    "\"2\" : 5," +
    "\"-1\" : 65.91927337646484," +
    "\"-2\" : 0.0028703256975859404," +
    "\"-3\" : 0.0006672912277281284," +
    "\"-4\" : 66.18640899658203" +
    "}" +
    ",\"2654\" : {" +
    "\"1\" : 67," +
    "\"2\" : 6," +
    "\"-1\" : 78.96196746826172," +
    "\"-2\" : 0.0034426217898726463," +
    "\"-3\" : 0.0007985358824953437," +
    "\"-4\" : 79.28180694580078" +
    "}" +
    ",\"2656\" : [" +
    "67," +
    "0," +
    "3618," +
    "4355," +
    "1474," +
    "552013" +
    "]" +
    ",\"2655\" : {" +
    "\"1\" : 55," +
    "\"2\" : 6," +
    "\"-1\" : 74.7834701538086," +
    "\"-2\" : 0.005122726317495108," +
    "\"-3\" : 0.0005858910153619945," +
    "\"-4\" : 75.07266998291016" +
    "}" +
    "}" +
    "}";

	public static void main(String[] args) {
		junit.textui.TestRunner.run(UrlEncodedJsonFormTest.class);
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
