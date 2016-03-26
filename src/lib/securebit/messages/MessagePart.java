package lib.securebit.messages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.gson.stream.JsonWriter;

import lib.securebit.messages.events.ClickEvent;
import lib.securebit.messages.events.ClickEvent.ClickEventType;
import lib.securebit.messages.events.HoverEvent;
import lib.securebit.messages.events.HoverEvent.HoverEventType;
import lib.securebit.messages.events.ShiftClickEvent;

public final class MessagePart implements JsonRepresentedObject, ConfigurationSerializable, Cloneable {
	
	public static final BiMap<ChatColor, String> stylesToNames;

	static {
		ConfigurationSerialization.registerClass(MessagePart.class);
		
		ImmutableBiMap.Builder<ChatColor, String> builder = ImmutableBiMap.builder();
		
		for (final ChatColor style : ChatColor.values()){
			if (!style.isFormat()){
				continue;
			}

			String styleName;
			
			switch (style) {
			case MAGIC:
				styleName = "obfuscated";
				break;
			case UNDERLINE:
				styleName = "underlined";
				break;
			default:
				styleName = style.name().toLowerCase();
				break;
			}
			
			builder.put(style, styleName);
		}
		
		stylesToNames = builder.build();
	}
	
	
	private ChatColor color = ChatColor.WHITE;
	private List<ChatColor> styles = new ArrayList<ChatColor>();
	
	private TextualComponent text = null;
	
	private ClickEvent clickEvent = null;
	private HoverEvent hoverEvent = null;
	private ShiftClickEvent shiftClickEvent = null;
	
	private List<JsonRepresentedObject> translationReplacements = new ArrayList<JsonRepresentedObject>();
	
	public MessagePart() {
		this(null);
	}
	
	public MessagePart(final TextualComponent text){
		this.text = text;
	}

	public boolean hasText() {
		return this.text != null;
	}
	
	@Override
	public MessagePart clone() throws CloneNotSupportedException{
		MessagePart obj = (MessagePart) super.clone();
		
		obj.styles = FancyMessage.cloneList(this.styles);
		obj.hoverEvent = new HoverEvent(this.hoverEvent.getType(), this.hoverEvent.getValue());
		obj.clickEvent = new ClickEvent(this.clickEvent.getType(), this.clickEvent.getValue());
		obj.shiftClickEvent = new ShiftClickEvent(this.shiftClickEvent.getValue());
		obj.translationReplacements = FancyMessage.cloneList(this.translationReplacements);
		
		return obj;

	}

	public void writeJson(JsonWriter json) {
		try {
			json.beginObject();
			
			this.text.writeJson(json);
			
			json.name("color").value(this.color.name().toLowerCase());
			
			for (final ChatColor style : this.styles) {
				json.name(stylesToNames.get(style)).value(true);
			}
			
			if (this.clickEvent != null) {
				json.name("clickEvent");
				this.clickEvent.writeJson(json);
			}
			
			if (this.hoverEvent != null) {
				json.name("hoverEvent");
				this.hoverEvent.writeJson(json);
			}
			
			if (this.shiftClickEvent != null){
				json.name("insertion");
				this.shiftClickEvent.writeJson(json);
			}
			
			if (this.translationReplacements.size() > 0 && this.text != null && TextualComponent.isTranslatableText(this.text)){
				json.name("with").beginArray();
				
				for(JsonRepresentedObject obj : this.translationReplacements){
					obj.writeJson(json);
				}
				
				json.endArray();
			}
			
			json.endObject();
		} catch (IOException e){
			Bukkit.getLogger().log(Level.WARNING, "A problem occured during writing of JSON string", e);
		}
	}

	public Map<String, Object> serialize() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("text", this.text);
		map.put("styles", this.styles);
		map.put("color", this.color.getChar());
		map.put("hoverActionName", this.hoverEvent.getType().toString());
		map.put("hoverActionData", this.hoverEvent.getValue());
		map.put("clickActionName", this.clickEvent.getType().toString());
		map.put("clickActionData", this.clickEvent.getValue());
		map.put("insertion", this.shiftClickEvent.getValue());
		map.put("translationReplacements", this.translationReplacements);
		return map;
	}
	
	public void setClickEvent(ClickEvent clickEvent) {
		this.clickEvent = clickEvent;
	}
	
	public ClickEvent getClickEvent() {
		return this.clickEvent;
	}
	
	public void setHoverEvent(HoverEvent hoverEvent) {
		this.hoverEvent = hoverEvent;
	}
	
	public HoverEvent getHoverEvent() {
		return this.hoverEvent;
	}
	
	public void setShiftClickEvent(ShiftClickEvent shiftClickEvent) {
		this.shiftClickEvent = shiftClickEvent;
	}
	
	public ShiftClickEvent getShiftClickEvent() {
		return this.shiftClickEvent;
	}
	
	public void setColor(ChatColor color) {
		this.color = color;
	}
	
	public ChatColor getColor() {
		return this.color;
	}
	
	public void setStyles(List<ChatColor> styles) {
		this.styles = styles;
	}
	
	public List<ChatColor> getStyles() {
		return this.styles;
	}
	
	public void setText(TextualComponent text) {
		this.text = text;
	}
	
	public TextualComponent getText() {
		return this.text;
	}
	
	public void setTranslationReplacements(ArrayList<JsonRepresentedObject> translationReplacements) {
		this.translationReplacements = translationReplacements;
	}
	
	public List<JsonRepresentedObject> getTranslationReplacements() {
		return this.translationReplacements;
	}
	
	@SuppressWarnings("unchecked")
	public static MessagePart deserialize(Map<String, Object> serialized){
		MessagePart part = new MessagePart((TextualComponent) serialized.get("text"));
		
		part.styles = (ArrayList<ChatColor>) serialized.get("styles");
		part.color = ChatColor.getByChar(serialized.get("color").toString());
		part.clickEvent = new ClickEvent(ClickEventType.get((String) serialized.get("clickActionName")), (String) serialized.get("clickActionData"));
		part.hoverEvent = new HoverEvent(HoverEventType.get((String) serialized.get("hoverActionName")), (JsonRepresentedObject) serialized.get("hoverActionData"));
		part.shiftClickEvent = new ShiftClickEvent((String) serialized.get("insertion"));
		part.translationReplacements = (ArrayList<JsonRepresentedObject>)serialized.get("translationReplacements");
		
		return part;
	}
	
}