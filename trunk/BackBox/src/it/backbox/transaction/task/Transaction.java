package it.backbox.transaction.task;

import java.util.ArrayList;

public class Transaction {

	public static final short ESITO_OK = 1;
	public static final short ESITO_KO = -1;

	private ArrayList<Task> tasks;
	private short resultCode;
	private String resultDescription;
	private String description;

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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
