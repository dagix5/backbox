package it.backbox.client.rest.bean;

import com.google.api.client.util.Key;

public class BoxUserInfo {

	@Key
	public String id;
	
	@Key
	public String name;
	
	@Key
	public String login;
	
	@Key
	public long space_amount;
	
	@Key
	public long space_used;
	
	@Key
	public long max_upload_size;

}
