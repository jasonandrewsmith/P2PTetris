package tetris.network;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class that handles all the networking to provide a fully connected P2P overlay network. Manages sending and receiving messages for the client as well as connecting to other servers.
 * 
 * @author Dominik Lameter
 *
 */
public class ServerManager {
	
	private Server server;
	private Registry registry;
	private Map<Connection, ServerInterface> connections;
	
	public ServerManager(Connection connection) throws RemoteException {
		this.server = new Server(connection, this);
		
		ServerInterface stub = (ServerInterface) UnicastRemoteObject.exportObject(server, 0);
		
		registry = LocateRegistry.createRegistry(server.getHostConnection().getPort());
		registry.rebind("TetrisServer", stub);
		System.out.println("Server ready");
		
		this.connections = new ConcurrentHashMap<Connection, ServerInterface>();
	}
	
	/**
	 * Connects to a peer and all the peers connected to that peer. Enables a fully connected network.
	 * 
	 * @param connection Connection of server to connect to
	 * @throws RemoteException
	 */
	public void connect(Connection connection) throws RemoteException {
		// Connect to server's registry
		Registry otherRegistry = LocateRegistry.getRegistry(connection.getHost(), connection.getPort());
		
		// Get server instance
		ServerInterface stub = null;
		if (connections.containsKey(connection)) {
			stub = connections.get(connection);
		}
		else {
			try {
				stub = (ServerInterface) otherRegistry.lookup("TetrisServer");
			} catch (NotBoundException e) {
				System.err.println("Could not connect to \"Tetris Server\" on " + connection.getHost() + " with port " + connection.getPort());
				e.printStackTrace();
				return;
			}
			
			// Add connection to mapping
			connections.put(connection, stub);
			
			// Register self on other server
			stub.register(server.getHostConnection());
		}
		
		// Connect to all register connection in that which is being connected to
		for (Connection conn : stub.getRegistered()) {
			if (!connections.keySet().contains(conn) && !conn.equals(server.getHostConnection())) {
				connect(conn);
			}
		}
	}
	
	/**
	 * Disconnects from the specified connection if it exists. Does nothing otherwise.
	 * 
	 * @param conn Connection of the client to disconnect from
	 */
	public void disconnect(Connection conn) {
		connections.remove(conn);
	}
	
	/**
	 * Returns the information for the current host.
	 * 
	 * @return Connection representing this application
	 */
	public Connection getConnection() {
		return server.getHostConnection();
	}
	
	/**
	 * Returns the set of connections connected to this client.
	 * 
	 * @return Set<Connection> of connected clients
	 */
	public Set<Connection> getConnected() {
		return connections.keySet();
	}
	
	
	/**
	 * Sends an object to all the connected servers. Disconnects from a server if it fails to send a message to it.
	 * 
	 * @param object Object to send in a message.
	 */
	public void send(Object object) {
		Message outgoingMessage = new Message(server.getHostConnection(), object);
		
		for (Entry<Connection, ServerInterface> entry : connections.entrySet()) {
			try {
				entry.getValue().send(outgoingMessage);
			} catch (RemoteException e) {
				System.err.println("Failed to send object to server \"" + entry.getKey().getHost() + "\". Disconnecting from server.");
				e.printStackTrace();
				disconnect(entry.getKey());
			}
		}
	}
	
	/**
	 * Try to receive a Message from the Server. Returns null if there are no messages.
	 * 
	 * @return Message or null if there are messages.
	 */
	public Message receive() {
		return server.receive();
	}
	
	/**
	 * Closes all connections the current manager has and unbinds the server from the rmi repository
	 */
	public void close() {
		for (ServerInterface otherServer : connections.values()) {
			try {
				otherServer.unregister(server.getHostConnection());
			} catch (RemoteException e) {
				System.err.println("Failed to close connection with " + server.getHostConnection().getHost());
				e.printStackTrace();
			}
		}
		
		try {
			registry.unbind("TetrisServer");
		} catch (RemoteException | NotBoundException e) {
			System.err.println("Failed to unbind \"TetrisServer\" from current registry.");
			e.printStackTrace();
		}
	}
}
