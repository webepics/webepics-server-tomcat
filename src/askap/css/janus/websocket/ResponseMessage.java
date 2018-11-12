package askap.css.janus.websocket;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class ResponseMessage {
	
	public enum ResponseType {
		connection, error, writeCompleted, value
	}
	
	String message = "event";
	long id;
	
	JsonObject value;
	
	String channel = "event";
	
	ResponseType type;
	boolean connected;
	boolean writeConnected;
	
	boolean successful;
	String error;
	
	// got to only send relevent fields 
	static public class ResponseMessageSerializer implements JsonSerializer<ResponseMessage> {

		@Override
		public JsonElement serialize(ResponseMessage src, Type type, JsonSerializationContext context) {
			JsonObject obj = new JsonObject();
			obj.addProperty("message", src.message);
			obj.addProperty("id", src.id);
			obj.addProperty("type", src.type.name());

			switch (src.type) {
				case connection:
					obj.addProperty("connected", src.connected);
					obj.addProperty("writeConnected", src.writeConnected);					
					obj.add("value", src.value);
					break;
				case error:
					obj.addProperty("error", src.error);
					break;
				case writeCompleted:
					obj.addProperty("successful", src.successful);
					obj.addProperty("error", src.error);
					break;
				case value:
					obj.add("value", src.value);
			}
						
			return obj;
		}
		
	}
	
	public static ResponseMessage createErrorMessage(long id, String pvName, String errorMsg) {
		ResponseMessage response = new ResponseMessage();
		response.id = id;
		response.type = ResponseType.error;
		response.error = errorMsg;
		response.channel = pvName;

		return response;
	}
	
	public static ResponseMessage createConnectedMessage(long id, String pvName, boolean connected, boolean writeConnected) {
		ResponseMessage response = new ResponseMessage();
		response.id = id;
		response.type = ResponseType.connection;
		response.connected = connected;
		response.writeConnected = writeConnected;
		response.channel = pvName;
		
		return response;		
	}

	
	public static ResponseMessage createValueMessage(long id, String pvName, JsonObject value) {
		ResponseMessage response = new ResponseMessage();
		response.id = id;
		response.type = ResponseType.value;
		response.value = value;
		response.channel = pvName;
		
		return response;		
	}

	public static ResponseMessage createWriteResponseMessage(long id, String pvName, boolean successful, String error) {
		ResponseMessage response = new ResponseMessage();
		response.id = id;
		response.type = ResponseType.writeCompleted;
		response.successful = successful;
		response.error = error;
		response.channel = pvName;
		
		return response;
	}
	
}
