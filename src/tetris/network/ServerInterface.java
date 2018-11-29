package tetris.network;

import java.rmi.Remote;
import java.util.Set;

public interface ServerInterface extends Remote {

	public void connect(String host, int port);
	public void register(String host, int port);
	public void send(Object object);
	public Set<Connection> getRegistered();
}
