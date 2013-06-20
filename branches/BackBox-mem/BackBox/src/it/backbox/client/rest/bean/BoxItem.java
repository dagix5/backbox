package it.backbox.client.rest.bean;

import com.google.api.client.util.Key;

public class BoxItem {
	
	@Key
	public String type;
	
	@Key
	public String id;
	
	@Key
	public String sequence_id;
	
	@Key
	public String etag;
	
	@Key
	public String name;
	
	@Key
	public BoxFolder parent;
	
	@Key
	public long size;

}
