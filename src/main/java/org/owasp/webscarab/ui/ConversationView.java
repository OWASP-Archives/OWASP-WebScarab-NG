/**
 *
 */
package org.owasp.webscarab.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.net.URI;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.bushe.swing.event.EventService;
import org.bushe.swing.event.EventServiceEvent;
import org.owasp.webscarab.domain.Annotation;
import org.owasp.webscarab.domain.Conversation;
import org.owasp.webscarab.services.ConversationService;
import org.owasp.webscarab.ui.forms.AnnotationForm;
import org.owasp.webscarab.ui.forms.RequestForm;
import org.owasp.webscarab.ui.forms.ResponseForm;
import org.owasp.webscarab.ui.forms.support.ConversationFormSupport;
import org.springframework.binding.form.CommitListener;
import org.springframework.binding.form.FormModel;
import org.springframework.richclient.application.PageComponent;
import org.springframework.richclient.application.PageComponentContext;
import org.springframework.richclient.application.support.AbstractView;
import org.springframework.richclient.command.support.AbstractActionCommandExecutor;
import org.springframework.richclient.form.Form;
import org.springframework.richclient.form.FormModelHelper;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.matchers.Matcher;
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

	private EventList<Conversation> conversationList;

    private EventService eventService;

	private ConversationService conversationService;

	private ConversationTableFactory conversationTableFactory;

	private Conversation selectedConversation;

	private FormModel conversationModel;

	private FormModel annotationModel;

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
		requestForm = new RequestForm(conversationModel);
		responseForm = new ResponseForm(conversationModel);
		annotationForm = new AnnotationForm(annotationModel);

		annotationModel.addCommitListener(new AnnotationListener());

		JTextField filterField = getComponentFactory().createTextField();
		UriMatcher uriMatcher = new UriMatcher();

		EventList<Conversation> conversationList = getConversationList();
		FilterList<Conversation> uriFilterList = new FilterList(conversationList, uriMatcher);
		TextFilterator<Conversation> filterator = new ConversationFilter();
		MatcherEditor<Conversation> matcher = new TextComponentMatcherEditor<Conversation>(filterField, filterator);
		FilterList<Conversation> filterList = new FilterList(uriFilterList, matcher);
		SortedList<Conversation> sortedList = new SortedList<Conversation>(filterList);

		JPanel panel = getComponentFactory().createPanel(new BorderLayout());
		JSplitPane mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		mainSplitPane.setResizeWeight(0.5);

		filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		filterPanel.add(getComponentFactory().createLabelFor("filter", filterField));
		filterPanel.add(filterField);

		JTable table = getConversationTableFactory().getConversationTable(sortedList);
		final EventSelectionModel<Conversation> conversationSelectionModel = new EventSelectionModel<Conversation>(
				sortedList);
		table.setSelectionModel(conversationSelectionModel);
		JScrollPane tableScrollPane = getComponentFactory().createScrollPane(
				table);
		tableScrollPane.setMinimumSize(new Dimension(100, 60));
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(tableScrollPane, BorderLayout.CENTER);
		topPanel.add(filterPanel, BorderLayout.SOUTH);
		mainSplitPane.setTopComponent(topPanel);

		table.getSelectionModel().addListSelectionListener(
			new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					if (e.getValueIsAdjusting())
						return;
					EventList<Conversation> selected = conversationSelectionModel
							.getSelected();
					if (selected.isEmpty() || selected.size() > 1) {
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
		if (getSelectedConversation() != null) {
			conversationSelectionModel.getSelected().add(getSelectedConversation());
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


	private void updateSelection(Conversation conversation) {
		if (annotationModel.isDirty())
			annotationModel.commit();
		if (conversation != null) {
			conversationModel.setFormObject(getConversationService()
					.getConversation(conversation.getId()));
			Annotation annotation = getConversationService().getAnnotation(
					conversation.getId());
			if (annotation == null) {
				annotation = new Annotation();
				annotation.setId(conversation.getId());
			}
			annotationModel.setFormObject(annotation);
		} else {
			conversationModel.setFormObject(new Conversation());
			annotationModel.setFormObject(new Annotation());
		}
	}

	public EventList<Conversation> getConversationList() {
		return this.conversationList;
	}

	public void setConversationList(
			EventList<Conversation> conversationList) {
		this.conversationList = conversationList;
	}

	public Conversation getSelectedConversation() {
		return this.selectedConversation;
	}

	public void setSelectedConversation(Conversation selectedConversation) {
		this.selectedConversation = selectedConversation;
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

	private class ConversationFilter implements TextFilterator<Conversation> {

		public void getFilterStrings(List<String> list, Conversation conversation) {
			list.add(conversation.getRequestMethod());
			list.add(conversation.getRequestUri().toString());
			list.add(conversation.getResponseStatus());
			list.add(conversation.getResponseMessage());
			list.add(conversation.getSource());
			Annotation annotation = getConversationService().getAnnotation(conversation.getId());
			if (annotation != null && !"".equals(annotation.getAnnotation()))
				list.add(annotation.getAnnotation());
		}

	}

	private class FindExecutor extends AbstractActionCommandExecutor {
		public void execute() {
			filterPanel.setVisible(true);
		}
	}

	private class UriMatcher extends AbstractMatcherEditor<Conversation> {

		private Matcher<Conversation> matcher;

        private URI[] selection = new URI[0];

		public UriMatcher() {
			matcher = new Matcher<Conversation>() {
				public boolean matches(Conversation conversation) {
					if (selection.length == 0) return true;
					for (int i=0; i<selection.length; i++) {
						if (conversation.getRequestUri().toString().startsWith(selection[i].toString()))
							return true;
					}
					return false;
				}
			};
            eventService.subscribeStrongly(URISelectionEvent.class, new SwingEventSubscriber() {
                protected void handleEventOnEDT(EventServiceEvent evt) {
                    if (evt instanceof URISelectionEvent) {
                        URISelectionEvent use = (URISelectionEvent) evt;
                        Object source = use.getSource();
                        if (source instanceof PageComponent) {
                            PageComponent pc = (PageComponent) source;
                            if (pc.getContext().getPage().equals(getContext().getPage())) {
                                selection = use.getSelection();
                                for (int i=0; i<selection.length; i++)
                                    System.out.println("Sel["+i+"] : " + selection[i]);
                                if (selection.length == 0) {
                                    fireMatchNone();
                                } else {
                                    fireChanged(matcher);
                                }
                            }
                        }
                    }
                }

            });
		}

		/* (non-Javadoc)
		 * @see ca.odell.glazedlists.matchers.AbstractMatcherEditor#getMatcher()
		 */
		@Override
		public Matcher<Conversation> getMatcher() {
			return matcher;
		}

	}

    /**
     * @param eventService the eventService to set
     */
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }
}
