package tetris.network;

import java.io.Serializable;

/**
 * POJO that holds information about a message.
 * 
 * @author Dominik Lameter
 */
public class Message implements Serializable {

	/**
	 * Generated serial version Id
	 */
	private static final long serialVersionUID = -6311154892365700349L;
	public Connection source;
	public Object content;
	
	public Message (Connection source, Object content) {
		this.source = source;
		this.content = content;
	}
	
	/**
	 * Returns the source of the message.
	 * 
	 * @return Connection representing message source
	 */
	public Connection getSource() {
		return source;
	}
	
	/**
	 * Returns the content of the message.
	 * 
	 * @return Object representing message content
	 */
	public Object getContent() {
		return content;
	}
}
