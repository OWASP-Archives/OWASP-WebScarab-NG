/**
 * 
 */
package org.owasp.webscarab.ui.rcp.forms;

import java.awt.Event;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.springframework.binding.form.FormModel;

/**
 * @author rdawes
 * 
 */
public class HexForm extends AbstractContentForm {

	private static String FORM_ID = "hexForm";

	private HexTableModel tableModel;

	private HexTable table;

	private int length = 0;
	
	public HexForm(FormModel model, String headerPropertyName,
			String contentPropertyName) {
		super(model, FORM_ID, headerPropertyName, contentPropertyName);
	}

	@Override
	protected JComponent createContentFormControl() {
		tableModel = new HexTableModel(16);
		table = new HexTable(tableModel);
		DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
		dtcr.putClientProperty("html.disable", Boolean.TRUE);
		table.setDefaultRenderer(Object.class, dtcr);
		return new JScrollPane(table);
	}

	protected void updateContentFormControl() {
	    byte[] content = getContent();
	    if (content != null) {
	        length = content.length;
	    } else {
	        length = 0;
	    }
		tableModel.fireTableDataChanged();
	}

	protected void clearContentFormControl() {
        byte[] content = getContent();
        if (content != null) {
            length = content.length;
        } else {
            length = 0;
        }
        tableModel.fireTableDataChanged();
	}

	public boolean canHandle(String contentType) {
		// hex can handle anything
		return true;
	}

	private class HexTable extends JTable {

		private static final long serialVersionUID = 3392044528006906279L;

		public HexTable(HexTableModel tableModel) {
			super(tableModel);
			int columns = tableModel.getColumnCount() - 2;
			setAutoResizeMode(AUTO_RESIZE_OFF);
			setFont(new Font("Monospaced", Font.PLAIN, 12));
			getTableHeader().setReorderingAllowed(false);
			TableColumnModel colModel = getColumnModel();
			// FIXME : use FontMetrics to get the real width of the font
			for (int i = 0; i < columns; i++) {
				colModel.getColumn(i + 1).setPreferredWidth(2 * 9);
				colModel.getColumn(i + 1).setResizable(false);
			}
			colModel.getColumn(0).setPreferredWidth(8 * 9);
			colModel.getColumn(columns + 1).setPreferredWidth(columns * 9);
			InputMap im = getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
			im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK),
					"Save");
			im.put(KeyStroke.getKeyStroke(KeyEvent.VK_L, Event.CTRL_MASK),
					"Load");
			getActionMap().put("Save", new SaveAction());
			getActionMap().put("Load", new LoadAction());

		}

	}

	private class HexTableModel extends AbstractTableModel {

		private static final long serialVersionUID = -3782965899741537329L;

		private int columns;

		public HexTableModel(int columns) {
			this.columns = columns;
		}

		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) {
				return "Position";
			} else if (columnIndex - 1 < columns) {
				return Integer.toHexString(columnIndex - 1).toUpperCase();
			} else {
				return "String";
			}
		}

		public int getColumnCount() {
			return columns + 2;
		}

		public int getRowCount() {
			if (length == 0) {
				return 0;
			}
			if (length % columns == 0) {
				return (length / columns);
			} else {
				return (length / columns) + 1;
			}
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			byte[] data = getContent();
			if (data == null)
			    return null;
			if (columnIndex == 0) {
				return pad(Integer.toHexString(rowIndex * columns)
						.toUpperCase(), '0', 8);
			} else if (columnIndex - 1 < columns) {
				int position = rowIndex * columns + columnIndex - 1;
				if (position < data.length) {
					int i = data[position];
					if (i < 0) {
						i = i + 256;
					}
					return pad(Integer.toString(i, 16).toUpperCase(), '0', 2);
				} else {
					return "";
				}
			} else {
				int start = rowIndex * columns;
				StringBuffer buff = new StringBuffer();
				for (int i = 0; i < columns; i++) {
					int pos = start + i;
					if (pos >= data.length) {
						return buff.toString();
					}
					if (data[pos] < 32 || data[pos] > 126) {
						buff.append(".");
					} else {
						buff.append((char) data[pos]);
					}
				}
				return buff.toString();
			}
		}

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if (isReadOnly())
				return false;
            if (columnIndex == 0 || columnIndex > columns) {
                return false;
            }
			int position = rowIndex * columns + columnIndex - 1;
			if (position < length) {
				return true;
			}
			return false;
		}

		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			byte[] data = getContent();
			int position = rowIndex * columns + columnIndex - 1;
			if (position >= data.length) {
				return;
			}
			if (aValue instanceof String) {
				try {
					String s = (String) aValue;
					byte[] newData = new byte[data.length];
					System.arraycopy(data, 0, newData, 0, data.length);
					newData[position] = new Integer(Integer.parseInt(s.trim(),
							16)).byteValue();
					fireTableCellUpdated(rowIndex, columns + 1);
					setContent(newData);
				} catch (NumberFormatException nfe) {
					logger.error("Number format error : " + nfe);
				}
			} else {
			    logger.error("Value is a " + aValue.getClass().getName());
			}
		}

		private String pad(String initial, char padchar, int length) {
			if (initial.length() >= length) {
				return initial;
			}
			StringBuffer buff = new StringBuffer(length);
			for (int i = 0; i < length - initial.length(); i++) {
				buff.append(padchar);
			}
			buff.append(initial);
			return buff.toString();
		}

	}

	private class SaveAction extends AbstractAction {
		private static final long serialVersionUID = -872604689834638795L;

		public void actionPerformed(ActionEvent evt) {
			JFileChooser jfc = new JFileChooser();
			jfc.setDialogTitle("Select a file to write the message content to");
			int returnVal = jfc.showOpenDialog(table);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				try {
					FileOutputStream fos = new FileOutputStream(jfc
							.getSelectedFile());
					fos.write(getContent());
					fos.close();
				} catch (IOException ioe) {
					JOptionPane.showMessageDialog(table, "Error writing file: "
							+ ioe.getMessage(), "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}

	private class LoadAction extends AbstractAction {
		private static final long serialVersionUID = 7286272198340993109L;

		public void actionPerformed(ActionEvent evt) {
			if (isReadOnly()) {
				Toolkit.getDefaultToolkit().beep();
				return;
			}
			JFileChooser jfc = new JFileChooser();
			jfc
					.setDialogTitle("Select a file to read the message content from");
			int returnVal = jfc.showOpenDialog(table);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				try {
					FileInputStream fis = new FileInputStream(jfc
							.getSelectedFile());
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					byte[] buff = new byte[2048];
					int got;
					while ((got = fis.read(buff)) > 0) {
						baos.write(buff, 0, got);
					}
					fis.close();
					baos.close();
					setContent(baos.toByteArray());
				} catch (IOException ioe) {
					JOptionPane.showMessageDialog(table, "Error writing file: "
							+ ioe.getMessage(), "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}
}
