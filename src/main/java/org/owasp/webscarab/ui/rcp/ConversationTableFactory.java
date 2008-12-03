/**
 *
 */
package org.owasp.webscarab.ui.rcp;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.jdesktop.swingx.JXTable;
import org.owasp.webscarab.domain.Annotation;
import org.owasp.webscarab.domain.Conversation;
import org.owasp.webscarab.services.ConversationService;
import org.owasp.webscarab.util.UrlUtils;
import org.owasp.webscarab.util.swing.renderers.DateRenderer;
import org.owasp.webscarab.util.swing.renderers.TableColorProvider;
import org.owasp.webscarab.util.swing.renderers.TableColorRenderer;
import org.springframework.richclient.application.support.ApplicationServicesAccessor;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.gui.WritableTableFormat;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;

/**
 * @author rdawes
 *
 */
public class ConversationTableFactory extends ApplicationServicesAccessor {

	private CompoundConversationTableFormat tableFormat = new CompoundConversationTableFormat();
	private ConversationService conversationService;

	public ConversationTableFactory() {
		tableFormat.addColumn(new ObjectAttribute<Conversation>() {
			public Class<?> getAttributeClass() {
				return Integer.class;
			}
			public String getAttributeId() {
				return "conversation.id";
			}
			public Object getValue(Conversation conversation) {
				return conversation.getId();
			}
		});
		tableFormat.addColumn(new ObjectAttribute<Conversation>() {
			public Class<?> getAttributeClass() {
				return Date.class;
			}
			public String getAttributeId() {
				return "conversation.date";
			}
			public Object getValue(Conversation conversation) {
				return conversation.getDate();
			}
		});
		tableFormat.addColumn(new ObjectAttribute<Conversation>() {
			public Class<?> getAttributeClass() {
				return String.class;
			}
			public String getAttributeId() {
				return "conversation.requestMethod";
			}
			public Object getValue(Conversation conversation) {
				return conversation.getRequestMethod();
			}
		});
		tableFormat.addColumn(new ObjectAttribute<Conversation>() {
			public Class<?> getAttributeClass() {
				return String.class;
			}
			public String getAttributeId() {
				return "conversation.requestUri.host";
			}
			public Object getValue(Conversation conversation) {
				return UrlUtils.getSchemeHostPort(conversation.getRequestUri());
			}
		});
		tableFormat.addColumn(new ObjectAttribute<Conversation>() {
			public Class<?> getAttributeClass() {
				return String.class;
			}
			public String getAttributeId() {
				return "conversation.requestUri.path";
			}
			public Object getValue(Conversation conversation) {
				return conversation.getRequestUri().getPath();
			}
		});
		tableFormat.addColumn(new ObjectAttribute<Conversation>() {
			public Class<?> getAttributeClass() {
				return String.class;
			}
			public String getAttributeId() {
				return "conversation.requestUri.parameters";
			}
			public Object getValue(Conversation conversation) {
				return conversation.getRequestUri().getQuery();
			}
		});
		tableFormat.addColumn(new ObjectAttribute<Conversation>() {
			public Class<?> getAttributeClass() {
				return String.class;
			}
			public String getAttributeId() {
				return "conversation.responseStatus";
			}
			public Object getValue(Conversation conversation) {
				return conversation.getResponseStatus() + " " + conversation.getResponseMessage();
			}
		});
		tableFormat.addColumn(new ObjectAttribute<Conversation>() {
			public Class<?> getAttributeClass() {
				return Integer.class;
			}
			public String getAttributeId() {
				return "conversation.requestContentSize";
			}
			public Object getValue(Conversation conversation) {
				int size = conversation.getRequestContentSize();
				if (size == 0) return null;
				return new Integer(size);
			}
		});
		tableFormat.addColumn(new ObjectAttribute<Conversation>() {
			public Class<?> getAttributeClass() {
				return Integer.class;
			}
			public String getAttributeId() {
				return "conversation.responseContentSize";
			}
			public Object getValue(Conversation conversation) {
				int size = conversation.getResponseContentSize();
				if (size == 0) return null;
				return new Integer(size);
			}
		});
		tableFormat.addColumn(new ObjectAttribute<Conversation>() {
			public Class<?> getAttributeClass() {
				return String.class;
			}
			public String getAttributeId() {
				return "annotation.annotation";
			}
			public Object getValue(Conversation conversation) {
				Annotation annotation = getConversationService().getAnnotation(conversation.getId());
				if (annotation != null)
					return annotation.getAnnotation();
				return null;
			}
		});
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

	private void registerRenderersForTable(JTable table, TableColorProvider colorProvider) {
		Set<Class<?>> columnClasses = new HashSet<Class<?>>();
		for (int i=0; i<table.getColumnCount(); i++) {
			columnClasses.add(table.getColumnClass(i));
		}
		for (Class<?> klass: columnClasses) {
			TableCellRenderer delegate = table.getDefaultRenderer(klass);
			TableCellRenderer renderer = new TableColorRenderer(delegate, colorProvider);
			table.setDefaultRenderer(klass, renderer);
		}
	}

	public JTable getConversationTable(SortedList<Conversation> conversationList) {
		JTable table = getComponentFactory().createTable();
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		if (table instanceof JXTable) {
			JXTable jx = (JXTable) table;
			jx.setColumnControlVisible(true);
			jx.setSortable(false);
		}
		table.setModel(new EventTableModel<Conversation>(conversationList, tableFormat));
		new TableComparatorChooser<Conversation>(table, conversationList, true);
		table.setDefaultRenderer(Date.class, new DateRenderer());
		TableColorProvider colorProvider = new AnnotationColorProvider(conversationList);
		registerRenderersForTable(table, colorProvider);
		return table;
	}

	private class CompoundConversationTableFormat implements WritableTableFormat<Conversation>, AdvancedTableFormat<Conversation> {

		private List<ObjectAttribute<Conversation>> columns = new ArrayList<ObjectAttribute<Conversation>>();

		public void addColumn(ObjectAttribute<Conversation> column) {
			columns.add(column);
		}

		public Class<?> getColumnClass(int column) {
			return columns.get(column).getAttributeClass();
		}

		public Comparator<?> getColumnComparator(int column) {
			return columns.get(column).getComparator();
		}

		public int getColumnCount() {
			return columns.size();
		}

		public String getColumnName(int column) {
			String name = getMessage(columns.get(column).getAttributeId());
			if (name != null) return name;
			return columns.get(column).getAttributeId();
		}

		public Object getColumnValue(Conversation conversation, int column) {
			return columns.get(column).getValue(conversation);
		}

		public boolean isEditable(Conversation conversation, int column) {
			return columns.get(column).isAttributeEditable();
		}

		public Conversation setColumnValue(Conversation conversation, Object value, int column) {
			return columns.get(column).setAttribute(conversation, value);
		}

	}

	private class AnnotationColorProvider implements TableColorProvider {

		private EventList<Conversation> conversations;

		public AnnotationColorProvider(EventList<Conversation> conversations) {
			this.conversations = conversations;
		}

		public Color getBackGroundColor(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Conversation conversation = conversations.get(row);
			if (getConversationService().getAnnotation(conversation.getId()) != null) {
				return Color.PINK.darker();
			} else {
				return table.getBackground();
			}
		}

		public Color getForegroundColor(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			return table.getForeground();
		}

	}
}
