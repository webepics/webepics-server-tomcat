package askap.css.janus.util;

import org.apache.log4j.Logger;
import org.epics.pvaClient.PvaClientGetData;
import org.epics.pvaClient.PvaClientMonitorData;
import org.epics.pvaClient.PvaClientPutData;
import org.epics.pvdata.property.Alarm;
import org.epics.pvdata.property.TimeStamp;
import org.epics.pvdata.pv.BooleanArrayData;
import org.epics.pvdata.pv.ByteArrayData;
import org.epics.pvdata.pv.DoubleArrayData;
import org.epics.pvdata.pv.FloatArrayData;
import org.epics.pvdata.pv.IntArrayData;
import org.epics.pvdata.pv.LongArrayData;
import org.epics.pvdata.pv.PVBoolean;
import org.epics.pvdata.pv.PVBooleanArray;
import org.epics.pvdata.pv.PVByte;
import org.epics.pvdata.pv.PVByteArray;
import org.epics.pvdata.pv.PVDouble;
import org.epics.pvdata.pv.PVDoubleArray;
import org.epics.pvdata.pv.PVFloat;
import org.epics.pvdata.pv.PVFloatArray;
import org.epics.pvdata.pv.PVInt;
import org.epics.pvdata.pv.PVIntArray;
import org.epics.pvdata.pv.PVLong;
import org.epics.pvdata.pv.PVLongArray;
import org.epics.pvdata.pv.PVScalar;
import org.epics.pvdata.pv.PVScalarArray;
import org.epics.pvdata.pv.PVShort;
import org.epics.pvdata.pv.PVShortArray;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStringArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.ShortArrayData;
import org.epics.pvdata.pv.StringArrayData;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

/**
 * Scalar Type { "type": "double", "value": 1.54, "alarm": { "severity":
 * "MINOR", "status": "LOW", "message": "LOW_ALARM", }, "time": UNIX millisec }
 * 
 * Array Type: { "type": "doubleArray", "value": [1.54, 0.0, 3.14] "alarm": {
 * "severity": "NONE", "status": "NONE", }, "timestamp": UNIX epoch millisec }
 * 
 * Structure Type: { "type": "structure", "value":"OK", "index": 1, "choices" :
 * ["BAD","OK"], "alarm": { "severity": "NONE", "status": "NONE", },
 * "timestamp": UNIX epoch millisec }
 * 
 * For json to vtype, no need timestamp or alarm, just type and value
 * 
 * { "type": "double", "value": 1.54, }
 * 
 * { "type": "doubleArray", "value": [1.54, 0.0, 3.14] }
 * 
 * 
 * @author wu049
 *
 */

public class VTypeJsonConvert {

	private static Logger logger = Logger.getLogger(VTypeJsonConvert.class);

	public static void jsonToPVData(String value, PvaClientPutData pvData) throws Exception {
		JsonParser parser = new JsonParser();
		JsonObject jsonObj = parser.parse(value).getAsJsonObject();

		JsonElement typeElm = jsonObj.get("type");
		if (typeElm == null)
			throw new Exception("Write error: type not supplied");

		JsonElement valueElm = jsonObj.get("value");
		if (valueElm == null)
			throw new Exception("Write error: value not supplied");

		String type = typeElm.getAsString();
		if (type.equalsIgnoreCase("boolean")) {
			((PVBoolean) pvData.getScalarValue()).put(valueElm.getAsBoolean());

		} else if (type.equalsIgnoreCase("byte")) {
			((PVByte) pvData.getScalarValue()).put(valueElm.getAsByte());

		} else if (type.equalsIgnoreCase("double")) {
			((PVDouble) pvData.getScalarValue()).put(valueElm.getAsDouble());

		} else if (type.equalsIgnoreCase("float")) {
			((PVFloat) pvData.getScalarValue()).put(valueElm.getAsFloat());

		} else if (type.equalsIgnoreCase("int")) {
			((PVInt) pvData.getScalarValue()).put(valueElm.getAsInt());

		} else if (type.equalsIgnoreCase("long")) {
			((PVLong) pvData.getScalarValue()).put(valueElm.getAsLong());

		} else if (type.equalsIgnoreCase("short")) {
			((PVShort) pvData.getScalarValue()).put(valueElm.getAsShort());

		} else if (type.equalsIgnoreCase("String")) {
			((PVString) pvData.getScalarValue()).put(valueElm.getAsString());

		} else {
			throw new Exception("Write error: " + type.toString() + " not support.");
		}

		/*
		 * won't support array write for prototype
		 * 
		 * } else if (type.equalsIgnoreCase("booleanArray")) {
		 * ((PVBooleanArray)pvData.getScalarValue()).put
		 * 
		 * } else if (type.equalsIgnoreCase("byteArray")) { } else if
		 * (type.equalsIgnoreCase("doubleArray")) { } else if
		 * (type.equalsIgnoreCase("floatArray")) { } else if
		 * (type.equalsIgnoreCase("intArray")) { } else if (type.equalsIgnoreCase(
		 * "longArray")) { } else if (type.equalsIgnoreCase("shortArray")) { } else if
		 * (type.equalsIgnoreCase("stringArray")) {
		 * 
		 */

	}

