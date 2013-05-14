package it.backbox.transaction.task;

import it.backbox.utility.Utility;

public abstract class Task {
	private String id;
	private String description;
	private long weight = 1;
	private boolean countWeight = true;
	protected boolean stop = false;
	private long totalTime = 0;

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

	public boolean isCountWeight() {
		return countWeight;
	}

	public void setCountWeight(boolean countWeight) {
		this.countWeight = countWeight;
	}
	
	public abstract void run() throws Exception;
	
	public void stop() {
		stop = true;
	}

	public long getTotalTime() {
		return totalTime;
	}

	public void setTotalTime(long totalTime) {
		this.totalTime = totalTime;
	}

}
