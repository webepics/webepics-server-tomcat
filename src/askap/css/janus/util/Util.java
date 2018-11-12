package askap.css.janus.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import askap.css.janus.websocket.ClientManager;
import askap.css.janus.websocket.ResponseMessage;

public class Util {
	public static Logger logger = Logger.getLogger(Util.class);
	
	public static final Gson theGson;
	
	public static String PVFILE_LOCATION = "";
	private static Properties parset;
	
	static {
		GsonBuilder gsonbuilder = new GsonBuilder();
		gsonbuilder.registerTypeAdapter(ResponseMessage.class, new ResponseMessage.ResponseMessageSerializer());
		theGson = gsonbuilder.setPrettyPrinting().create();
		
		
		parset = new Properties();
		try {
			InputStream in = Util.class.getClassLoader().getResourceAsStream("epics-react.properties");
			parset.load(in);
		} catch (IOException e) {
			System.out.println("Could not load peroperties file: obs.properties");
			e.printStackTrace();
		}
		
		PVFILE_LOCATION = parset.getProperty("pvfiles.directory");	
	}

	
	/**
	 * merge the json contents of all given filesnames into a single json list
	 * @param fileNames
	 * @return
	 */
	public static JsonElement mergeJsonFiles(String fileNames[]) throws Exception {
		JsonArray pvList = new JsonArray();
		
		for (String fileName : fileNames) {
			
			String fullFileName = PVFILE_LOCATION + "/" + fileName.trim();
			
			JsonParser parser = new JsonParser();
			 JsonArray obj = parser.parse(new FileReader(new File(fullFileName))).getAsJsonArray();

			for (JsonElement elem : obj) {
				pvList.add(elem);
			}
		}
		
		return pvList;
	}	
	
}
