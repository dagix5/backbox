package it.backbox.gui.bean;

public class Filename {

	public static final int DIRECTORY_TYPE = 0;
	public static final int FILE_TYPE = 1;
	
	private String name;
	private int type;
	
	public Filename(String name, int type) {
		super();
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String toString() {
		return name;
	}

	public boolean equals(Filename tf) {
		return (type == tf.type) && name.equals(tf.name);
	}
}
