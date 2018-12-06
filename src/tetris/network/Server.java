package tetris.network;

import java.rmi.RemoteException;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Server implements ServerInterface {

	private Connection host;
	private Queue<Message> queue;
	private ServerManager manager;
	
	public Server(Connection host, ServerManager manager) {
		this.host = host;
		this.manager = manager;
		
		queue = new ConcurrentLinkedQueue<Message>();
	}
	
	@Override
	public void register(Connection conn) throws RemoteException {
		manager.connect(conn);
	}
	
	@Override
	public void unregister(Connection connection) {
		manager.disconnect(connection);
	}

	@Override
	public void send(Message message) {
		queue.offer(message);
	}

	@Override
	public Connection getHostConnection() {
		return host;
	}
	@Override
	public Set<Connection> getRegistered() {
		return manager.getConnected();
	}
	
	/**
	 * Grabs a message from the Server's queue.
	 * 
	 * @return Message from queue or null if queue is empty
	 */
	public Message receive() {
		return queue.poll();
	}
	
	/**
	 * Returns the size of the server's queue right now.
	 * 
	 * @return int size of queue
	 */
	public int queueSize() {
		return queue.size();
	}
}
