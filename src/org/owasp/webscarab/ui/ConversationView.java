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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.xml.transform.URIResolver;

import org.owasp.webscarab.domain.Annotation;
import org.owasp.webscarab.domain.Conversation;
import org.owasp.webscarab.domain.ConversationSummary;
import org.owasp.webscarab.services.ConversationService;
import org.owasp.webscarab.ui.forms.AnnotationForm;
import org.owasp.webscarab.ui.forms.RequestForm;
import org.owasp.webscarab.ui.forms.ResponseForm;
import org.owasp.webscarab.ui.forms.support.ConversationFormSupport;
import org.owasp.webscarab.util.swing.UriTreeModel;
import org.owasp.webscarab.util.swing.renderers.UriRenderer;
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
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
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

	private EventList<ConversationSummary> conversationSummaryList;

	private ConversationService conversationService;

	private ConversationTableFactory conversationTableFactory;

	private ConversationSummary selectedSummary;

	private ValidatingFormModel conversationModel;

	private ValidatingFormModel annotationModel;

	private JPanel filterPanel;
	
	private FindExecutor findExecutor = new FindExecutor();
	
	private UriTreeModel uriTreeModel;
	
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

		JTextField filterField = getComponentFactory().createTextField();
		uriTreeModel = new UriTreeModel();
		JTree uriTree = new JTree(uriTreeModel);
		uriTree.setRootVisible(false);
		uriTree.setShowsRootHandles(true);
		uriTree.setCellRenderer(new UriRenderer());
		UriMatcher uriMatcher = new UriMatcher(uriTree);
		
		EventList<ConversationSummary> conversationList = getConversationSummaryList();
		FilterList<ConversationSummary> uriFilterList = new FilterList(conversationList, uriMatcher);
		TextFilterator<ConversationSummary> filterator = new ConversationSummaryFilter();
		MatcherEditor<ConversationSummary> matcher = new TextComponentMatcherEditor<ConversationSummary>(filterField, filterator);
		FilterList<ConversationSummary> filterList = new FilterList(uriFilterList, matcher);
		SortedList<ConversationSummary> sortedList = new SortedList<ConversationSummary>(filterList);
		
		JPanel panel = getComponentFactory().createPanel(new BorderLayout());
		JSplitPane mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		mainSplitPane.setResizeWeight(0.5);
		
		JSplitPane topSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		topSplitPane.setResizeWeight(0.2);
		topSplitPane.setOneTouchExpandable(true);

		new UriTreeManager(getConversationSummaryList(), uriTreeModel);
		JScrollPane treeScrollPane = getComponentFactory().createScrollPane(uriTree);
		treeScrollPane.setMinimumSize(new Dimension(200, 30));
		topSplitPane.setLeftComponent(treeScrollPane);
		
		filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		filterPanel.add(getComponentFactory().createLabelFor("filter", filterField));
		filterPanel.add(filterField);
		
		JTable table = getConversationTableFactory().getConversationTable(sortedList);
		final EventSelectionModel<ConversationSummary> conversationSelectionModel = new EventSelectionModel<ConversationSummary>(
				sortedList);
		table.setSelectionModel(conversationSelectionModel);
		JScrollPane tableScrollPane = getComponentFactory().createScrollPane(
				table);
		tableScrollPane.setMinimumSize(new Dimension(100, 60));
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(tableScrollPane, BorderLayout.CENTER);
		topPanel.add(filterPanel, BorderLayout.SOUTH);
		topSplitPane.setRightComponent(topPanel);
		mainSplitPane.setTopComponent(topSplitPane);

		table.getSelectionModel().addListSelectionListener(
			new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					if (e.getValueIsAdjusting())
						return;
					EventList<ConversationSummary> selected = conversationSelectionModel
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
	
	private class UriTreeManager implements ListEventListener<ConversationSummary> {

		private EventList<ConversationSummary> list;
		private List<URI> uriList = new ArrayList<URI>();
		private UriTreeModel uriTree;
		
		public UriTreeManager(EventList<ConversationSummary> list, UriTreeModel uriTree) {
			this.list = list;
			this.uriTree = uriTree;
			populateExisting();
			// the conversationSummary list is only ever updated on the EDT
			list.addListEventListener(this); 
		}
		
		private void populateExisting() {
			list.getReadWriteLock().readLock().lock();
			uriList.clear();
			Iterator<ConversationSummary> it = list.iterator();
			while (it.hasNext()) {
				URI uri = it.next().getRequestUri();
				uriList.add(uri);
				uriTree.add(uri);
			}
			list.getReadWriteLock().readLock().unlock();
		}
		
		public void listChanged(ListEvent<ConversationSummary> evt) {
			while (evt.next()) {
				int index = evt.getIndex();
				if (evt.getType() == ListEvent.DELETE) {
					uriTree.remove(uriList.remove(index));
				} else if (evt.getType() == ListEvent.INSERT) {
					URI uri = list.get(index).getRequestUri();
					uriList.add(index, uri);
					uriTree.add(uri);
				}
			}
		}
		
	}
	
	private class UriMatcher extends AbstractMatcherEditor<ConversationSummary> implements TreeSelectionListener {

		private TreeSelectionModel tsm;
		private Matcher<ConversationSummary> matcher;
		
		public UriMatcher(JTree tree) {
			this.tsm = tree.getSelectionModel();
			matcher = new Matcher<ConversationSummary>() {
				public boolean matches(ConversationSummary summary) {
					if (tsm.getSelectionCount() == 0) return true;
					TreePath[] selection = tsm.getSelectionPaths(); 
					for (int i=0; i<selection.length; i++) {
						Object lastComponent = selection[i].getLastPathComponent(); 
						if (!(lastComponent instanceof URI)) continue;
						URI uri = (URI) lastComponent;
						if (summary.getRequestUri().toString().startsWith(uri.toString()))
							return true;
					}
					return false;
				}
			};
			tree.addTreeSelectionListener(this);
		}
		
		/* (non-Javadoc)
		 * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
		 */
		public void valueChanged(TreeSelectionEvent e) {
			if (tsm.getSelectionCount() == 0) {
				fireMatchAll();
			} else {
				fireChanged(matcher);
			}
		}

		/* (non-Javadoc)
		 * @see ca.odell.glazedlists.matchers.AbstractMatcherEditor#getMatcher()
		 */
		@Override
		public Matcher<ConversationSummary> getMatcher() {
			return matcher;
		}
		
	}
}
