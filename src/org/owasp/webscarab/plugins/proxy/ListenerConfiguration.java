/**
 * 
 */
package org.owasp.webscarab.plugins.proxy;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * @author rdawes
 * 
 */
public class ListenerConfiguration {

	public static final String PROPERTY_HOSTNAME = "hostName";
	
	public static final String PROPERTY_PORT = "port";
	
	public static final String PROPERTY_BASE = "base";
	
	private String hostName;
	
	private int port;

	private String base;

	public ListenerConfiguration() {
	}

	public ListenerConfiguration(ListenerConfiguration config) {
		setBase(config.getBase());
		setHostName(config.getHostName());
		setPort(config.getPort());
	}
	
	public String getBase() {
		return this.base;
	}

	public SocketAddress getSocketAddress() {
		if (port < 1 || port > 65535) return null;
		if (hostName == null) 
			return new InetSocketAddress(port);
		return new InetSocketAddress(hostName, port);
	}

	public String getHostName() {
		return this.hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public int getPort() {
		return this.port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setBase(String base) {
		this.base = base;
	}

}
