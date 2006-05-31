/**
 * 
 */
package org.owasp.webscarab.ui.forms;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import org.owasp.webscarab.domain.Conversation;
import org.owasp.webscarab.domain.NamedValue;
import org.springframework.binding.form.FormModel;
import org.springframework.binding.value.ValueModel;
import org.springframework.richclient.form.AbstractForm;
import org.springframework.richclient.form.Form;

/**
 * @author rdawes
 * 
 */
public class ParsedResponseForm extends AbstractForm {

	private static final String FORM_ID = "parsedResponseForm";

	private boolean contentShowing = false;
	
	private ContentListener listener;

	private JPanel panel;
	private JPanel topPanel;
	private JSplitPane splitPane;
	private JTabbedPane contentTabbedPane;
	
	public ParsedResponseForm(FormModel model) {
		super(model, FORM_ID);
		listener = new ContentListener();
		model.getValueModel(Conversation.PROPERTY_RESPONSE_HEADERS)
				.addValueChangeListener(listener);
	}

	/**
	 * Construct the form with the required fields.
	 */
	protected JComponent createFormControl() {
		panel = getComponentFactory().createPanel(new BorderLayout());
		topPanel = getComponentFactory().createPanel(new BorderLayout());
		topPanel.add(new JLabel("Still to come, parsed responses"), BorderLayout.CENTER);
		panel.add(topPanel, BorderLayout.CENTER);
		NamedValue[] headers = (NamedValue[]) getValue(Conversation.PROPERTY_RESPONSE_HEADERS);
		String contentType = NamedValue.get("Content-Type", headers);
		updateContentForms(null, contentType);
		return panel;
	}

	private void updateContentForms(String oldContentType, String newContentType) {
//		if (!isChanged(oldContentType, newContentType)) return;
		ValueModel vm = getValueModel(Conversation.PROPERTY_RESPONSE_CONTENT);
		byte[] content = (byte[]) vm.getValue();
		// if the model is read-only, and we have no content, don't show any content forms
		if (!getFormModel().isEnabled() && (content == null || content.length == 0)) {
			if (contentShowing) {
				panel.removeAll();
				panel.add(topPanel, BorderLayout.CENTER);
				contentShowing = false;
				panel.validate();
			}
			return;
		}
		// show the content forms
		if (! contentShowing) {
			panel.removeAll();
			if (splitPane == null)
				splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
			splitPane.setTopComponent(topPanel);
			if (contentTabbedPane == null)
				contentTabbedPane = getComponentFactory().createTabbedPane();
			splitPane.setBottomComponent(contentTabbedPane);
			panel.add(splitPane, BorderLayout.CENTER);
			contentShowing = true;
		}
		Form[] contentForms = getContentForms(newContentType);
		contentTabbedPane.removeAll();
		for (int i=0; i<contentForms.length; i++)
			contentTabbedPane.addTab(contentForms[i].getId(), contentForms[i].getControl());
		panel.validate();
	}
	
	private Form[] getContentForms(String contentType) {
		List<Form> list = new LinkedList<Form>();
		list.add(new HexForm(getFormModel(), Conversation.PROPERTY_RESPONSE_CONTENT));
		if (contentType == null)
			return list.toArray(new Form[list.size()]);
		if (contentType.matches("text.*"))
			list.add(0, new TextForm(getFormModel(), Conversation.PROPERTY_RESPONSE_CONTENT));
		if (contentType.matches("text/html.*"))
			list.add(0, new HtmlForm(getFormModel(), Conversation.PROPERTY_RESPONSE_CONTENT));
		return list.toArray(new Form[list.size()]);
	}
	
	private class ContentListener implements PropertyChangeListener {

		public void propertyChange(PropertyChangeEvent evt) {
			NamedValue[] oldHeaders = (NamedValue[]) evt.getOldValue();
			NamedValue[] newHeaders = (NamedValue[]) evt.getNewValue();
			String oldContentType = NamedValue.get("Content-Type", oldHeaders);
			String newContentType = NamedValue.get("Content-Type", newHeaders);
			updateContentForms(oldContentType, newContentType);
		}

	}

}
