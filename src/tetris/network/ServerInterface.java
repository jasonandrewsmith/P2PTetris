package tetris.network;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

public interface ServerInterface extends Remote {

	/**
	 * Registers the provided connection on the server.
	 * 
	 * @param connection Connection to register on the server
	 * @throws RemoteException
	 */
	public void register(Connection connection) throws RemoteException;
	
	/**
	 * Removes the provided Connection from the server's registered connections.
	 * 
	 * @param connection - Connection to remove
	 * @throws RemoteException
	 */
	public void unregister(Connection connection) throws RemoteException;
	
	/**
	 * Sends a message to the server to be processed.
	 * 
	 * @param message Message to be processed
	 * @throws RemoteException
	 */
	public void send(Message message) throws RemoteException;
	
	/**
	 * Returns the host Connection of the server.
	 * 
	 * @return Connection representing this server
	 * @throws RemoteException
	 */
	public Connection getHostConnection() throws RemoteException;
	
	/**
	 * Returns the set of Connections registered on the server.
	 * 
	 * @return Set<Connection> of registered connections
	 * @throws RemoteException
	 */
	public Set<Connection> getRegistered() throws RemoteException;
}
