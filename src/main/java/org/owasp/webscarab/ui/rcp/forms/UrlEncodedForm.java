/**
 * 
 */
package org.owasp.webscarab.ui.rcp.forms;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.owasp.webscarab.domain.NamedValue;
import org.springframework.binding.form.FormModel;
import org.springframework.binding.value.ValueModel;
import org.springframework.richclient.command.AbstractCommand;
import org.springframework.richclient.command.ActionCommand;
import org.springframework.richclient.command.config.CommandConfigurer;
import org.springframework.richclient.factory.ComponentFactory;
import org.springframework.richclient.list.ListMultipleSelectionGuard;
import org.springframework.richclient.list.ListSelectionValueModelAdapter;

/**
 * @author rdawes
 * 
 */
public class UrlEncodedForm extends AbstractContentForm {

	private static String FORM_ID = "urlEncodedForm";

	private List<NamedValue> values = new ArrayList<NamedValue>();
	
	private JTable table;
	
	private NamedValueTableModel model;
	
	public UrlEncodedForm(FormModel model, String headerPropertyName,
			String contentPropertyName) {
		super(model, FORM_ID, headerPropertyName, contentPropertyName);
	}

	@Override
	protected JComponent createContentFormControl() {
	    model = new NamedValueTableModel();
	    ComponentFactory cf = getComponentFactory();
	    JPanel panel = cf.createPanel(new BorderLayout());
	    table = cf.createTable(model);
	    panel.add(cf.createScrollPane(table), BorderLayout.CENTER);
	    if (! isReadOnly()) {
	        JPanel buttonPanel = cf.createPanel(new GridLayout(2,1));
	        CommandConfigurer cc = getCommandConfigurer();
	        AbstractCommand addRowCommand = cc.configure(new AddRowCommand());
            AbstractButton addButton = addRowCommand.createButton();
            AbstractCommand deleteRowCommand = cc.configure(new DeleteRowCommand());
            ValueModel selectionHolder = new ListSelectionValueModelAdapter(table
                    .getSelectionModel());
            new ListMultipleSelectionGuard(selectionHolder, deleteRowCommand);
            AbstractButton deleteButton = deleteRowCommand.createButton();
            buttonPanel.add(addButton);
            buttonPanel.add(deleteButton);
            panel.add(buttonPanel, BorderLayout.EAST);
	    }
		return panel;
	}

	protected void updateContentFormControl() {
        this.values.clear();
	    try {
    	    String content = getContentAsString();
    	    if (content != null) {
        	    NamedValue[] values = NamedValue.parse(content, "&", "=");
        	    this.values.addAll(Arrays.asList(values));
    	    }
    	    model.fireTableDataChanged();
	    } catch (UnsupportedEncodingException uee) {
	        // do nothing? Ideally we should show some kind of error condition?
	    }
	}

	protected void clearContentFormControl() {
	    this.values.clear();
	}

	private void setContent() throws UnsupportedEncodingException {
        setContent(NamedValue.join(values.toArray(new NamedValue[0]), "&", "="));
	}
	
	public boolean canHandle(String contentType) {
		return "application/x-www-form-urlencoded".equals(contentType);
	}
	
	private class NamedValueTableModel extends AbstractTableModel {

        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#getColumnCount()
         */
        public int getColumnCount() {
            return 2;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#getRowCount()
         */
        public int getRowCount() {
            return values.size();
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            NamedValue value = values.get(rowIndex);
            if (columnIndex == 0)
                return value.getName();
            if (value.getValue() == null)
            	return null;
            try {
                return URLDecoder.decode(value.getValue(), "ISO-8859-1");
            } catch (UnsupportedEncodingException uee) {
                // this should never happen
                return null;
            }
        }

        /* (non-Javadoc)
         * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
         */
        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.AbstractTableModel#getColumnName(int)
         */
        @Override
        public String getColumnName(int column) {
            return column == 0 ? "Name" : "Value";
        }

        /* (non-Javadoc)
         * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
         */
        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return ! isReadOnly();
        }

        /* (non-Javadoc)
         * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
         */
        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            NamedValue old = values.get(rowIndex);
            if (columnIndex == 0) {
                values.set(rowIndex, new NamedValue((String) value, old.getValue()));
            } else {
                try {
                    String v = (String) value;
                    v = URLEncoder.encode(v, "ISO-8859-1");
                    values.set(rowIndex, new NamedValue(old.getName(), v));
                }  catch (UnsupportedEncodingException uee) {} // should never happen
            }
            try {
                setContent();
                fireTableCellUpdated(rowIndex, columnIndex);
            } catch (UnsupportedEncodingException uee) {
                values.set(rowIndex, old);
            }
        }
	    
	}
	
	private class AddRowCommand extends ActionCommand {

	    public AddRowCommand() {
	        super("addRow", "addRow");
	    }
	    
        /* (non-Javadoc)
         * @see org.springframework.richclient.command.ActionCommand#doExecuteCommand()
         */
        @Override
        protected void doExecuteCommand() {
            NamedValue value = new NamedValue("name", "value");
            int row = table.getSelectedRow();
            if (row == -1) {
                values.add(value);
                model.fireTableRowsInserted(values.size() - 1, values.size() - 1);
            } else {
                values.add(row + 1, value);
                model.fireTableRowsInserted(row + 1, row + 1);
            }
            try {
                setContent();
            } catch (UnsupportedEncodingException uee) {
                // TODO: should do something, I guess
            }
        }
	    
	}

    private class DeleteRowCommand extends ActionCommand {

        public DeleteRowCommand() {
            super("deleteRow");
        }
        
        /* (non-Javadoc)
         * @see org.springframework.richclient.command.ActionCommand#doExecuteCommand()
         */
        @Override
        protected void doExecuteCommand() {
            int[] rows = table.getSelectedRows();
            for (int i = rows.length-1; i>=0; i--) {
                values.remove(rows[i]);
                model.fireTableRowsDeleted(rows[i], rows[i]);
            }
            try {
                setContent();
            } catch (UnsupportedEncodingException uee) {
                // shouldn't happen
            }
        }
        
    }

}
