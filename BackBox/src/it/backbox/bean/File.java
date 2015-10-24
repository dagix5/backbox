package it.backbox.bean;

import java.util.ArrayList;
import java.util.Date;

public class File {
	public static final String EXT = ".bup";

	private String hash;
	private String filename;
	private String folder;
	private Date timestamp;
	private long size;
	private short encrypted;
	private short compressed;
	private short splitted;
	private ArrayList<Chunk> chunks;

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public ArrayList<Chunk> getChunks() {
		if (chunks == null)
			chunks = new ArrayList<Chunk>();
		return chunks;
	}

	public void setChunks(ArrayList<Chunk> chunknames) {
		this.chunks = chunknames;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String getFolder() {
		return folder;
	}

	public void setFolder(String folder) {
		this.folder = folder;
	}

	public short getEncrypted() {
		return encrypted;
	}

	public void setEncrypted(short encrypted) {
		this.encrypted = encrypted;
	}

	public short getCompressed() {
		return compressed;
	}

	public void setCompressed(short compressed) {
		this.compressed = compressed;
	}

	public short getSplitted() {
		return splitted;
	}

	public void setSplitted(short splitted) {
		this.splitted = splitted;
	}

}
