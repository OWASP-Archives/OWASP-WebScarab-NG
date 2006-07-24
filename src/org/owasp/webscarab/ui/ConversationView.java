/**
 * 
 */
package org.owasp.webscarab.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
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
import org.springframework.richclient.application.PageComponentContext;
import org.springframework.richclient.application.support.AbstractView;
import org.springframework.richclient.command.support.AbstractActionCommandExecutor;
import org.springframework.richclient.form.Form;
import org.springframework.richclient.form.FormModelHelper;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;

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

	private JPanel filterPanel;
	
	private FindExecutor findExecutor = new FindExecutor();
	
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
		
		filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JTextField filterField = getComponentFactory().createTextField();
		filterPanel.add(getComponentFactory().createLabelFor("filter", filterField));
		filterPanel.add(filterField);
		
		TextFilterator<ConversationSummary> filterator = new ConversationSummaryFilter();
		MatcherEditor<ConversationSummary> matcher = new TextComponentMatcherEditor<ConversationSummary>(filterField, filterator);
		FilterList<ConversationSummary> filterList = new FilterList(getConversationSummaryList(), matcher);
		SortedList<ConversationSummary> sortedList = new SortedList<ConversationSummary>(filterList);
		
		JTable table = getConversationTableFactory().getConversationTable(sortedList);
		final EventSelectionModel<ConversationSummary> conversationSelectionModel = new EventSelectionModel<ConversationSummary>(
				sortedList);
		table.setSelectionModel(conversationSelectionModel);
		JScrollPane tableScrollPane = getComponentFactory().createScrollPane(
				table);
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(tableScrollPane, BorderLayout.CENTER);
		topPanel.add(filterPanel, BorderLayout.SOUTH);
		mainSplitPane.setTopComponent(topPanel);

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
			}
		);
		table.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				findExecutor.setEnabled(true);
			}
			@Override
			public void focusLost(FocusEvent e) {
//				findExecutor.setEnabled(false);
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
	
	
	/* (non-Javadoc)
	 * @see org.springframework.richclient.application.support.AbstractView#registerLocalCommandExecutors(org.springframework.richclient.application.PageComponentContext)
	 */
	@Override
	protected void registerLocalCommandExecutors(PageComponentContext context) {
        context.register("findCommand", findExecutor);
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
	
	private class ConversationSummaryFilter implements TextFilterator<ConversationSummary> {

		public void getFilterStrings(List<String> list, ConversationSummary summary) {
			list.add(summary.getRequestMethod());
			list.add(summary.getRequestUri().toString());
			list.add(summary.getResponseStatus());
			list.add(summary.getResponseMessage());
			list.add(summary.getPlugin());
			Annotation annotation = getConversationService().getAnnotation(summary.getId());
			if (annotation != null && !"".equals(annotation.getAnnotation()))
				list.add(annotation.getAnnotation());
		}
		
	}
	
	private class FindExecutor extends AbstractActionCommandExecutor {
		public void execute() {
			filterPanel.setVisible(true);
		}
	}
}
