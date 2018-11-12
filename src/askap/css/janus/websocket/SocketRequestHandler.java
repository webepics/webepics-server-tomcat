package askap.css.janus.websocket;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import askap.css.janus.util.Util;
import askap.css.janus.websocket.RequestMessage.MessageType;

/**
 * 
 * Protocol based on WebPOD: https://github.com/diirt/diirt/wiki/WebPods-Protocol-Specification-v1
 * We don't have to recreated one from scratch.
 * 
 * Requests:
 *
 * {
 *   “message” : “subscribe”,
 *   “id” : <unique-id-integer>,
 *   “channel” : <channel-name>
 * }
 * 
 * {
 *   “message” : “unsubscribe”,
 *   “id” : <unique-id-integer>,
 * }
 * 
 * {
 *   “message” : “write”,
 *   “id” : <unique-id-integer>,
 *   “value” : <json-value>
 * }
 * 
 * 
 * Responses:
 * {
 *   “message” : “event”,
 *   “id” : <unique-id-integer>,
 *   “channel” : <channel-name>
 *   “type” : “connection”,
 *   “connected” : <boolean>,
 *   “writeConnected” :<boolean>
 * }
 *
 * {
 *   “message” : “event”,
 *   “id” : <unique-id-integer>,
 *   “channel” : <channel-name>
 *   “type” : ”value”,
 *   “value” : <json-value>
 * }
 * 
 * {
 *   “message” : “event”,
 *   “id” : <unique-id-integer>,
 *   “channel” : <channel-name>
 *   “type” : ”error”,
 *   “error” : <message-string>
 * }
 * 
 * {
 *   “message” : “event”,
 *   “id” : <unique-id-integer>,
 *   “channel” : <channel-name>
 *   “type” : ”writeCompleted”,
 *   “successful” : <boolean>,
 *   “error” : <message-string>
 * }
 * 
 * @author wu049
 *
 */
@ServerEndpoint("/ws/pv")
public class SocketRequestHandler {
	
	private static Logger logger = Logger.getLogger(SocketRequestHandler.class);
	
	private static Map<String, ClientManager> sessionClientManagerMap = new HashMap<String, ClientManager>();
	private static WebSocketThrottler throttler = new WebSocketThrottler();
	
	public SocketRequestHandler() {				
	}

	@OnOpen
	public void open(Session session) {
		ClientManager manager = new ClientManager(session);
		
		String sessionId = session.getId();
		sessionClientManagerMap.put(sessionId, manager);
	}

	@OnClose
	public void close(Session session) {
		ClientManager manager = sessionClientManagerMap.remove(session.getId());
		if (manager!=null)
			manager.close();		
	}

	@OnError
	public void onError(Throwable error) {
	}

	@OnMessage
	public void handleMessage(String message, Session session, boolean last) {
		RequestMessage request = Util.theGson.fromJson(message, RequestMessage.class);
		
		ClientManager manager = sessionClientManagerMap.get(session.getId());
		if (manager==null) {			
			sendErrorResponse("No PV Channel found.", request.channel, request.id, session);
			return;
		}
					
		if (request.message.equals(MessageType.subscribe)) {			
			manager.subscribe(request.channel, request.id);			
		} else if (request.message.equals(MessageType.unsubscribe)) {			
			manager.unsubscriber(request.id);			
		} else if (request.message.equals(MessageType.write)) {
			manager.write(request.value, request.id);
		}
	}
	
	public static void sendErrorResponse(String errorMsg, String pvName, long id, Session session) {
		ResponseMessage response = ResponseMessage.createErrorMessage(id, pvName, errorMsg);
		sendResponse(session, response);
	}

	
	public static void sendResponse(Session session, ResponseMessage response) {
		throttler.sendMessage(session, response);
	}

/*	
	public static void sendResponse(Session session, ResponseMessage response) {				
		synchronized (session) {		
			if (!session.isOpen()) {
				logger.debug("Session closed, could not send message");
				ClientManager manager = sessionClientManagerMap.remove(session.getId());
				if (manager!=null)
					manager.close();		
				return;
			}
			
			try {
				String message = Util.theGson.toJson(response);
				session.getBasicRemote().sendText(message);				
			} catch (Exception e) {
				logger.error("Could not send message", e);
				ClientManager manager = sessionClientManagerMap.remove(session.getId());
				if (manager!=null)
					manager.close();		
			}
		}		
	}
*/
	public static void send(Session session, List<ResponseMessage> messages) {	
		synchronized (session) {                
			if (!session.isOpen()) {
		       logger.error("Session closed, could not send message");
				ClientManager manager = sessionClientManagerMap.remove(session.getId());
				if (manager!=null)
					manager.close();	
				throttler.removeSession(session);
				return;
			}
		   
			try {
				String msg = Util.theGson.toJson(messages);
				session.getBasicRemote().sendText(msg);
			} catch (Exception e) {
		       logger.error("Could not send message", e);
		       ClientManager manager = sessionClientManagerMap.remove(session.getId());
		       if (manager!=null)
		    	       manager.close();                
				throttler.removeSession(session);
			}
        }              
	}

}
