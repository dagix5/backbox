package it.backbox.client.rest.bean;

import java.util.List;

import com.google.api.client.util.Key;

public class BoxSearchResult {
	
	@Key
	public List<BoxObject> entries;

}
