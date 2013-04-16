package it.backbox.transaction.task;

public abstract class Task {
	private int id;
	private String description;
	private long weight = 1;
	protected boolean stop = false;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
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
