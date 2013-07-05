package it.backbox.gui.bean;

import it.backbox.utility.Utility;

public class Size {

	private String hsize;
	private long size;
	
	public Size(long size) {
		this.size = size;
		if (size >= 0)
			this.hsize = Utility.humanReadableByteCount(size, true);
		else
			this.hsize = " - ";
	}

	@Override
	public String toString() {
		return hsize;
	}

	public String getHsize() {
		return hsize;
	}

	public void setHsize(String hsize) {
		this.hsize = hsize;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

}
