/**
 * 
 */
package org.owasp.webscarab.ui.rcp.forms;

import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.springframework.binding.form.FormModel;

/**
 * @author rdawes
 * 
 */
public class ImageForm extends AbstractContentForm {

	private static String FORM_ID = "imageForm";

	private JLabel imageLabel;

	public ImageForm(FormModel model, String headerPropertyName,
			String contentPropertyName) {
		super(model, FORM_ID, headerPropertyName, contentPropertyName);
	}

	@Override
	protected JComponent createContentFormControl() {
		imageLabel = new JLabel();
		return getComponentFactory().createScrollPane(imageLabel);
	}

	public boolean canHandle(String contentType) {
		return contentType != null && contentType.matches("image/.*");
	}

	protected void updateContentFormControl() {
		InputStream content = getContentAsStream();
		if (content == null) {
			imageLabel.setIcon(null);
		} else {
			try {
				Image image = ImageIO.read(content);
				if (image != null) {
					imageLabel.setIcon(new ImageIcon(image));
				} else {
					imageLabel.setIcon(null);
				}
			} catch (IOException e) {
				e.printStackTrace();
				imageLabel.setIcon(null);
			}
		}
	}

	protected void clearContentFormControl() {
		imageLabel.setIcon(null);
	}

}
