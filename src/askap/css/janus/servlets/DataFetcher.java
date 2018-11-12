package askap.css.janus.servlets;

import java.io.IOException;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import askap.css.janus.util.Util;


/**
 * Fetches corresponding json files for the given type and merge them into one json list in the following format:
 *
 * [{
 *		"name": ":bul:fpga:temp",
 *		"description": "tempurature",
 *		"unit": "C"
 *	},
 *	{
 *		"name": ":bul:fpga:vccAux",
 *		"description": "Aux Voltage",
 *		"unit": "V"
 *	}
 * ]
 * 
 * If there is an error, return error json message:
 * 
 * {
 *   "status"  : "ERROR",
 *   "message" : "Type not supported"
 * }
 *  
 * @author wu049
 *
 */
public class DataFetcher extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(DataFetcher.class);
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DataFetcher() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pvfiles = request.getParameter("pvfiles");
        JsonElement obj = getPVNames(pvfiles);
        
        String json = new Gson().toJson(obj);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);		
	}
	
	private JsonElement getPVNames(String pvfiles) {
		
        JsonObject obj = new JsonObject();
        if (pvfiles==null || pvfiles.trim().isEmpty()) {
        	obj.addProperty("status", "ERROR");
         	obj.addProperty("message", "'pvfiles' not supplied");
         	
         	return obj;
        }
        
        StringTokenizer tokenizer = new StringTokenizer(pvfiles, ",");
        int i = 0;
        String fileNames[]  = new String[tokenizer.countTokens()];
        while (tokenizer.hasMoreTokens()) {
        	fileNames[i] = tokenizer.nextToken().trim();
        	i++;
        }
        
        try {
			return Util.mergeJsonFiles(fileNames);
		} catch (Exception e) {
			
			logger.error("Could not load pvfiles for " + pvfiles, e);
			
        	obj.addProperty("status", "ERROR");
         	obj.addProperty("message", "Could not load pvfiles for " + pvfiles + ": " + e.getMessage());
         	
         	return obj;
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
