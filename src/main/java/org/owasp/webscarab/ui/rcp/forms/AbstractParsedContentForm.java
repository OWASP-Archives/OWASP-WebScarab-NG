/**
 * 
 */
package org.owasp.webscarab.ui.rcp.forms;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import org.springframework.binding.form.FormModel;
import org.springframework.richclient.form.AbstractForm;

/**
 * @author rdawes
 * 
 */
public abstract class AbstractParsedContentForm extends AbstractForm {

	private JPanel panel;

	private JSplitPane splitPane;

	private JTabbedPane contentTabbedPane;

	private String headerProperty;

	private String contentProperty;

	public AbstractParsedContentForm(FormModel model, String formId,
			String headerProperty, String contentProperty) {
		super(model, formId);
		this.headerProperty = headerProperty;
		this.contentProperty = contentProperty;
		// this listener shows or hides the content pane as necessary
	}

	protected abstract JComponent getParsedHeaderComponent();

	/**
	 * Construct the form with the required fields.
	 */
	protected JComponent createFormControl() {
		if (panel == null) {
			panel = getComponentFactory().createPanel(new BorderLayout());
			splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
			splitPane.setOneTouchExpandable(true);
			splitPane.setResizeWeight(0.5);
			splitPane.setTopComponent(getParsedHeaderComponent());
			contentTabbedPane = new ContentTabbedPane(getFormModel(),
					headerProperty, contentProperty);
			splitPane.setBottomComponent(contentTabbedPane);
			panel.add(splitPane, BorderLayout.CENTER);
		}
		return panel;
	}
}
