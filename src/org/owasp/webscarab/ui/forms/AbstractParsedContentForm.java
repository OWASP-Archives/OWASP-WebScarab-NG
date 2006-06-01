/**
 * 
 */
package org.owasp.webscarab.ui.forms;

import java.awt.BorderLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

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

	private boolean contentShowing = false;

	private Listener listener;

	private JPanel panel;

	private JPanel topPanel;

	private JSplitPane splitPane;

	private JTabbedPane contentTabbedPane;

	private String headerProperty;

	private String contentProperty;

	private boolean editable;
	
	public AbstractParsedContentForm(FormModel model, String formId,
			String headerProperty, String contentProperty, boolean editable) {
		super(model, formId);
		this.headerProperty = headerProperty;
		this.contentProperty = contentProperty;
		this.editable = editable;
		// this listener shows or hides the content pane as necessary
		listener = new Listener();
		model.getValueModel(contentProperty).addValueChangeListener(listener);
	}

	protected abstract JComponent getParsedHeaderComponent();

	/**
	 * Construct the form with the required fields.
	 */
	protected JComponent createFormControl() {
		if (panel == null) {
			panel = getComponentFactory().createPanel(new BorderLayout());
			topPanel = getComponentFactory().createPanel(new BorderLayout());
			topPanel.add(getParsedHeaderComponent(), BorderLayout.CENTER);
			panel.add(topPanel, BorderLayout.CENTER);
			contentTabbedPane = new ContentTabbedPane(getFormModel(),
					headerProperty, contentProperty, editable && getFormModel().isEnabled());
			updateContentVisibility();
			panel.addComponentListener(listener);
		}
		return panel;
	}

	private boolean shouldContentBeVisible() {
		byte[] content = (byte[]) getValueModel(contentProperty).getValue();
		boolean visible = false;
		if (content != null && content.length > 0) {
			// should be visible regardless
			visible = true;
		} else if (editable && getFormModel().isEnabled()) {
			// should also be visible
			visible = true;
		}
		return visible;
	}

	private void updateContentVisibility() {
		boolean visible = shouldContentBeVisible();
		if (visible && !contentShowing) {
			panel.removeAll();
			if (splitPane == null)
				splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
			splitPane.setTopComponent(topPanel);
			if (contentTabbedPane == null)
				contentTabbedPane = getComponentFactory().createTabbedPane();
			splitPane.setBottomComponent(contentTabbedPane);
			panel.add(splitPane, BorderLayout.CENTER);
			contentShowing = true;
			panel.invalidate();
		} else if (!visible && contentShowing) {
			panel.removeAll();
			panel.add(topPanel, BorderLayout.CENTER);
			contentShowing = false;
			panel.invalidate();
		}
	}

	private class Listener extends ComponentAdapter implements
			PropertyChangeListener {

		private boolean upToDate = false;

		public void propertyChange(PropertyChangeEvent evt) {
			upToDate = false;
			if (panel != null && panel.isShowing()) {
				updateContentVisibility();
				upToDate = true;
			}
		}

		public void componentShown(ComponentEvent e) {
			if (!upToDate) {
				updateContentVisibility();
				upToDate = true;
			}
		}

	}

}
