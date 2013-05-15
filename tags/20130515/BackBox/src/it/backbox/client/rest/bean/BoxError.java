package it.backbox.client.rest.bean;

import com.google.api.client.util.Key;

public class BoxError {
	
	@Key
	public String type;
	
	@Key
	public int status;
	
	@Key
	public String code;

	@Key
	public BoxContextInfo context_info;
	
}
