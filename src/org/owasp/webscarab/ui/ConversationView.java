/**
 * 
 */
package org.owasp.webscarab.ui;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.owasp.webscarab.domain.Annotation;
import org.owasp.webscarab.domain.Conversation;
import org.owasp.webscarab.domain.ConversationSummary;
import org.owasp.webscarab.services.ConversationService;
import org.owasp.webscarab.ui.forms.AnnotationForm;
import org.owasp.webscarab.ui.forms.RequestForm;
import org.owasp.webscarab.ui.forms.ResponseForm;
import org.owasp.webscarab.ui.forms.support.ConversationFormSupport;
import org.springframework.binding.form.CommitListener;
import org.springframework.binding.form.FormModel;
import org.springframework.binding.form.ValidatingFormModel;
import org.springframework.richclient.application.support.AbstractView;
import org.springframework.richclient.form.Form;
import org.springframework.richclient.form.FormModelHelper;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.swing.EventSelectionModel;

/**
 * @author rdawes
 * 
 */
public class ConversationView extends AbstractView {

	private Form requestForm;

	private Form responseForm;

	private Form annotationForm;

	private EventList<ConversationSummary> conversationSummaryList;

	private ConversationService conversationService;

	private ConversationTableFactory conversationTableFactory;

	private ConversationSummary selectedSummary;

	private ValidatingFormModel conversationModel;

	private ValidatingFormModel annotationModel;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.richclient.application.support.AbstractView#createControl()
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected JComponent createControl() {
		Conversation conversation = new Conversation();
		Annotation annotation = new Annotation();

		conversationModel = ConversationFormSupport
				.createReadOnlyFormModel(conversation);
		annotationModel = FormModelHelper.createFormModel(annotation, false);
		annotationModel.setEnabled(true);
		requestForm = new RequestForm(conversationModel);
		responseForm = new ResponseForm(conversationModel);
		annotationForm = new AnnotationForm(annotationModel);

		annotationModel.addCommitListener(new AnnotationListener());

		JPanel panel = getComponentFactory().createPanel(new BorderLayout());
		JSplitPane mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		mainSplitPane.setResizeWeight(0.5);
		SortedList<ConversationSummary> sorted = new SortedList(
				getConversationSummaryList());
		JTable table = getConversationTableFactory().getConversationTable(
				sorted);
		final EventSelectionModel<ConversationSummary> conversationSelectionModel = new EventSelectionModel<ConversationSummary>(
				conversationSummaryList);
		table.setSelectionModel(conversationSelectionModel);
		JScrollPane tableScrollPane = getComponentFactory().createScrollPane(
				table);

		mainSplitPane.setTopComponent(tableScrollPane);

		table.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent e) {
						if (e.getValueIsAdjusting())
							return;
						if (conversationSelectionModel.isSelectionEmpty())
							return;
						EventList<ConversationSummary> selected = conversationSelectionModel
								.getSelected();
						if (selected.isEmpty()) {
							updateSelection(null);
						} else {
							updateSelection(selected.get(0));
						}
					}
				});
		JSplitPane conversationSplitPane = new JSplitPane();
		conversationSplitPane.setOneTouchExpandable(true);
		conversationSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		conversationSplitPane.setResizeWeight(0.5);
		conversationSplitPane.setTopComponent(requestForm.getControl());
		conversationSplitPane.setBottomComponent(responseForm.getControl());
		mainSplitPane.setBottomComponent(conversationSplitPane);
		panel.add(mainSplitPane, BorderLayout.CENTER);
		panel.add(annotationForm.getControl(), BorderLayout.SOUTH);
		if (getSelectedSummary() != null) {
			conversationSelectionModel.getSelected().add(getSelectedSummary());
		}
		return panel;
	}

	private void updateSelection(ConversationSummary summary) {
		if (annotationModel.isDirty())
			annotationModel.commit();
		if (summary != null) {
			conversationModel.setFormObject(getConversationService()
					.getConversation(summary.getId()));
			Annotation annotation = getConversationService().getAnnotation(
					summary.getId());
			if (annotation == null) {
				annotation = new Annotation();
				annotation.setId(summary.getId());
			}
			annotationModel.setFormObject(annotation);
		} else {
			conversationModel.setFormObject(null);
			annotationModel.setFormObject(null);
		}
	}

	public EventList<ConversationSummary> getConversationSummaryList() {
		return this.conversationSummaryList;
	}

	public void setConversationSummaryList(
			EventList<ConversationSummary> conversationList) {
		this.conversationSummaryList = conversationList;
	}

	public ConversationSummary getSelectedSummary() {
		return this.selectedSummary;
	}

	public void setSelectedSummary(ConversationSummary selectedSummary) {
		this.selectedSummary = selectedSummary;
	}

	/**
	 * @return Returns the conversationService.
	 */
	public ConversationService getConversationService() {
		if (conversationService == null)
			conversationService = (ConversationService) getApplicationContext()
					.getBean("conversationService");
		return conversationService;
	}

	/**
	 * @return Returns the conversationTableFactory.
	 */
	public ConversationTableFactory getConversationTableFactory() {
		if (conversationTableFactory == null)
			conversationTableFactory = (ConversationTableFactory) getApplicationContext()
					.getBean("conversationTableFactory");
		return conversationTableFactory;
	}

	private class AnnotationListener implements CommitListener {

		public void postCommit(FormModel formModel) {
			getConversationService().updateAnnotation((Annotation)formModel.getFormObject());
		}
		public void preCommit(FormModel formModel) {
		}
	}
}
