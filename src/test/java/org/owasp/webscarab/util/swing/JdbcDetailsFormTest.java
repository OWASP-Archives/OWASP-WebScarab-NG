/**
 * 
 */
package org.owasp.webscarab.util.swing;

import java.awt.Image;
import java.io.IOException;

import org.owasp.webscarab.ui.forms.support.ArrayChangeDetector;
import org.owasp.webscarab.util.JdbcConnectionDetails;
import org.springframework.binding.form.ValidatingFormModel;
import org.springframework.core.io.ClassPathResource;
import org.springframework.richclient.application.support.DefaultApplicationServices;
import org.springframework.richclient.dialog.DialogPage;
import org.springframework.richclient.dialog.FormBackedDialogPage;
import org.springframework.richclient.dialog.TitledPageApplicationDialog;
import org.springframework.richclient.form.FormModelHelper;
import org.springframework.richclient.image.AwtImageResource;
import org.springframework.richclient.image.DefaultIconSource;
import org.springframework.richclient.image.ImageSource;
import org.springframework.richclient.test.SpringRichTestCase;

/**
 * @author rdawes
 *
 */
public class JdbcDetailsFormTest extends SpringRichTestCase {

	public static void main(String[] args) {
		junit.textui.TestRunner.run(JdbcDetailsFormTest.class);
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
	 * Test method for 'org.springframework.richclient.form.AbstractForm.createControl()'
	 */
	public void testCreateControl() {
		JdbcConnectionDetails jcd = new JdbcConnectionDetails();
    	jcd.setDriverClassName("org.hsqldb.jdbcDriver");
    	jcd.setUrl("jdbc:hsqldb:file:c:/temp/webscarab");
    	jcd.setUsername("sa");
		ValidatingFormModel model = FormModelHelper.createFormModel(jcd, true);
		final JdbcDetailsForm jdbcDetailsForm = new JdbcDetailsForm(model);
		
		final DialogPage page = new FormBackedDialogPage(jdbcDetailsForm);
		TitledPageApplicationDialog dialog = new TitledPageApplicationDialog(page, null) {
			protected void onAboutToShow() {
				setEnabled(page.isPageComplete());
			}
			
			protected boolean onFinish() {
				System.out.println("Commit!");
				jdbcDetailsForm.commit();
				return true;
			}
		};
		dialog.getDialog().setSize(800, 600);
		dialog.showDialog();
	}

}
