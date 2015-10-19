package it.backbox.transaction.task;

import it.backbox.utility.Utility;

import java.util.ArrayList;

public class Transaction {

	public static final class Result {
		public static final short OK = 1;
		public static final short KO = -1;
		public static final short NO_RESULT = 0;
		public static final short ROLLBACK = -2;
	}
	
	private ArrayList<Task> tasks;
	private short resultCode;
	private String resultDescription;
	private String id;
	
	public Transaction() {
		setId(Utility.genID());
	}

	public void addTask(Task task, int position) {
		getTasks().add(position, task);
	}

	public void addTask(Task task) {
		getTasks().add(task);
	}

	public ArrayList<Task> getTasks() {
		if (tasks == null)
			tasks = new ArrayList<>();
		return tasks;
	}

	public void setTasks(ArrayList<Task> tasks) {
		this.tasks = tasks;
	}

	public short getResultCode() {
		return resultCode;
	}

	public void setResultCode(short resultCode) {
		this.resultCode = resultCode;
	}

	public String getResultDescription() {
		return resultDescription;
	}

	public void setResultDescription(String resultDescription) {
		this.resultDescription = resultDescription;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
