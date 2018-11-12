package askap.css.janus.websocket;

import java.util.HashMap;
import java.util.Map;

import javax.websocket.Session;

public class ClientManager {
	Map<Long, Client> pvMonitorMap = new HashMap<Long, Client>();
	Session session;
	
	public ClientManager(Session session) {
		this.session = session;
	}
	
	public void subscribe(String pvName, long id) {
		Client monitor = pvMonitorMap.get(id);
		if (monitor!=null)
			return;
		
		monitor = new Client(this.session, pvName, id);
		monitor.subscribe();
		pvMonitorMap.put(id, monitor);
	}
	
	public void unsubscriber(long id) {
		Client monitor = pvMonitorMap.remove(id);
		if (monitor!=null)	
			monitor.unsubscriber();
	}

	public void write(String jsonValue, long id) {
		Client monitor = pvMonitorMap.get(id);
		if (monitor!=null)
			monitor.write(jsonValue);
	}
	
	public void close() {
		for (Client client : pvMonitorMap.values()) {
			client.unsubscriber();
		}
		pvMonitorMap.clear();
	}
}
