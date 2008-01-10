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
public class JsonFormTest extends WebScarabUITestCase {

	private Conversation c;
	
	private ValidatingFormModel model;
	
	private Form form;
	
	private DialogPage page;
	
	private TitledPageApplicationDialog dialog;
	
	private String content = "{\n" +
			"\"data\" : {\n" +
			"\"2647\" : {\n" +
			"\"1\" : 34,\n" +
			"\"2\" : 5,\n" +
			"\"-1\" : 47.415401458740234,\n" +
			"\"-2\" : 0.0028453764971345663,\n" +
			"\"-3\" : 0.0003528574015945196,\n" +
			"\"-4\" : 47.58454132080078\n" +
			"}\n" +
			",\"2648\" : {\n" +
			"\"1\" : 56,\n" +
			"\"2\" : 4,\n" +
			"\"-1\" : 43.44365692138672,\n" +
			"\"-2\" : 0.002811904065310955,\n" +
			"\"-3\" : 0.0002513712097425014,\n" +
			"\"-4\" : 43.580631256103516\n" +
			"}\n" +
			",\"2649\" : {\n" +
			"\"1\" : 444,\n" +
			"\"2\" : 54,\n" +
			"\"-1\" : 559.5677490234375,\n" +
			"\"-2\" : 0.03179256245493889,\n" +
			"\"-3\" : 0.004500608425587416,\n" +
			"\"-4\" : 561.6305541992188\n" +
			"}\n" +
			",\"2651\" : {\n" +
			"\"1\" : 56,\n" +
			"\"2\" : 5,\n" +
			"\"-1\" : 61.01683807373047,\n" +
			"\"-2\" : 0.0031497376039624214,\n" +
			"\"-3\" : 0.0005504416185431182,\n" +
			"\"-4\" : 61.25362014770508\n" +
			"}\n" +
			",\"2652\" : {\n" +
			"\"1\" : 56,\n" +
			"\"2\" : 5,\n" +
			"\"-1\" : 61.01683807373047,\n" +
			"\"-2\" : 0.0031497376039624214,\n" +
			"\"-3\" : 0.0005504416185431182,\n" +
			"\"-4\" : 61.25362014770508\n" +
			"}\n" +
			",\"2653\" : {\n" +
			"\"1\" : 56,\n" +
			"\"2\" : 5,\n" +
			"\"-1\" : 65.91927337646484,\n" +
			"\"-2\" : 0.0028703256975859404,\n" +
			"\"-3\" : 0.0006672912277281284,\n" +
			"\"-4\" : 66.18640899658203\n" +
			"}\n" +
			",\"2654\" : {\n" +
			"\"1\" : 67,\n" +
			"\"2\" : 6,\n" +
			"\"-1\" : 78.96196746826172,\n" +
			"\"-2\" : 0.0034426217898726463,\n" +
			"\"-3\" : 0.0007985358824953437,\n" +
			"\"-4\" : 79.28180694580078\n" +
			"}\n" +
            ",\"2656\" : [\n" +
            "67,\n" +
            "0,\n" +
            "3618,\n" +
            "4355,\n" +
            "1474,\n" +
            "552013\n" +
            "]\n" +
			",\"2655\" : {\n" +
			"\"1\" : 55,\n" +
			"\"2\" : 6,\n" +
			"\"-1\" : 74.7834701538086,\n" +
			"\"-2\" : 0.005122726317495108,\n" +
			"\"-3\" : 0.0005858910153619945,\n" +
			"\"-4\" : 75.07266998291016\n" +
			"}\n" +
			"}\n" +
			"}";
	
	public static void main(String[] args) {
		junit.textui.TestRunner.run(JsonFormTest.class);
	}

	protected void doSetUp() throws Exception {
		c = new Conversation();
		c.setRequestMethod("GET");
		c.setRequestUri(new URI("http://localhost/"));
		c.setRequestVersion("HTTP/1.0");
		c.setRequestHeader(new NamedValue("Host", "localhost"));
		c.setResponseVersion("HTTP/1.0");
		c.setResponseStatus("200");
		c.setResponseMessage("Ok");
		c.setResponseHeader(new NamedValue("Content-Type", "application/json"));
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
