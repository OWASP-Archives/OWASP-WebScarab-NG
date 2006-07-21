/**
 * 
 */
package org.owasp.webscarab.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.swing.JTable;

import org.jdesktop.swingx.JXTable;
import org.owasp.webscarab.domain.Annotation;
import org.owasp.webscarab.domain.ConversationSummary;
import org.owasp.webscarab.services.ConversationService;
import org.owasp.webscarab.util.UrlUtils;
import org.owasp.webscarab.util.swing.renderers.DateRenderer;
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
		tableFormat.addColumn(new ObjectAttribute<ConversationSummary>() {
			public String getAttributeId() {
				return "conversationSummary.id";
			}
			public Object getValue(ConversationSummary summary) {
				return summary.getId();
			}
		});
		tableFormat.addColumn(new ObjectAttribute<ConversationSummary>() {
			public Class getAttributeClass() {
				return Date.class;
			}
			public String getAttributeId() {
				return "conversationSummary.date";
			}
			public Object getValue(ConversationSummary summary) {
				return summary.getDate();
			}
		});
		tableFormat.addColumn(new ObjectAttribute<ConversationSummary>() {
			public String getAttributeId() {
				return "conversationSummary.requestMethod";
			}
			public Object getValue(ConversationSummary summary) {
				return summary.getRequestMethod();
			}
		});
		tableFormat.addColumn(new ObjectAttribute<ConversationSummary>() {
			public String getAttributeId() {
				return "conversationSummary.requestUri.host";
			}
			public Object getValue(ConversationSummary summary) {
				return UrlUtils.getSchemeHostPort(summary.getRequestUri());
			}
		});
		tableFormat.addColumn(new ObjectAttribute<ConversationSummary>() {
			public String getAttributeId() {
				return "conversationSummary.requestUri.path";
			}
			public Object getValue(ConversationSummary summary) {
				return summary.getRequestUri().getPath();
			}
		});
		tableFormat.addColumn(new ObjectAttribute<ConversationSummary>() {
			public String getAttributeId() {
				return "conversationSummary.requestUri.parameters";
			}
			public Object getValue(ConversationSummary summary) {
				return summary.getRequestUri().getQuery();
			}
		});
		tableFormat.addColumn(new ObjectAttribute<ConversationSummary>() {
			public String getAttributeId() {
				return "conversationSummary.responseStatus";
			}
			public Object getValue(ConversationSummary summary) {
				return summary.getResponseStatus() + " " + summary.getResponseMessage();
			}
		});
		tableFormat.addColumn(new ObjectAttribute<ConversationSummary>() {
			public String getAttributeId() {
				return "conversationSummary.requestContentSize";
			}
			public Object getValue(ConversationSummary summary) {
				return summary.getRequestContentSize();
			}
		});
		tableFormat.addColumn(new ObjectAttribute<ConversationSummary>() {
			public String getAttributeId() {
				return "conversationSummary.responseContentSize";
			}
			public Object getValue(ConversationSummary summary) {
				return summary.getResponseContentSize();
			}
		});
		tableFormat.addColumn(new ObjectAttribute<ConversationSummary>() {
			public String getAttributeId() {
				return "annotation.annotation";
			}
			public Object getValue(ConversationSummary summary) {
				Annotation annotation = getConversationService().getAnnotation(summary.getId());
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

	public JTable getConversationTable(EventList<ConversationSummary> conversationSummaryList) {
		JTable table = getComponentFactory().createTable();
		table.setModel(new EventTableModel<ConversationSummary>(conversationSummaryList, tableFormat));
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		if (table instanceof JXTable) {
			JXTable jx = (JXTable) table;
			jx.setColumnControlVisible(true);
			jx.setSortable(false);
		}
//		if (conversationSummaryList instanceof SortedList) {
//			SortedList<ConversationSummary> sorted = (SortedList<ConversationSummary>) conversationSummaryList;
//			new TableComparatorChooser<ConversationSummary>(table, sorted, true);
//		}
		table.setDefaultRenderer(Date.class, new DateRenderer());
		return table;
	}
	
	private class CompoundConversationTableFormat implements WritableTableFormat<ConversationSummary>, AdvancedTableFormat<ConversationSummary> {

		private List<ObjectAttribute<ConversationSummary>> columns = new ArrayList<ObjectAttribute<ConversationSummary>>();
		
		public void addColumn(ObjectAttribute<ConversationSummary> column) {
			columns.add(column);
		}
		
		public Class getColumnClass(int column) {
			return columns.get(column).getAttributeClass();
		}

		public Comparator getColumnComparator(int column) {
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

		public Object getColumnValue(ConversationSummary summary, int column) {
			return columns.get(column).getValue((ConversationSummary) summary);
		}

		public boolean isEditable(ConversationSummary summary, int column) {
			return columns.get(column).isAttributeEditable();
		}

		public ConversationSummary setColumnValue(ConversationSummary summary, Object value, int column) {
			return columns.get(column).setAttribute(summary, value);
		}
		
	}
}
