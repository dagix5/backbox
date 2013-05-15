package it.backbox.gui.bean;

import it.backbox.transaction.task.Task;
import it.backbox.transaction.task.Transaction;

public class TableTask {

	private Task task;
	private Transaction transaction;
	private int tableIndex;

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public Transaction getTransaction() {
		return transaction;
	}

	public void setTransaction(Transaction transaction) {
		this.transaction = transaction;
	}

	public int getTableIndex() {
		return tableIndex;
	}

	public void setTableIndex(int tableIndex) {
		this.tableIndex = tableIndex;
	}

}
