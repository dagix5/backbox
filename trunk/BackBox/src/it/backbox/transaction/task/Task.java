package it.backbox.transaction.task;

import it.backbox.utility.Utility;

public abstract class Task {
	private String id;
	private String description;
	private long weight = 1;
	protected boolean stop = false;

	public Task() {
		setId(Utility.genID());
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public long getWeight() {
		return weight;
	}

	public void setWeight(long weight) {
		this.weight = weight;
	}
	
	public abstract void run() throws Exception;
	
	public void stop() {
		stop = true;
	}
}