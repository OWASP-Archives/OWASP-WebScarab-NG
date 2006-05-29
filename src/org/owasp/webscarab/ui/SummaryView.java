/*
 * SummaryView.java
 *
 * Created on 21 February 2006, 09:52
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.owasp.webscarab.ui;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.tree.TreeModel;

import org.owasp.webscarab.Annotation;
import org.owasp.webscarab.Conversation;
import org.owasp.webscarab.ConversationSummary;
import org.owasp.webscarab.services.ConversationService;
import org.owasp.webscarab.ui.forms.RequestForm;
import org.owasp.webscarab.ui.forms.ResponseForm;
import org.owasp.webscarab.util.UrlUtils;
import org.owasp.webscarab.util.swing.UriTreeModel;
import org.springframework.binding.form.FormModel;
import org.springframework.binding.form.ValidatingFormModel;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.richclient.application.PageComponentContext;
import org.springframework.richclient.application.support.AbstractView;
import org.springframework.richclient.command.CommandGroup;
import org.springframework.richclient.command.support.AbstractActionCommandExecutor;
import org.springframework.richclient.command.support.GlobalCommandIds;
import org.springframework.richclient.form.Form;
import org.springframework.richclient.form.FormModelHelper;
import org.springframework.richclient.settings.SettingsException;
import org.springframework.richclient.settings.SettingsManager;
import org.springframework.richclient.settings.support.TableMemento;
import org.springframework.richclient.util.PopupMenuMouseListener;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.impl.gui.ThreadProxyEventList;
import ca.odell.glazedlists.impl.swing.SwingThreadProxyEventList;
import ca.odell.glazedlists.swing.EventTableModel;

/**
 * 
 * @author rdawes
 */
public class SummaryView extends AbstractView implements ApplicationListener {

	private ConversationService conversationService = null;

	private ConversationTableFormat format = new ConversationTableFormat();

	private EventList<ConversationSummary> conversationSummaryList;

	private JTable conversationTable;

	private TableMemento tableMemento;
	
	private TableModel tableModel;

	private JTree uriTree;

	private UriTreeModel treeModel;

	private ShowConversationExecutor showConversationExecutor = new ShowConversationExecutor();

	private SettingsManager settingsManager;
	
	/** Creates a new instance of SummaryView */
	public SummaryView() {
	}

	/**
	 * @return Returns the conversationSummaryList.
	 */
	public EventList getConversationSummaryList() {
		return conversationSummaryList;
	}

	protected void registerLocalCommandExecutors(PageComponentContext context) {
		context.register(GlobalCommandIds.PROPERTIES, showConversationExecutor);
	}

	@SuppressWarnings("unchecked")
	private TableModel getTableModel() {
		if (tableModel == null) {
			if (getConversationSummaryList() == null) {
				tableModel = new DefaultTableModel(2, 2);
			} else {
				tableModel = new EventTableModel<ConversationSummary>(
						conversationSummaryList, format);
			}
		}
		return tableModel;
	}

	@SuppressWarnings("unchecked")
	private TreeModel getTreeModel() {
		if (treeModel == null) {
			if (getConversationSummaryList() == null) {
				tableModel = new DefaultTableModel(2, 2);
			} else {
				treeModel = new UriTreeModel();
				new TreeBuilder(treeModel, getConversationSummaryList());
			}
		}
		return treeModel;
	}
	/**
	 * @param conversationSummaryList
	 *            The conversationSummaryList to set.
	 */
	public void setConversationSummaryList(
			EventList<ConversationSummary> conversationSummaryList) {
		this.conversationSummaryList = conversationSummaryList;
		this.tableModel = null;
		getConversationTable().setModel(getTableModel());
	}
	
	protected javax.swing.JComponent createControl() {
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setResizeWeight(0.3);
		JScrollPane treeSP = getComponentFactory().createScrollPane(
				getUriTree());
		JScrollPane tableSP = getComponentFactory().createScrollPane(
				getConversationTable());
		splitPane.setTopComponent(treeSP);
		splitPane.setBottomComponent(tableSP);
		return splitPane;
	}

