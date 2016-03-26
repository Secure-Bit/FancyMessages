package lib.securebit.messages;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import com.google.gson.stream.JsonWriter;

public final class JsonString implements JsonRepresentedObject, ConfigurationSerializable {
	
	public static JsonString deserialize(Map<String, Object> map){
		return new JsonString(map.get("stringValue").toString());
	}
	
	private String value;
	
	public JsonString(CharSequence value){
		this.value = value == null ? null : value.toString();
	}

	@Override
	public void writeJson(JsonWriter writer) throws IOException {
		writer.value(getValue());
	}
	
	@Override
	public Map<String, Object> serialize() {
		HashMap<String, Object> theSingleValue = new HashMap<String, Object>();
		theSingleValue.put("stringValue", this.value);
		return theSingleValue;
	}
	
	@Override
	public String toString(){
		return this.value;
	}
	
	public String getValue(){
		return this.value;
	}

}