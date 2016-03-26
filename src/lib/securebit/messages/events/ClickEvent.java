package lib.securebit.messages.events;

import java.io.IOException;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;

import lib.securebit.messages.JsonRepresentedObject;

public class ClickEvent implements JsonRepresentedObject {
	
	public static ClickEvent deserialize(JsonObject obj) {
		return new ClickEvent(ClickEventType.get(obj.get("action").getAsString()), obj.get("value").getAsString());
	}
	
	
	private ClickEventType type;
	private String value;
	
	public ClickEvent(ClickEventType type, String value) {
		this.type = type;
		this.value = value;
	}
	
	@Override
	public void writeJson(JsonWriter writer) throws IOException {
		writer.beginObject();
		writer.name("action").value(this.type.toString());
		writer.name("value").value(this.value);
		writer.endObject();
	}
	
	public ClickEventType getType() {
		return this.type;
	}
	
	public String getValue() {
		return this.value;
	}
	
	public static enum ClickEventType {
		
		RUN_COMMAND("run_command"),
		SUGGEST_MESSAGE("suggest_command"),
		OPEN_FILE("open_file"),
		OPEN_URL("open_url");
		
		
		private String str;
		
		private ClickEventType(String str) {
			this.str = str;
		}
		
		@Override
		public String toString() {
			return this.str;
		}
		
		
		public static ClickEventType get(String str) {
			for (ClickEventType type : ClickEventType.values()) {
				if (type.toString().equals(str)) {
					return type;
				}
			}
			
			return null;
		}
		
	}

}
