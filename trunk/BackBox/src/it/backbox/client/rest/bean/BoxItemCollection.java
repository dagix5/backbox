package it.backbox.client.rest.bean;

import java.util.List;

import com.google.api.client.util.Key;

public class BoxItemCollection {
	
	@Key
	public List<BoxFile> entries;
	@Key
	public int offset;
	@Key
	public int limit;
	@Key
	public int total_count;

}
