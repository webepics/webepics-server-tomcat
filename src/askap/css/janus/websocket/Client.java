package askap.css.janus.websocket;

import javax.websocket.Session;

import org.apache.log4j.Logger;
import org.epics.pvaClient.PvaClientMonitor;
import org.epics.pvaClient.PvaClientMonitorData;
import org.epics.pvaClient.PvaClientMonitorRequester;

import com.google.gson.JsonObject;

import askap.css.janus.pvmanager.PVChannelManager;
import askap.css.janus.util.VTypeJsonConvert;;

public class Client implements PvaClientMonitorRequester {
	
	private static Logger logger = Logger.getLogger(Client.class);
	
	
	private boolean isStopped = false;
	private Session session;
	private String pvName;
	private PvaClientMonitor clientMonitor;
	private long id;
	
	public Client(Session session, String pvName, long id) {
		this.session = session;
		this.pvName = pvName;
		this.id = id;
		
		try{
			JsonObject val = PVChannelManager.getPVChannelManager().connect(this.pvName);
			
			connected(val);
		} catch (Exception e) {
			SocketRequestHandler.sendErrorResponse(e.getMessage(), pvName, this.id, this.session);			
		}
	}
	
	public void subscribe() {
		try{
			PVChannelManager.getPVChannelManager().subscriber(this.pvName, this);
		} catch (Exception e) {
			logger.error("Could not subscribe to pv: " + pvName);
			SocketRequestHandler.sendErrorResponse(e.getMessage(), pvName, this.id, this.session);			
		}		
	}

	public void write(String jsonValue) {
		try{
			PVChannelManager.getPVChannelManager().write(this.pvName, jsonValue);
		} catch (Exception e) {			
			logger.error("Could not write to pv: " + pvName, e);
			writeCompleted(false, e.getMessage());
		}
	}

	public void connected(JsonObject val) {
		// send connected event
		ResponseMessage response = ResponseMessage.createConnectedMessage(this.id, pvName, true, false);		
		response.value = val;
		
		SocketRequestHandler.sendResponse(this.session, response);
	}
	
	public void writeCompleted(boolean success, String errorMsg) {		
		// send writeCompleted event
		ResponseMessage response = ResponseMessage.createWriteResponseMessage(this.id, pvName, success, errorMsg);
		SocketRequestHandler.sendResponse(this.session, response);
	}
	
	@Override
	public void event(PvaClientMonitor clientMonitor) {
		if (isStopped) {
			clientMonitor.destroy();
			return;
		}
		
		while (clientMonitor.poll()) {
			PvaClientMonitorData monitorData = clientMonitor.getData();
			ResponseMessage response = ResponseMessage.createValueMessage(this.id, pvName, VTypeJsonConvert.PVToJson(monitorData));
			SocketRequestHandler.sendResponse(this.session, response);
			clientMonitor.releaseEvent();
		}
	}
	
	public void unsubscriber() {
		isStopped = true;
		if (this.clientMonitor!=null) {
			this.clientMonitor.destroy();		
			this.clientMonitor = null;
		}
	}
}
