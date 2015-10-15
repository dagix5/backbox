package it.backbox.client.rest.bean;

import com.google.api.client.util.Key;

public class BoxContextInfo {

	@Key
	public BoxFile[] conflicts;
	
	@Key
	public BoxFile conflict;
}
