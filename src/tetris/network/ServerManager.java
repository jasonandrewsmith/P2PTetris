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
	
	public void disconnect(Connection conn) {
		connections.remove(conn);
	}
	
	public Connection getConnection() {
		return server.getHostConnection();
	}
	
	public Set<Connection> getConnected() {
		return connections.keySet();
	}
	
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
	
	public Message receive() {
		return server.receive();
	}
	
	public void close() {
		for (ServerInterface otherServer : connections.values()) {
			try {
				otherServer.unregister(server.getHostConnection());
			} catch (RemoteException e) {
				System.err.println("Failed to close connection with " + server.getHostConnection().getHost());
				e.printStackTrace();
			}
		}
	}
}