	private JTable getConversationTable() {
		if (conversationTable == null) {
			conversationTable = getComponentFactory().createTable(
					getTableModel());
			if (getSettingsManager() != null) {
				tableMemento = new TableMemento(conversationTable, getClass().getName() + "conversationTable");
				try {
					tableMemento.restoreState(getSettingsManager().getUserSettings());
				} catch (SettingsException se) {}
			}
			conversationTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			conversationTable.getSelectionModel().addListSelectionListener(
					new ListSelectionListener() {
						public void valueChanged(ListSelectionEvent event) {
							showConversationExecutor
									.setEnabled(conversationTable
											.getSelectedRowCount() == 1);
						}
					});
			conversationTable.addMouseListener(new PopupMenuMouseListener() {
				protected boolean onAboutToShow(MouseEvent e) {
					return getSelectedConversation() != null;
				}

				protected JPopupMenu getPopupMenu() {
					return getSelectedConversation() != null
							? createConversationPopupContextMenu()
							: null;
				}
			});
			conversationTable.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() == 2
							&& e.getButton() == MouseEvent.BUTTON1
							&& showConversationExecutor.isEnabled()) {
						showConversationExecutor.execute();
					}
				}
			});
			conversationTable.setSurrendersFocusOnKeystroke(true);
			conversationTable.addKeyListener(new KeyAdapter() {

				@Override
				public void keyTyped(KeyEvent evt) {
					if (evt.getKeyChar() == KeyEvent.VK_ENTER
							&& showConversationExecutor.isEnabled()) {
						showConversationExecutor.execute();
					}
				}

			});
		}
		return conversationTable;
	}

	private JTree getUriTree() {
		if (uriTree == null) {
			uriTree = new JTree(getTreeModel());
			uriTree.setShowsRootHandles(true);
			uriTree.setRootVisible(false);
		}
		return uriTree;
	}

	public void onApplicationEvent(ApplicationEvent applicationEvent) {
		// logger.info(applicationEvent);
	}

	public void setConversationService(ConversationService conversationService) {
		this.conversationService = conversationService;
	}

	private Conversation getSelectedConversation() {
		int row = conversationTable.getSelectedRow();
		if (row == -1)
			return null;
		ConversationSummary summary = conversationSummaryList.get(row);
		return conversationService.getConversation(summary.getId());
	}

	private JPopupMenu createConversationPopupContextMenu() {
		// rename, separator, delete, addPet separator, properties
		CommandGroup group = getWindowCommandManager().createCommandGroup(
				"conversationCommandGroup", new Object[]{"propertiesCommand"});
		return group.createPopupMenu();
	}

	private class ConversationTableFormat implements TableFormat {

		private String[] columnNames = new String[]{"Id", "Date", "Method",
				"Host", "Path", "Parameters", "Status", "Annotation"};

		/*
		 * (non-Javadoc)
		 * 
		 * @see ca.odell.glazedlists.gui.TableFormat#getColumnCount()
		 */
		public int getColumnCount() {
			return columnNames.length;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see ca.odell.glazedlists.gui.TableFormat#getColumnName(int)
		 */
		public String getColumnName(int column) {
			return columnNames[column];
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see ca.odell.glazedlists.gui.TableFormat#getColumnValue(E, int)
		 */
		public Object getColumnValue(Object object, int column) {
			ConversationSummary summary = (ConversationSummary) object;
			if (summary == null)
				return "NULL!";
			switch (column) {
				case 0 :
					return summary.getId();
				case 1 :
					return summary.getDate();
				case 2 :
					return summary.getRequestMethod();
				case 3 :
					return UrlUtils.getSchemeHostPort(summary.getRequestUri());
				case 4 :
					return summary.getRequestUri().getPath();
				case 5 :
					return summary.getRequestUri().getQuery();
				case 6 :
					return summary.getResponseStatus() + " "
							+ summary.getResponseMessage();
				case 7 : 
					Annotation a = conversationService.getAnnotation(summary.getId());
					if (a == null) return a;
					return a.getAnnotation();
			}
			return "Error";
		}

	}

	private class ShowConversationExecutor
			extends
				AbstractActionCommandExecutor {

		public void execute() {
			Conversation conversation = getSelectedConversation();

			ValidatingFormModel model = FormModelHelper.createFormModel(conversation);
			model.setEnabled(false);
			Form requestForm = new RequestForm(model);
			Form responseForm = new ResponseForm(model);
			ConversationView cv = new ConversationView(requestForm, responseForm, null);
			JFrame frame = new JFrame();
			frame.getContentPane().setLayout(new BorderLayout());
			frame.getContentPane().add(cv.getControl(), BorderLayout.CENTER);
			frame.pack();
			frame.setVisible(true);
		}
	}

	private static class TreeBuilder
			implements
				ListEventListener<ConversationSummary> {

		private UriTreeModel model;
		private ThreadProxyEventList<ConversationSummary> list;
		
		public TreeBuilder(UriTreeModel model,
				EventList<ConversationSummary> list) {
			for (int i = 0; i < list.size(); i++) {
				ConversationSummary summary = list.get(i);
				model.add(summary.getRequestUri());
			}
			this.list = new SwingThreadProxyEventList<ConversationSummary>(list);
			this.model = model;
			this.list.addListEventListener(this);
		}

		public void listChanged(ListEvent<ConversationSummary> listChanges) {
			while (listChanges.next()) {
				// get the current change info
				int unsortedIndex = listChanges.getIndex();
				int changeType = listChanges.getType();
				// handle change with the specified index and type
				// we don't handle delete or change events, since changes don't happen
				// and for delete's we can't get the Summary that existed at that position
				// This is a bit of a lose, since it means that we cannot remove 
				if (changeType == ListEvent.INSERT) {
					ConversationSummary summary = listChanges.getSourceList()
							.get(unsortedIndex);
					model.add(summary.getRequestUri());
				}
			}
		}
	}

	public SettingsManager getSettingsManager() {
		return this.settingsManager;
	}

	public void setSettingsManager(SettingsManager settingsManager) {
		this.settingsManager = settingsManager;
	}
}
