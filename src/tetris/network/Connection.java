package tetris.network;

import java.io.Serializable;

public class Connection implements Serializable {

	/**
	 * Generated serial version id
	 */
	private static final long serialVersionUID = 404275120794208140L;
	private String host;
	private int port;
	
	public Connection(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
}
