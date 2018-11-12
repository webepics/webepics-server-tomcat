package askap.css.janus.websocket;

public class RequestMessage {
	
	public enum MessageType {
		subscribe, unsubscribe, write
	};
	
	MessageType message;
	long id;
	String channel;
	String value;	
}
