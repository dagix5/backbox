package it.backbox.client.rest.bean;

public class ProxyConfiguration {

	private boolean enabled;
	private String address;
	private int port;

	public ProxyConfiguration(boolean enabled, String address, int port) {
		super();
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
