/**
 * 
 */
package org.owasp.webscarab.ui.forms;

import java.awt.Event;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
import javax.swing.table.TableColumnModel;

import org.springframework.binding.form.FormModel;
import org.springframework.binding.value.ValueModel;
import org.springframework.richclient.form.AbstractForm;

/**
 * @author rdawes
 * 
 */
public class HexForm extends AbstractForm implements ContentForm {

	private static String FORM_ID = "hexForm";

	private ValueModel vm;
	
	private JScrollPane scrollPane = null;
	
	boolean readOnly;
	
	public HexForm(FormModel model, String propertyName) {
		super(model, FORM_ID);
		vm = getValueModel(propertyName);
		readOnly = getFormModel().getFieldMetadata(propertyName).isReadOnly();
	}

	@Override
	protected JComponent createFormControl() {
		if (scrollPane == null) {
			HexTable table = new HexTable(vm, !readOnly);
			scrollPane = new JScrollPane(table);
		}
		return scrollPane;
	}

	public boolean canHandle(String contentType) {
		// hex can handle anything
		return true;
	}
	
	private class HexTable extends JTable {

		private static final long serialVersionUID = 3392044528006906279L;

		public HexTable(ValueModel vm) {
			this(vm, false);
		}

		public HexTable(ValueModel vm, boolean editable) {
			this(vm, editable, 16);
		}

		public HexTable(final ValueModel vm, boolean editable, int columns) {
			super(new HexTableModel(vm, editable, columns));
			setAutoResizeMode(AUTO_RESIZE_OFF);
			setFont(new Font("Monospaced", Font.PLAIN, 12));
			getTableHeader().setReorderingAllowed(false);
			TableColumnModel colModel = getColumnModel();
			// FIXME : use FontMetrics to get the real width of the font
			for (int i = 0; i < columns + 2; i++) {
				colModel.getColumn(i).setPreferredWidth(2 * 9);
				colModel.getColumn(i).setResizable(false);
			}
			colModel.getColumn(0).setPreferredWidth(8 * 9);
			colModel.getColumn(columns + 1).setPreferredWidth(columns * 9);
			InputMap im = getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
			im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK),
					"Save");
			im.put(KeyStroke.getKeyStroke(KeyEvent.VK_L, Event.CTRL_MASK),
					"Load");
			getActionMap().put("Save", new AbstractAction() {
				private static final long serialVersionUID = -872604689834638795L;

				public void actionPerformed(ActionEvent evt) {
					JFileChooser jfc = new JFileChooser();
					jfc
							.setDialogTitle("Select a file to write the message content to");
					int returnVal = jfc.showOpenDialog(HexTable.this);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						try {
							FileOutputStream fos = new FileOutputStream(jfc
									.getSelectedFile());
							fos.write((byte[]) vm.getValue());
							fos.close();
						} catch (IOException ioe) {
							JOptionPane.showMessageDialog(HexTable.this,
									"Error writing file: " + ioe.getMessage(),
									"Error", JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			});
			if (editable) {
				getActionMap().put("Load", new AbstractAction() {
					private static final long serialVersionUID = 7286272198340993109L;

					public void actionPerformed(ActionEvent evt) {
						JFileChooser jfc = new JFileChooser();
						jfc
								.setDialogTitle("Select a file to read the message content from");
						int returnVal = jfc.showOpenDialog(HexTable.this);
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
								vm.setValue(baos.toByteArray());
							} catch (IOException ioe) {
								JOptionPane.showMessageDialog(HexTable.this,
										"Error writing file: "
												+ ioe.getMessage(), "Error",
										JOptionPane.ERROR_MESSAGE);
							}
						}
					}
				});
			}

		}

	}

	private static class HexTableModel extends AbstractTableModel implements
			PropertyChangeListener {

		private static final long serialVersionUID = -3782965899741537329L;

		private ValueModel vm;

		private int columns;

		private boolean editable;

		public HexTableModel(ValueModel vm, boolean editable, int columns) {
			this.vm = vm;
			vm.addValueChangeListener(this);
			this.columns = columns;
			this.editable = editable;
		}

		public HexTableModel(ValueModel vm, boolean editable) {
			this(vm, editable, 8);
		}

		public HexTableModel(ValueModel vm) {
			this(vm, false);
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
			byte[] data = (byte[]) vm.getValue();
			if (data == null || data.length == 0) {
				return 0;
			}
			if (data.length % columns == 0) {
				return (int) (data.length / columns);
			} else {
				return (int) (data.length / columns) + 1;
			}
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			byte[] data = (byte[]) vm.getValue();
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
			if (!editable)
				return false;
			byte[] data = (byte[]) vm.getValue();
			if (columnIndex == 0 || columnIndex > columns) {
				return false;
			}
			int position = rowIndex * columns + columnIndex - 1;
			if (position < data.length) {
				return editable;
			}
			return false;
		}

		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			byte[] data = (byte[]) vm.getValue();
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
					System.out.println("Calling setValue");
					vm.setValueSilently(newData, this);
				} catch (NumberFormatException nfe) {
					System.out.println("Number format error : " + nfe);
				}
			} else {
				System.out.println("Value is a " + aValue.getClass().getName());
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

		public void propertyChange(PropertyChangeEvent evt) {
			fireTableDataChanged();
		}
	}

}