	public static JsonObject PVToJson(PvaClientMonitorData monitorData) {

		JsonObject obj = new JsonObject();

		boolean isScalar = monitorData.isValueScalar();
		boolean isScalarArray = monitorData.isValueScalarArray();

		if (isScalar) {
			PVScalar scalarVal = monitorData.getScalarValue();
			ScalarType type = scalarVal.getScalar().getScalarType();
			// these basic types will do for prototyping
			if (ScalarType.pvBoolean.equals(type)) {

				boolean boolVal = ((PVBoolean) scalarVal).get();
				obj.addProperty("type", "boolean");
				obj.addProperty("value", boolVal);

			} else if (ScalarType.pvByte.equals(type)) {

				byte byteVal = ((PVByte) scalarVal).get();
				obj.addProperty("type", "byte");
				obj.addProperty("value", byteVal);

			} else if (ScalarType.pvDouble.equals(type)) {

				double doubleVal = ((PVDouble) scalarVal).get();
				obj.addProperty("type", "double");

				if (Double.POSITIVE_INFINITY == doubleVal) {
					obj.addProperty("value", "Infinity");
				} else if (Double.NEGATIVE_INFINITY == doubleVal) {
					obj.addProperty("value", "-ve Infinity");
				} else
					obj.addProperty("value", doubleVal);

			} else if (ScalarType.pvFloat.equals(type)) {

				float floatVal = ((PVFloat) scalarVal).get();
				obj.addProperty("type", "float");
				obj.addProperty("value", floatVal);

			} else if (ScalarType.pvInt.equals(type)) {

				int intVal = ((PVInt) scalarVal).get();
				obj.addProperty("type", "int");
				obj.addProperty("value", intVal);

			} else if (ScalarType.pvLong.equals(type)) {

				long longVal = ((PVLong) scalarVal).get();
				obj.addProperty("type", "long");
				obj.addProperty("value", longVal);

			} else if (ScalarType.pvShort.equals(type)) {

				short shortVal = ((PVShort) scalarVal).get();
				obj.addProperty("type", "short");
				obj.addProperty("value", shortVal);

			} else if (ScalarType.pvString.equals(type)) {

				String strVal = ((PVString) scalarVal).get();
				obj.addProperty("type", "String");
				obj.addProperty("value", strVal);

			} else {
				logger.error("Convert error: " + type.toString() + " not support.");
			}

		} else if (isScalarArray)

		{
			// array types
			PVScalarArray arrayVal = monitorData.getScalarArrayValue();
			ScalarType type = arrayVal.getScalarArray().getElementType();
			int len = arrayVal.getLength();

			// these basic types will do for prototyping
			if (ScalarType.pvBoolean.equals(type)) {
				obj.addProperty("type", "booleanArray");

				BooleanArrayData data = new BooleanArrayData();
				((PVBooleanArray) arrayVal).get(0, len, data);

				JsonArray jsonArray = new JsonArray();
				for (boolean d : data.data) {
					jsonArray.add(new JsonPrimitive(d));
				}
				obj.add("value", jsonArray);

			} else if (ScalarType.pvByte.equals(type)) {

				obj.addProperty("type", "byteArray");

				ByteArrayData data = new ByteArrayData();
				((PVByteArray) arrayVal).get(0, len, data);

				JsonArray jsonArray = new JsonArray();
				for (byte d : data.data) {
					jsonArray.add(new JsonPrimitive(d));
				}
				obj.add("value", jsonArray);

			} else if (ScalarType.pvDouble.equals(type)) {

				obj.addProperty("type", "doubleArray");

				DoubleArrayData data = new DoubleArrayData();
				((PVDoubleArray) arrayVal).get(0, len, data);

				JsonArray jsonArray = new JsonArray();
				for (double d : data.data) {
					jsonArray.add(new JsonPrimitive(d));
				}
				obj.add("value", jsonArray);

			} else if (ScalarType.pvFloat.equals(type)) {

				obj.addProperty("type", "floatArray");

				FloatArrayData data = new FloatArrayData();
				((PVFloatArray) arrayVal).get(0, len, data);

				JsonArray jsonArray = new JsonArray();
				for (float d : data.data) {
					jsonArray.add(new JsonPrimitive(d));
				}
				obj.add("value", jsonArray);

			} else if (ScalarType.pvInt.equals(type)) {

				obj.addProperty("type", "intArray");

				IntArrayData data = new IntArrayData();
				((PVIntArray) arrayVal).get(0, len, data);

				JsonArray jsonArray = new JsonArray();
				for (int d : data.data) {
					jsonArray.add(new JsonPrimitive(d));
				}
				obj.add("value", jsonArray);

			} else if (ScalarType.pvLong.equals(type)) {

				obj.addProperty("type", "longArray");

				LongArrayData data = new LongArrayData();
				((PVLongArray) arrayVal).get(0, len, data);

				JsonArray jsonArray = new JsonArray();
				for (long d : data.data) {
					jsonArray.add(new JsonPrimitive(d));
				}
				obj.add("value", jsonArray);

			} else if (ScalarType.pvShort.equals(type)) {

				obj.addProperty("type", "shortArray");

				ShortArrayData data = new ShortArrayData();
				((PVShortArray) arrayVal).get(0, len, data);

				JsonArray jsonArray = new JsonArray();
				for (short d : data.data) {
					jsonArray.add(new JsonPrimitive(d));
				}
				obj.add("value", jsonArray);

			} else if (ScalarType.pvString.equals(type)) {

				obj.addProperty("type", "stringArray");

				StringArrayData data = new StringArrayData();
				((PVStringArray) arrayVal).get(0, len, data);

				JsonArray jsonArray = new JsonArray();
				for (String d : data.data) {
					jsonArray.add(new JsonPrimitive(d));
				}
				obj.add("value", jsonArray);

			} else {
				logger.error("Write error: " + type.toString() + " not support.");
			}
		} else {

			PVStructure pvStructure = monitorData.getPVStructure();
			if (pvStructure != null) {
				PVStructure valueField = pvStructure.getSubField(PVStructure.class, "value");

				PVInt pvIndex = valueField.getSubField(PVInt.class, "index");
				PVStringArray pvChoices = valueField.getSubField(PVStringArray.class, "choices");

				StringArrayData data = new StringArrayData();
				pvChoices.get(0, pvChoices.getLength(), data);

				int index = pvIndex.get();

				JsonArray jsonArray = new JsonArray();
				for (String d : data.data) {
					jsonArray.add(new JsonPrimitive(d));
				}

				obj.addProperty("type", "structure");
				obj.addProperty("index", index);
				obj.addProperty("value", jsonArray.get(index).getAsString());
				obj.add("choices", jsonArray);
			} else {
				logger.error("Write error: " + monitorData.toString() + " not support.");
			}
		}

		// deal with timestamp
		TimeStamp timeStamp = monitorData.getTimeStamp();
		long ms = timeStamp.getMilliSeconds();
		obj.addProperty("timestamp", ms);

		// deal with Alarm
		Alarm alarm = monitorData.getAlarm();
		JsonObject alarmObj = new JsonObject();
		alarmObj.addProperty("severity", alarm.getSeverity().name());
		alarmObj.addProperty("status", alarm.getStatus().name());
		alarmObj.addProperty("message", alarm.getMessage());
		obj.add("alarm", alarmObj);

		return obj;
	}

