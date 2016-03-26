package lib.securebit.messages.events;

import java.io.IOException;

import com.google.gson.stream.JsonWriter;

import lib.securebit.messages.JsonRepresentedObject;

public class ShiftClickEvent implements JsonRepresentedObject {
	
	private ShiftClickEventType type;
	private String value;
	
	public ShiftClickEvent(String value) {
		this(ShiftClickEventType.SUGGEST_MESSAGE, value);
	}
	
	public ShiftClickEvent(ShiftClickEventType type, String value) {
		this.type = type;
		this.value = value;
	}
	
	@Override
	public void writeJson(JsonWriter writer) throws IOException {
		writer.value(this.value);
	}
	
	public ShiftClickEventType getType() {
		return this.type;
	}
	
	public String getValue() {
		return this.value;
	}
	
	public static enum ShiftClickEventType {
		
		SUGGEST_MESSAGE;
		
	}

}
