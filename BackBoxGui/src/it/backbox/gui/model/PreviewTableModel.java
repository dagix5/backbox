package it.backbox.gui.model;

import javax.swing.table.DefaultTableModel;

import it.backbox.gui.bean.Size;
import it.backbox.transaction.task.Task;
import it.backbox.transaction.task.Transaction;

public class PreviewTableModel extends DefaultTableModel {

	private static final long serialVersionUID = 1L;
	
	public static final int RESULT_COLUMN_INDEX = 3;
	public static final int TRANSACTION_COLUMN_INDEX = 4;
	public static final int TASK_COLUMN_INDEX = 5;

	public PreviewTableModel() {
		super(new Object[][] {}, new String[] { "Filename", "Size", "Operation", "Result", "Transaction", "Task" });
	}

	private Class[] columnTypes = new Class[] { String.class, Size.class, String.class, String.class, Transaction.class,
			Task.class };

	public Class getColumnClass(int columnIndex) {
		return columnTypes[columnIndex];
	}

	public boolean isCellEditable(int row, int column) {
		return false;
	}
}
