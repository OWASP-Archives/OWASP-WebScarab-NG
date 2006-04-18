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

import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.owasp.webscarab.Conversation;
import org.owasp.webscarab.ConversationSummary;
import org.owasp.webscarab.services.ConversationService;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.richclient.application.PageComponentContext;
import org.springframework.richclient.application.support.AbstractView;
import org.springframework.richclient.command.CommandGroup;
import org.springframework.richclient.command.support.AbstractActionCommandExecutor;
import org.springframework.richclient.command.support.GlobalCommandIds;
import org.springframework.richclient.dialog.FormBackedDialogPage;
import org.springframework.richclient.dialog.TitledPageApplicationDialog;
import org.springframework.richclient.form.FormModelHelper;
import org.springframework.richclient.util.PopupMenuMouseListener;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.gui.TableFormat;
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

	private TableModel tableModel;

	private ShowConversationExecutor showConversationExecutor = new ShowConversationExecutor();

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
		return getComponentFactory().createScrollPane(getConversationTable());
	}

	private JTable getConversationTable() {
		if (conversationTable == null) {
			conversationTable = getComponentFactory().createTable(
					getTableModel());
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
					return getSelectedConversation() != null ?
					 createConversationPopupContextMenu() :
					 null;
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

	public void onApplicationEvent(ApplicationEvent applicationEvent) {
		logger.info(applicationEvent);
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
                "conversationCommandGroup",
                new Object[] {"propertiesCommand"});
        return group.createPopupMenu();
    }

	private static class ConversationTableFormat implements TableFormat {

		private String[] columnNames = new String[] { "Id", "Date", "Method",
				"Url", "Status" };

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
			case 0:
				return summary.getId();
			case 1:
				return summary.getDate();
			case 2:
				return summary.getRequestMethod();
			case 3:
				return summary.getRequestUrl();
			case 4:
				return summary.getResponseStatus() + " "
						+ summary.getResponseMessage();
			}
			return "Error";
		}

	}

	private class ShowConversationExecutor extends
			AbstractActionCommandExecutor {

		public void execute() {
			final Conversation conversation = getSelectedConversation();
			final ConversationForm conversationForm = new ConversationForm(
					FormModelHelper.createFormModel(conversation));
			final FormBackedDialogPage dialogPage = new FormBackedDialogPage(
					conversationForm);

			TitledPageApplicationDialog dialog = new TitledPageApplicationDialog(
					dialogPage, getWindowControl()) {
				protected void onAboutToShow() {
					// conversationForm.requestFocusInWindow();
					setEnabled(dialogPage.isPageComplete());
				}

				protected boolean onFinish() {
					conversationForm.commit();
					return true;
				}
			};
			dialog.setModal(false);
			dialog.setPreferredSize(new Dimension(1024, 768));
			dialog.showDialog();
		}
	}

}
