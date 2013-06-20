package it.backbox.bean;

public class Chunk {

	private String chunkname;
	private String chunkhash;
	private String boxid;
	private byte[] content;
	private long size;

	public String getChunkname() {
		return chunkname;
	}

	public void setChunkname(String chunkname) {
		this.chunkname = chunkname;
	}

	public String getBoxid() {
		return boxid;
	}

	public void setBoxid(String boxid) {
		this.boxid = boxid;
	}

	public String getChunkhash() {
		return chunkhash;
	}

	public void setChunkhash(String chunkhash) {
		this.chunkhash = chunkhash;
	}

	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

}
