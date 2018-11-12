package askap.css.janus.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.google.gson.JsonObject;

import askap.css.janus.pvmanager.PVChannelManager;
import askap.css.janus.util.Util;
import askap.css.janus.util.VTypeJsonConvert;
import askap.css.janus.websocket.ResponseMessage;


/**
 * Fetches corresponding pvvalue files for the given pvname. Will use same message format as subscribe:
 * 
 * {
 *   “message” : “event”,
 *   “id” : <unique-id-integer>,
 *   “channel” : <channel-name>
 *   “type” : ”value”,
 *   “value” : <json-value>
 * }
 * 
 *  
 * @author wu049
 *
 */
public class PVFetcher extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(PVFetcher.class);
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public PVFetcher() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pvname = request.getParameter("pvname");
        String obj = getPVNames(pvname);
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(obj);		
	}
	
	private String getPVNames(String pvname) {
		
        JsonObject obj = new JsonObject();
        if (pvname==null || pvname.trim().isEmpty()) {
	        	obj.addProperty("status", "ERROR");
	        obj.addProperty("message", "'pvname' not supplied");
         	
	        return Util.theGson.toJson(obj);
        }
        
        try {
        	
        		ResponseMessage val = ResponseMessage.createValueMessage(-1, pvname, PVChannelManager.getPVChannelManager().getValue(pvname));
	        return Util.theGson.toJson(val);
        } catch (Exception e) {
	        	obj.addProperty("status", "ERROR");
	        obj.addProperty("message", "Could not get value for " + pvname + ": " + e.getMessage());
	     	
	        return Util.theGson.toJson(obj);        	
        }
	}
    
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
	

}
