package it.backbox.client.rest.bean;

import java.util.List;

import com.google.api.client.util.Key;

public class BoxError {
	
	@Key
	public String type;
	
	@Key
	public int status;
	
	@Key
	public String code;

	@Key
	public ContextInfo context_info;
	
	public class ContextInfo {
		
		@Key
		public List<BoxFile> conflicts;
		
	}
}
