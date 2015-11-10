package it.backbox.transaction;

import java.util.ArrayList;

import it.backbox.utility.Utility;

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
		this(Utility.genID());
	}

	public Transaction(String id) {
		setId(id);
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Transaction))
			return false;
		Transaction other = (Transaction) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
