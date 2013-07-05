package it.backbox.bean;

import com.google.api.client.util.Key;

public class ProxyConfiguration {

	@Key
	private boolean enabled;
	@Key
	private String address;
	@Key
	private int port;
	
	public ProxyConfiguration() {
		
	}

	public ProxyConfiguration(boolean enabled, String address, int port) {
		this.enabled = enabled;
		this.address = address;
		this.port = port;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

}