	public static JsonObject PVToJson(PvaClientGetData monitorData) {
		JsonObject obj = new JsonObject();

		boolean isScalar = monitorData.isValueScalar();
		boolean isScalarArray = monitorData.isValueScalarArray();

		if (isScalar) {
			PVScalar scalarVal = monitorData.getScalarValue();
			ScalarType type = scalarVal.getScalar().getScalarType();
			// these basic types will do for prototyping
			if (ScalarType.pvBoolean.equals(type)) {

				boolean boolVal = ((PVBoolean) scalarVal).get();
				obj.addProperty("type", "boolean");
				obj.addProperty("value", boolVal);

			} else if (ScalarType.pvByte.equals(type)) {

				byte byteVal = ((PVByte) scalarVal).get();
				obj.addProperty("type", "byte");
				obj.addProperty("value", byteVal);

			} else if (ScalarType.pvDouble.equals(type)) {

				double doubleVal = ((PVDouble) scalarVal).get();
				obj.addProperty("type", "double");
				
				if (Double.POSITIVE_INFINITY == doubleVal) {
					obj.addProperty("value", "Infinity");
				} else if (Double.NEGATIVE_INFINITY == doubleVal) {
					obj.addProperty("value", "-ve Infinity");
				} else
					obj.addProperty("value", doubleVal);

			} else if (ScalarType.pvFloat.equals(type)) {

				float floatVal = ((PVFloat) scalarVal).get();
				obj.addProperty("type", "float");
				obj.addProperty("value", floatVal);

			} else if (ScalarType.pvInt.equals(type)) {

				int intVal = ((PVInt) scalarVal).get();
				obj.addProperty("type", "int");
				obj.addProperty("value", intVal);

			} else if (ScalarType.pvLong.equals(type)) {

				long longVal = ((PVLong) scalarVal).get();
				obj.addProperty("type", "long");
				obj.addProperty("value", longVal);

			} else if (ScalarType.pvShort.equals(type)) {

				short shortVal = ((PVShort) scalarVal).get();
				obj.addProperty("type", "short");
				obj.addProperty("value", shortVal);

			} else if (ScalarType.pvString.equals(type)) {

				String strVal = ((PVString) scalarVal).get();
				obj.addProperty("type", "String");
				obj.addProperty("value", strVal);

			} else {
				logger.error("Convert error: " + type.toString() + " not support.");
			}

		} else if (isScalarArray) {
			// array types
			PVScalarArray arrayVal = monitorData.getScalarArrayValue();
			ScalarType type = arrayVal.getScalarArray().getElementType();
			int len = arrayVal.getLength();

			// these basic types will do for prototyping
			if (ScalarType.pvBoolean.equals(type)) {
				obj.addProperty("type", "booleanArray");

				BooleanArrayData data = new BooleanArrayData();
				((PVBooleanArray) arrayVal).get(0, len, data);

				JsonArray jsonArray = new JsonArray();
				for (boolean d : data.data) {
					jsonArray.add(new JsonPrimitive(d));
				}
				obj.add("value", jsonArray);

			} else if (ScalarType.pvByte.equals(type)) {

				obj.addProperty("type", "byteArray");

				ByteArrayData data = new ByteArrayData();
				((PVByteArray) arrayVal).get(0, len, data);

				JsonArray jsonArray = new JsonArray();
				for (byte d : data.data) {
					jsonArray.add(new JsonPrimitive(d));
				}
				obj.add("value", jsonArray);

			} else if (ScalarType.pvDouble.equals(type)) {

				obj.addProperty("type", "doubleArray");

				DoubleArrayData data = new DoubleArrayData();
				((PVDoubleArray) arrayVal).get(0, len, data);

				JsonArray jsonArray = new JsonArray();
				for (double d : data.data) {
					jsonArray.add(new JsonPrimitive(d));
				}
				obj.add("value", jsonArray);

			} else if (ScalarType.pvFloat.equals(type)) {

				obj.addProperty("type", "floatArray");

				FloatArrayData data = new FloatArrayData();
				((PVFloatArray) arrayVal).get(0, len, data);

				JsonArray jsonArray = new JsonArray();
				for (float d : data.data) {
					jsonArray.add(new JsonPrimitive(d));
				}
				obj.add("value", jsonArray);

			} else if (ScalarType.pvInt.equals(type)) {

				obj.addProperty("type", "intArray");

				IntArrayData data = new IntArrayData();
				((PVIntArray) arrayVal).get(0, len, data);

				JsonArray jsonArray = new JsonArray();
				for (int d : data.data) {
					jsonArray.add(new JsonPrimitive(d));
				}
				obj.add("value", jsonArray);

			} else if (ScalarType.pvLong.equals(type)) {

				obj.addProperty("type", "longArray");

				LongArrayData data = new LongArrayData();
				((PVLongArray) arrayVal).get(0, len, data);

				JsonArray jsonArray = new JsonArray();
				for (long d : data.data) {
					jsonArray.add(new JsonPrimitive(d));
				}
				obj.add("value", jsonArray);

			} else if (ScalarType.pvShort.equals(type)) {

				obj.addProperty("type", "shortArray");

				ShortArrayData data = new ShortArrayData();
				((PVShortArray) arrayVal).get(0, len, data);

				JsonArray jsonArray = new JsonArray();
				for (short d : data.data) {
					jsonArray.add(new JsonPrimitive(d));
				}
				obj.add("value", jsonArray);

			} else if (ScalarType.pvString.equals(type)) {

				obj.addProperty("type", "stringArray");

				StringArrayData data = new StringArrayData();
				((PVStringArray) arrayVal).get(0, len, data);

				JsonArray jsonArray = new JsonArray();
				for (String d : data.data) {
					jsonArray.add(new JsonPrimitive(d));
				}
				obj.add("value", jsonArray);

			} else {
				logger.error("Write error: " + type.toString() + " not support.");
			}
		} else {

			PVStructure pvStructure = monitorData.getPVStructure();
			if (pvStructure != null) {
				PVStructure valueField = pvStructure.getSubField(PVStructure.class, "value");

				PVInt pvIndex = valueField.getSubField(PVInt.class, "index");
				PVStringArray pvChoices = valueField.getSubField(PVStringArray.class, "choices");

				StringArrayData data = new StringArrayData();
				pvChoices.get(0, pvChoices.getLength(), data);

				int index = pvIndex.get();

				JsonArray jsonArray = new JsonArray();
				for (String d : data.data) {
					jsonArray.add(new JsonPrimitive(d));
				}

				obj.addProperty("type", "structure");
				obj.addProperty("index", index);
				obj.addProperty("value", jsonArray.get(index).getAsString());
				obj.add("choices", jsonArray);
			} else {
				logger.error("Write error: " + monitorData.toString() + " not support.");
			}
		}

		// deal with timestamp
		TimeStamp timeStamp = monitorData.getTimeStamp();
		long ms = timeStamp.getMilliSeconds();
		obj.addProperty("timestamp", ms);

		// deal with Alarm
		Alarm alarm = monitorData.getAlarm();
		JsonObject alarmObj = new JsonObject();
		alarmObj.addProperty("severity", alarm.getSeverity().name());
		alarmObj.addProperty("status", alarm.getStatus().name());
		obj.add("alarm", alarmObj);

		return obj;
	}

}
