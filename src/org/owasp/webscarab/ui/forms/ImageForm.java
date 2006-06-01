/**
 * 
 */
package org.owasp.webscarab.ui.forms;

import java.awt.Image;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentAdapter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import org.springframework.binding.form.FormModel;
import org.springframework.binding.value.ValueModel;
import org.springframework.richclient.form.AbstractForm;

/**
 * @author rdawes
 * 
 */
public class ImageForm extends AbstractForm implements ContentForm {

	private static String FORM_ID = "imageForm";

	private ContentListener listener;

	private ValueModel vm;

	private JLabel imageLabel;

	private JScrollPane scrollPane = null;
	
	public ImageForm(FormModel model, String contentPropertyName) {
		super(model, FORM_ID);
		vm = model.getValueModel(contentPropertyName);
	}

	@Override
	protected JComponent createFormControl() {
		if (scrollPane == null) {
			listener = new ContentListener();
			vm.addValueChangeListener(listener);
			imageLabel = new JLabel();
			scrollPane = getComponentFactory().createScrollPane(
					imageLabel);
			// we use a component listener to delay rendering the image until
			// someone is actually interested in it i.e. the component is shown
			// we have to add it to the scrollPane, since the imageLabel does not
			// receive the necessary events for some reason.
			scrollPane.addComponentListener(listener);
		}
		return scrollPane;
	}

	public boolean canHandle(String contentType) {
		return contentType != null && contentType.matches("image/.*");
	}

	private void updateFormControl() {
		byte[] content = (byte[]) vm.getValue();
		if (content == null || content.length == 0) {
			imageLabel.setIcon(null);
		} else {
			try {
				ByteArrayInputStream bais = new ByteArrayInputStream(content);
				Image image = ImageIO.read(bais);
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

	private class ContentListener extends ComponentAdapter implements
			PropertyChangeListener {

		private boolean upToDate = false;

		public void propertyChange(PropertyChangeEvent evt) {
			upToDate = false;
			if (imageLabel != null && imageLabel.isShowing()) {
				updateFormControl();
				upToDate = true;
			}
		}

		public void componentShown(ComponentEvent e) {
			if (!upToDate) {
				updateFormControl();
				upToDate = true;
			}
		}

	}

}
