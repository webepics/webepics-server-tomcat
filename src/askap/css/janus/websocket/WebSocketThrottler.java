package askap.css.janus.websocket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.websocket.Session;

public class WebSocketThrottler {
	
	HashMap<Session, List<ResponseMessage>> sessionMessages = new HashMap<Session, List<ResponseMessage>>();
	
	Thread senderThread;
	boolean keepGoing = true;
	
	public WebSocketThrottler() {
		senderThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				while (keepGoing) {
					HashMap<Session, List<ResponseMessage>> sendMessages = null;
					
					synchronized (sessionMessages) {
						sendMessages = (HashMap<Session, List<ResponseMessage>>) sessionMessages.clone();
						sessionMessages.clear();
					}
					
					
					for (Session session : sendMessages.keySet()) {
						List<ResponseMessage> messages = sendMessages.get(session);
						SocketRequestHandler.send(session, messages);
					}

					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		
		senderThread.start();		
	}
	
	public void stop() {
		keepGoing = false;
	}
	
	public void sendMessage(Session session, ResponseMessage message) {
		synchronized (sessionMessages) {
			List<ResponseMessage> messages = sessionMessages.get(session);
			
			if (messages==null) {
				messages = new ArrayList<ResponseMessage>();
				sessionMessages.put(session, messages);
			}
			
			messages.add(message);			
		}
	}
	
	public void removeSession(Session session) {
		synchronized (sessionMessages) {
			sessionMessages.remove(session);
		}
	}

}
