/*
 * SummaryView.java
 *
 * Created on 21 February 2006, 09:52
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.owasp.webscarab.ui.rcp;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

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

import org.bushe.swing.event.EventService;
import org.jdesktop.swingx.JXTable;
import org.owasp.webscarab.domain.Annotation;
import org.owasp.webscarab.domain.Conversation;
import org.owasp.webscarab.services.ConversationService;
import org.owasp.webscarab.util.UrlUtils;
import org.owasp.webscarab.util.swing.UriTreeModel;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.richclient.application.PageComponentContext;
import org.springframework.richclient.application.PageDescriptor;
import org.springframework.richclient.application.support.AbstractView;
import org.springframework.richclient.application.support.DefaultViewDescriptor;
import org.springframework.richclient.application.support.SingleViewPageDescriptor;
import org.springframework.richclient.command.CommandGroup;
import org.springframework.richclient.command.support.AbstractActionCommandExecutor;
import org.springframework.richclient.command.support.GlobalCommandIds;
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

	private EventService eventService;

	private ConversationService conversationService = null;

	private ConversationTableFormat format = new ConversationTableFormat();

	private EventList<Conversation> conversationList;

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
	public EventList<Conversation> getConversationList() {
		return conversationList;
	}

	protected void registerLocalCommandExecutors(PageComponentContext context) {
		context.register(GlobalCommandIds.PROPERTIES, showConversationExecutor);
	}

	private TableModel getTableModel() {
		if (tableModel == null) {
			if (getConversationList() == null) {
				tableModel = new DefaultTableModel(2, 2);
			} else {
				tableModel = new EventTableModel<Conversation>(
						conversationList, format);
			}
		}
		return tableModel;
	}

	private TreeModel getTreeModel() {
		if (treeModel == null) {
			if (getConversationList() == null) {
				tableModel = new DefaultTableModel(2, 2);
			} else {
				treeModel = new UriTreeModel();
				new TreeBuilder(treeModel, getConversationList());
			}
		}
		return treeModel;
	}
	/**
	 * @param conversationSummaryList
	 *            The conversationSummaryList to set.
	 */
	public void setConversationList(
			EventList<Conversation> conversationList) {
		this.conversationList = conversationList;
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
			if (conversationTable instanceof JXTable)
				((JXTable) conversationTable).setColumnControlVisible(true);
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

	/**
	 * @return Returns the conversationService.
	 */
	public ConversationService getConversationService() {
		if (conversationService == null)
			conversationService = (ConversationService) getApplicationContext()
					.getBean("conversationService");
		return conversationService;
	}

	private Conversation getSelectedConversation() {
		int row = conversationTable.getSelectedRow();
		if (row == -1)
			return null;
		return conversationList.get(row);
	}

	private JPopupMenu createConversationPopupContextMenu() {
		// rename, separator, delete, addPet separator, properties
		CommandGroup group = getWindowCommandManager().createCommandGroup(
				"conversationCommandGroup", new Object[]{"propertiesCommand"});
		return group.createPopupMenu();
	}

	private class ConversationTableFormat implements TableFormat<Conversation> {

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
		public Object getColumnValue(Conversation conversation, int column) {
			if (conversation == null)
				return "NULL!";
			switch (column) {
				case 0 :
					return conversation.getId();
				case 1 :
					return conversation.getDate();
				case 2 :
					return conversation.getRequestMethod();
				case 3 :
					return UrlUtils.getSchemeHostPort(conversation.getRequestUri());
				case 4 :
					return conversation.getRequestUri().getPath();
				case 5 :
					return conversation.getRequestUri().getQuery();
				case 6 :
					return conversation.getResponseStatus() + " "
							+ conversation.getResponseMessage();
				case 7 :
					Annotation a = getConversationService().getAnnotation(conversation.getId());
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
			DefaultViewDescriptor viewDescriptor = (DefaultViewDescriptor) getApplicationContext().getBean("conversationView");
			Map<String, Object> viewProperties = new HashMap<String, Object>();
			viewProperties.put("conversationList", getConversationList());
			viewProperties.put("conversationService", getConversationService());
			viewProperties.put("selectedSummary", getSelectedConversation());
			viewDescriptor.setViewProperties(viewProperties);
			PageDescriptor pageDescriptor = new SingleViewPageDescriptor(viewDescriptor);
			DescriptorApplicationWindow window = new DescriptorApplicationWindow();
			window.showPage(pageDescriptor);
		}
	}

	private static class TreeBuilder
			implements
				ListEventListener<Conversation> {

		private UriTreeModel model;
		private ThreadProxyEventList<Conversation> list;

		public TreeBuilder(UriTreeModel model,
				EventList<Conversation> list) {
			for (int i = 0; i < list.size(); i++) {
				Conversation conversation= list.get(i);
				model.add(conversation.getRequestUri());
			}
			this.list = new SwingThreadProxyEventList<Conversation>(list);
			this.model = model;
			this.list.addListEventListener(this);
		}

		public void listChanged(ListEvent<Conversation> listChanges) {
			while (listChanges.next()) {
				// get the current change info
				int unsortedIndex = listChanges.getIndex();
				int changeType = listChanges.getType();
				// handle change with the specified index and type
				// we don't handle delete or change events, since changes don't happen
				// and for delete's we can't get the Summary that existed at that position
				// This is a bit of a lose, since it means that we cannot remove
				if (changeType == ListEvent.INSERT) {
					Conversation conversation = listChanges.getSourceList()
							.get(unsortedIndex);
					model.add(conversation.getRequestUri());
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

	public EventService getEventService() {
		return this.eventService;
	}

	public void setEventService(EventService eventService) {
		this.eventService = eventService;
	}
}
