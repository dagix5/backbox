package it.backbox.transaction.task;

import java.util.ArrayList;

public class Transaction {

	public static final short ESITO_OK = 1;
	public static final short ESITO_KO = -1;
	public static final short NO_ESITO = 0;

	private ArrayList<Task> tasks;
	private short resultCode;
	private String resultDescription;
	private String id;

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
