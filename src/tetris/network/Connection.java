package tetris.network;

import java.io.Serializable;
import java.util.Objects;

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

	/**
	 * Returns Connection host.
	 * 
	 * @return String host of Connection
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Sets the host of this Connection.
	 * 
	 * @param host Connection to set as host
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * Returns the port number of the Connection.
	 * 
	 * @return int port number of Connection
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Sets the port number of this Connection.
	 * 
	 * @param port int to set as port number
	 */
	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof Connection)) {
			return false;
		}
		
		Connection c = (Connection) o;
		return c.getPort() == this.getPort() && c.getHost().equals(this.getHost());
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(host, port);
	}
}
