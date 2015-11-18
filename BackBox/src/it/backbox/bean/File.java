package it.backbox.bean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class File {
	public static final String EXT = ".bup";

	private String hash;
	private String filename;
	private String folderAlias;
	private Date timestamp;
	private long size;
	private short encrypted;
	private short compressed;
	private short splitted;
	private List<Chunk> chunks;

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

	public List<Chunk> getChunks() {
		if (chunks == null)
			chunks = new ArrayList<Chunk>();
		return chunks;
	}

	public void setChunks(List<Chunk> chunks) {
		this.chunks = chunks;
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

	public String getFolderAlias() {
		return folderAlias;
	}

	public void setFolderAlias(String folderAlias) {
		this.folderAlias = folderAlias;
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

	@Override
	public String toString() {
		return "File [hash=" + hash + ", filename=" + filename + ", folderAlias=" + folderAlias + "]";
	}

}
