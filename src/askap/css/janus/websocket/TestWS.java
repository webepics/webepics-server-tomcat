package askap.css.janus.websocket;

import java.io.IOException;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@ServerEndpoint("/ws/test")
public class TestWS {

	private static Logger logger = Logger.getLogger(TestWS.class);
	private static Gson theGson = (new GsonBuilder()).setPrettyPrinting().create();

	public TestWS() {
	}

	@OnOpen
	public void open(Session session) {
	}

	@OnClose
	public void close(Session session) {
	}

	@OnError
	public void onError(Throwable error) {
	}

	@OnMessage
	public void handleMessage(String message, Session session, boolean last) {
		try {
			if (session.isOpen()) {
				session.getBasicRemote().sendText("Hello " + message);
			}
		} catch (IOException e) {
			logger.error("Could not send msg", e);
		}
	}
}
