package tetris.network;

import java.io.Serializable;

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
	
	public Connection getSource() {
		return source;
	}
	
	public Object getContent() {
		return content;
	}
}
