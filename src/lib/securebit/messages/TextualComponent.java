package lib.securebit.messages;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import com.google.common.collect.ImmutableMap;
import com.google.gson.stream.JsonWriter;

import lib.securebit.Validate;

public abstract class TextualComponent implements Cloneable {

	static {
		ConfigurationSerialization.registerClass(TextualComponent.ArbitraryTextTypeComponent.class);
		ConfigurationSerialization.registerClass(TextualComponent.ComplexTextTypeComponent.class);
	}
	
	protected static TextualComponent deserialize(Map<String, Object> map){
		if(map.containsKey("key") && map.size() == 2 && map.containsKey("value")){
			return ArbitraryTextTypeComponent.deserialize(map);
		} else if(map.size() >= 2 && map.containsKey("key") && !map.containsKey("value")){
			return ComplexTextTypeComponent.deserialize(map);
		}
		
		return null;
	}
	
	protected static boolean isTranslatableText(TextualComponent component){
		return component instanceof ComplexTextTypeComponent && ((ComplexTextTypeComponent) component).getKey().equals("translate");
	}
	
	public static boolean isTextKey(String key) {
		return TextKey.get(key) != null;
	}
	
	public static TextualComponent newText(TextKey key, String value) {
		if (key == TextKey.SCORE) {
			return TextualComponent.newScore("*", value);
		} else {
			return new ArbitraryTextTypeComponent(key, value);
		}
	}
	
	public static TextualComponent newScore(String playerName, String scoreboardObjective){
		return new ComplexTextTypeComponent(TextKey.SCORE, ImmutableMap.<String, String>builder()
				.put("name", playerName)
				.put("objective", scoreboardObjective)
				.build());
	}
	
	
	@Override
	public abstract TextualComponent clone() throws CloneNotSupportedException;
	
	@Override
	public String toString() {
		return getReadableString();
	}
	
	public abstract TextKey getKey();
	
	public abstract String getReadableString();
        
	public abstract void writeJson(JsonWriter writer) throws IOException;
	
	
	private static final class ArbitraryTextTypeComponent extends TextualComponent implements ConfigurationSerializable {
		
		public static ArbitraryTextTypeComponent deserialize(Map<String, Object> map){
			return new ArbitraryTextTypeComponent(TextKey.get(map.get("key").toString()), map.get("value").toString());
		}
		
		private TextKey key;
		private String value;
		
		public ArbitraryTextTypeComponent(TextKey key, String value){
			this.setKey(key);
			this.setValue(value);
		}
		
		@Override
		public TextKey getKey() {
			return this.key;
		}
		
		@Override
		public TextualComponent clone() throws CloneNotSupportedException {
			return new ArbitraryTextTypeComponent(getKey(), getValue());
		}

		@Override
		public void writeJson(JsonWriter writer) throws IOException {
			writer.name(this.getKey().toString()).value(this.getValue());
		}

		@Override
		@SuppressWarnings("serial")
		public Map<String, Object> serialize() {
			return new HashMap<String, Object>() {{
				put("key", getKey());
				put("value", getValue());
			}};
		}
		
		@Override
        public String getReadableString() {
			return this.getValue();
		}
		
		public void setKey(TextKey key) {
			Validate.notNull(key, "The key cannot be null!");
			this.key = key;
		}
		
		public String getValue() {
			return this.value;
		}
		
		public void setValue(String value) {
			Validate.notNull(value, "The value cannot be null!");
			this.value = value;
		}

	}
	
	private static final class ComplexTextTypeComponent extends TextualComponent implements ConfigurationSerializable {
		
		public static ComplexTextTypeComponent deserialize(Map<String, Object> map){
			String key = null;
			Map<String, String> value = new HashMap<String, String>();
			
			for(Map.Entry<String, Object> valEntry : map.entrySet()){
				if(valEntry.getKey().equals("key")){
					key = (String) valEntry.getValue();
				} else if(valEntry.getKey().startsWith("value.")){
					value.put(((String) valEntry.getKey()).substring(6), valEntry.getValue().toString());
				}
			}
			
			return new ComplexTextTypeComponent(TextKey.get(key), value);
		}
		
		private TextKey key;
		private Map<String, String> value;
		
		public ComplexTextTypeComponent(TextKey key, Map<String, String> values){
			setKey(key);
			setValue(values);
		}
		
		@Override
		public TextKey getKey() {
			return this.key;
		}
		
		@Override
		public TextualComponent clone() throws CloneNotSupportedException {
			return new ComplexTextTypeComponent(getKey(), getValue());
		}

		@Override
		public void writeJson(JsonWriter writer) throws IOException {
			writer.name(this.getKey().toString());
			writer.beginObject();
			
			for(Map.Entry<String, String> jsonPair : this.value.entrySet()){
				writer.name(jsonPair.getKey()).value(jsonPair.getValue());
			}
			
			writer.endObject();
		}
		
		@Override
		@SuppressWarnings("serial")
		public Map<String, Object> serialize() {
			return new java.util.HashMap<String, Object>() {{
				put("key", getKey());
				
				for(Map.Entry<String, String> valEntry : ComplexTextTypeComponent.this.getValue().entrySet()){
					put("value." + valEntry.getKey(), valEntry.getValue());
				}
			}};
		}
		
		@Override
		public String getReadableString() {
			return this.getKey().toString();
		}
		
		public void setKey(TextKey key) {
			Validate.notNull(key, "The key cannot be null!");
			this.key = key;
		}
		
		public Map<String, String> getValue() {
			return this.value;
		}
		
		public void setValue(Map<String, String> value) {
			Validate.notNull(value, "The value cannot be null!");
			this.value = value;
		}
		
	}
	
	public static enum TextKey {
		
		TRANSLATION("translate"),
		SCORE("score"),
		RAW_TEXT("text"),
		SELECTOR("selector");
		
		
		private String key;
		
		private TextKey(String key) {
			this.key = key;
		}
		
		@Override
		public String toString() {
			return this.key;
		}
		
		
		public static TextKey get(String key) {
			for (TextKey textKey : TextKey.values()) {
				if (textKey.toString().equals(key)) {
					return textKey;
				}
			}
			
			return null;
		}
		
	}
	
}