package tetris.network;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

public interface ServerInterface extends Remote {

	public void register(Connection connection) throws RemoteException;
	public void unregister(Connection connection) throws RemoteException;
	public void send(Message message) throws RemoteException;
	public Connection getHostConnection() throws RemoteException;
	public Set<Connection> getRegistered() throws RemoteException;
}
