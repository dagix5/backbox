package it.backbox.bean;

import com.google.api.client.util.Key;

public class Folder {

	@Key
	private String path;
	@Key
	private String id;
	@Key
	private String alias;
	
	public Folder() {
		
	}
	
	public Folder(String path, String id, String alias) {
		this.path = path;
		this.id = id;
		this.alias = alias;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public String getAlias() {
		return alias;
	}
	
	public void setAlias(String alias) {
		this.alias = alias;
	}

	@Override
	public String toString() {
		return "Folder [path=" + path + ", id=" + id + ", alias=" + alias + "]";
	}

}
