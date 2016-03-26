package lib.securebit.messages.events;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Achievement;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.Statistic.Type;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;

import lib.securebit.ReflectionUtil;
import lib.securebit.messages.FancyMessage;
import lib.securebit.messages.JsonRepresentedObject;
import lib.securebit.messages.JsonString;
import lib.securebit.messages.MessagePart;
import lib.securebit.messages.MessageReflection;
import lib.securebit.messages.TextualComponent;
import lib.securebit.messages.TextualComponent.TextKey;

public class HoverEvent implements JsonRepresentedObject {
	
	public static HoverEvent tooltip(String text) {
		return new HoverEvent(HoverEventType.SHOW_TEXT, new JsonString(text));
	}
	
	public static HoverEvent tooltip(List<String> lines) {
		return HoverEvent.tooltip(lines.toArray(new String[lines.size()]));
	}
	
	public static HoverEvent tooltip(String... lines) {
		StringBuilder builder = new StringBuilder();
		
		for (int i = 0; i < lines.length; i++){
			builder.append(lines[i]);
			if (i != lines.length - 1){
				builder.append('\n');
			}
		}
		
		return HoverEvent.tooltip(builder.toString());
	}
	
	public static HoverEvent formatedTooltip(FancyMessage text) {
		return new HoverEvent(HoverEventType.SHOW_TEXT, text);
	}
	
	public static HoverEvent formatedTooltip(List<FancyMessage> lines) {
		return HoverEvent.formatedTooltip(lines.toArray(new FancyMessage[lines.size()]));
	}
	
	public static HoverEvent formatedTooltip(FancyMessage... lines) {
		if (lines.length < 1){
			return null;
		}

		FancyMessage result = new FancyMessage();
		result.getMessageParts().clear();

		for (int i = 0; i < lines.length; i++){
			try {
				for (MessagePart component : lines[i]){
					if (component.getClickEvent() != null){
						throw new IllegalArgumentException("The tooltip text cannot have clickevent.");
					} else if (component.getHoverEvent() != null){
						throw new IllegalArgumentException("The tooltip text cannot have hoverevent.");
					}
					
					if (component.hasText()){
						result.getMessageParts().add(component.clone());
					}
				}
				
				if (i != lines.length - 1){
					result.getMessageParts().add(new MessagePart(TextualComponent.newText(TextKey.RAW_TEXT, "\n")));
				}
			} catch (CloneNotSupportedException e) {
				Bukkit.getLogger().log(Level.WARNING, "Failed to clone object", e);
				return null;
			}
		} 
		
		return HoverEvent.formatedTooltip(result.getMessageParts().isEmpty() ? null : result);
	}
	
	public static HoverEvent itemTooltip(String itemJSON) {
		return new HoverEvent(HoverEventType.SHOW_ITEM, new JsonString(itemJSON));
	}
	
	public static HoverEvent itemTooltip(ItemStack item) {
		Object nmsItem = ReflectionUtil.createStaticObject(MessageReflection.METHOD_NMS_COPY, item);
		Object nbtTag = ReflectionUtil.createObject(MessageReflection.CLASS_NBT);
		
		return HoverEvent.itemTooltip(ReflectionUtil.createObject(MessageReflection.METHOD_SAVE, nmsItem, nbtTag).toString());
	}
	
	public static HoverEvent achievementTooltip(String name) {
		return new HoverEvent(HoverEventType.SHOW_ACHIEVEMENT, new JsonString("achievement." + name));
	}
	
	public static HoverEvent achievementTooltip(Achievement achievement) {
		Object nmsAchievement = ReflectionUtil.createStaticObject(MessageReflection.METHOD_NMS_ACHIEVEMENT, achievement);
		Object name = ReflectionUtil.createObject(MessageReflection.FIELD_NAME_ACHIEVEMENT, nmsAchievement);
		
		return HoverEvent.achievementTooltip((String) name);
	}
	
	public static HoverEvent statisticTooltip(Statistic statistic) {
		Type type = statistic.getType();
		
		if (type != Type.UNTYPED) {
			throw new IllegalArgumentException("That statistic requires an additional " + type + " parameter!");
		}
		
		return HoverEvent.toEvent(ReflectionUtil.createStaticObject(MessageReflection.METHOD_NMS_STATISTIC, statistic));
	}
	
	public static HoverEvent statisticTooltip(Statistic statistic, Material material) {
		Type type = statistic.getType();
		
		if (type == Type.UNTYPED) {
			throw new IllegalArgumentException("That statistic needs no additional parameter!");
		}
		
		if ((type == Type.BLOCK && material.isBlock()) || type == Type.ENTITY) {
			throw new IllegalArgumentException("Wrong parameter type for that statistic - needs " + type + "!");
		}
		
		return HoverEvent.toEvent(ReflectionUtil.createStaticObject(MessageReflection.METHOD_NMS_STATISTIC_MATERIAL, statistic, material));
	}
	
	public static HoverEvent statisticTooltip(Statistic statistic, EntityType entityType) {
		Type type = statistic.getType();
		
		if (type == Type.UNTYPED) {
			throw new IllegalArgumentException("That statistic needs no additional parameter!");
		}
		if (type != Type.ENTITY) {
			throw new IllegalArgumentException("Wrong parameter type for that statistic - needs " + type + "!");
		}
		
		return HoverEvent.toEvent(ReflectionUtil.createStaticObject(MessageReflection.METHOD_NMS_STATISTIC_ENTITY, statistic, entityType));
	}
	
	public static HoverEvent deserialize(JsonObject obj) {
		String action = obj.get("action").getAsString();
		JsonRepresentedObject value = null;
		
		if (obj.get("value").isJsonPrimitive()) {
			value = new JsonString(obj.get("value").getAsString());
		} else {
			value = FancyMessage.deserialize(obj.get("value").toString());
		}
		
		return new HoverEvent(HoverEventType.get(action), value);
	}
	
	private static HoverEvent toEvent(Object nmsStatistic) {
		return HoverEvent.achievementTooltip((String) ReflectionUtil.createObject(MessageReflection.FIELD_NAME_STATISTIC, nmsStatistic));
	}
	
	
	private HoverEventType type;
	private JsonRepresentedObject value;
	
	public HoverEvent(HoverEventType type, JsonRepresentedObject value) {
		this.type = type;
		this.value = value;
	}
	
	@Override
	public void writeJson(JsonWriter writer) throws IOException {
		writer.beginObject();
		writer.name("action").value(this.type.toString());
		writer.name("value");
		
		this.value.writeJson(writer);
		
		writer.endObject();
	}
	
	public HoverEventType getType() {
		return this.type;
	}
	
	public JsonRepresentedObject getValue() {
		return this.value;
	}
	
	public static enum HoverEventType {
		
		SHOW_TEXT("show_text"),
		SHOW_ACHIEVEMENT("show_achievement"),
		SHOW_ITEM("show_item");
		
		
		private String str;
		
		private HoverEventType(String str) {
			this.str = str;
		}
		
		@Override
		public String toString() {
			return this.str;
		}
		
		
		public static HoverEventType get(String str) {
			for (HoverEventType type : HoverEventType.values()) {
				if (type.toString().equals(str)) {
					return type;
				}
			}
			
			return null;
		}
		
	}

}
