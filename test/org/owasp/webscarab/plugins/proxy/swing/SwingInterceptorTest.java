/**
 * 
 */
package org.owasp.webscarab.plugins.proxy.swing;

import java.awt.Image;
import java.io.IOException;
import java.net.URI;

import org.owasp.webscarab.Conversation;
import org.owasp.webscarab.NamedValue;
import org.owasp.webscarab.ui.forms.support.ArrayChangeDetector;
import org.springframework.core.io.ClassPathResource;
import org.springframework.richclient.application.support.DefaultApplicationServices;
import org.springframework.richclient.image.AwtImageResource;
import org.springframework.richclient.image.DefaultIconSource;
import org.springframework.richclient.image.ImageSource;
import org.springframework.richclient.test.SpringRichTestCase;

/**
 * @author rdawes
 * 
 */
public class SwingInterceptorTest extends SpringRichTestCase {

	private Conversation c;

	public static void main(String[] args) {
		junit.textui.TestRunner.run(SwingInterceptorTest.class);
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

	/*
	 * Test method for
	 * 'org.owasp.webscarab.plugins.proxy.swing.SwingInterceptor.editRequest(Conversation,
	 * Annotation)'
	 */
	public void testEditRequest() throws IOException {
		new SwingInterceptor().editRequest(c, null);
		System.out.println(c.getRequestMethod() + " " + c.getRequestUri() + c.getRequestVersion());
		NamedValue[] nv = c.getRequestHeaders();
		if (nv != null)
			for (int i=0; i< nv.length; i++)
				System.out.println(nv[i].getName() + ": " + nv[i].getValue());
		System.out.println();
		byte[] content = c.getRequestContent();
		if (content != null)
			System.out.println(new String(content));
	}

	/*
	 * Test method for
	 * 'org.owasp.webscarab.plugins.proxy.swing.SwingInterceptor.editResponse(Conversation,
	 * Annotation)'
	 */
	public void btestEditResponse() {

	}

}
