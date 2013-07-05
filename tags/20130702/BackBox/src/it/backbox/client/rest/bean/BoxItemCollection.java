package it.backbox.client.rest.bean;

import java.util.List;

import com.google.api.client.util.Key;

public class BoxItemCollection {
	
	@Key
	public List<BoxFile> entries;

}
