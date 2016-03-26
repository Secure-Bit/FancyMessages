package lib.securebit.messages;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;

import lib.securebit.ReflectionUtil;
import lib.securebit.messages.TextualComponent.TextKey;
import lib.securebit.messages.events.ClickEvent;
import lib.securebit.messages.events.HoverEvent;
import lib.securebit.messages.events.ShiftClickEvent;

public class FancyMessage implements JsonRepresentedObject, Cloneable, Iterable<MessagePart>, ConfigurationSerializable {
	
	static {
		ConfigurationSerialization.registerClass(FancyMessage.class);
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Object> List<T> cloneList(List<T> list) {
		return (List<T>) Arrays.asList(list.toArray());
	}
	
	public static FancyMessage deserialize(String json) {
		JsonObject serialized = new JsonParser().parse(json).getAsJsonObject();
		JsonArray extra = serialized.getAsJsonArray("extra");
		
		FancyMessage returnVal = new FancyMessage();
		returnVal.messageParts.clear();
		
		for (JsonElement mPrt : extra) {
			MessagePart component = new MessagePart();
			JsonObject messagePart = mPrt.getAsJsonObject();
			
			for (Map.Entry<String, JsonElement> entry : messagePart.entrySet()) {
				if (TextualComponent.isTextKey(entry.getKey())) {
					Map<String, Object> serializedMapForm = new HashMap<String, Object>();
					serializedMapForm.put("key", entry.getKey());
					
					if (entry.getValue().isJsonPrimitive()) {
						serializedMapForm.put("value", entry.getValue().getAsString());
					} else {
						for (Map.Entry<String, JsonElement> compositeNestedElement : entry.getValue().getAsJsonObject().entrySet()) {
							serializedMapForm.put("value." + compositeNestedElement.getKey(),compositeNestedElement.getValue().getAsString());
						}
					}
					
					component.setText(TextualComponent.deserialize(serializedMapForm));
				} else if (MessagePart.stylesToNames.inverse().containsKey(entry.getKey())) {
					if (entry.getValue().getAsBoolean()) {
						component.getStyles().add(MessagePart.stylesToNames.inverse().get(entry.getKey()));
					}
				} else if (entry.getKey().equals("color")) {
					component.setColor(ChatColor.valueOf(entry.getValue().getAsString().toUpperCase()));
				} else if (entry.getKey().equals("clickEvent")) {
					JsonObject object = entry.getValue().getAsJsonObject();
					component.setClickEvent(ClickEvent.deserialize(object));
				} else if (entry.getKey().equals("hoverEvent")) {
					JsonObject object = entry.getValue().getAsJsonObject();
					component.setHoverEvent(HoverEvent.deserialize(object));
				} else if (entry.getKey().equals("insertion")) {
					component.setShiftClickEvent(new ShiftClickEvent(entry.getValue().getAsString()));
				} else if (entry.getKey().equals("with")) {
					for (JsonElement object : entry.getValue().getAsJsonArray()) {
						if (object.isJsonPrimitive()) {
							component.getTranslationReplacements().add(new JsonString(object.getAsString()));
						} else {
							component.getTranslationReplacements().add(deserialize(object.toString()));
						}
					}
				}
			}
			
			returnVal.messageParts.add(component);
		}
		
		return returnVal;
	}
	
	@SuppressWarnings("unchecked")
	public static FancyMessage deserialize(Map<String, Object> serialized){
		FancyMessage msg = new FancyMessage();
		msg.messageParts = (List<MessagePart>) serialized.get("messageParts");
		msg.jsonString = serialized.containsKey("JSON") ? serialized.get("JSON").toString() : null;
		msg.dirty = !serialized.containsKey("JSON");
		return msg;
	}
	
	
	private List<MessagePart> messageParts;
	private String jsonString;
	private boolean dirty;

	public FancyMessage(String firstPartText) {
		this(TextualComponent.newText(TextKey.RAW_TEXT, firstPartText));
	}

	public FancyMessage(TextualComponent firstPartText) {
		this.messageParts = new ArrayList<MessagePart>();
		this.messageParts.add(new MessagePart(firstPartText));
		this.jsonString = null;
		this.dirty = false;
	}

	public FancyMessage() {
		this((TextualComponent) null);
	}
	
	@Override
	public FancyMessage clone() throws CloneNotSupportedException {
		FancyMessage instance = (FancyMessage) super.clone();
		instance.messageParts = new ArrayList<MessagePart>(messageParts.size());
		
		for (int i = 0; i < messageParts.size(); i++) {
			instance.messageParts.add(i, messageParts.get(i).clone());
		}
		
		instance.dirty = false;
		instance.jsonString = null;
		return instance;
	}
	
	@Override
	public Map<String, Object> serialize() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("messageParts", this.messageParts);
		return map;
	}

	@Override
	public Iterator<MessagePart> iterator() {
		return this.messageParts.iterator();
	}
	
	@Override
	public void writeJson(JsonWriter writer) throws IOException{
		if (this.messageParts.size() == 1) {
			latest().writeJson(writer);
		} else {
			writer.beginObject().name("text").value("").name("extra").beginArray();
			
			for (final MessagePart part : this) {
				part.writeJson(writer);
			}
			
			writer.endArray().endObject();
		}
	}
	
	public List<MessagePart> getMessageParts() {
		return this.messageParts;
	}
	
	public FancyMessage text(String text) {
		MessagePart latest = this.latest();
		latest.setText(TextualComponent.newText(TextKey.RAW_TEXT, text));
		
		this.dirty = true;
		return this;
	}

	public FancyMessage text(TextualComponent text) {
		MessagePart latest = this.latest();
		latest.setText(text);
		
		this.dirty = true;
		return this;
	}

	public FancyMessage color(ChatColor color) {
		if (!color.isColor()) {
			throw new IllegalArgumentException(color.name() + " is not a color");
		}
		
		this.latest().setColor(color);
		this.dirty = true;
		return this;
	}

	public FancyMessage style(ChatColor... styles) {
		for (final ChatColor style : styles) {
			if (!style.isFormat()) {
				throw new IllegalArgumentException(style.name() + " is not a style");
			}
		}
		
		this.latest().setStyles(Arrays.asList(styles));
		this.dirty = true;
		return this;
	}
	
	public FancyMessage translationReplacements(String... replacements){
		for (String str : replacements){
			latest().getTranslationReplacements().add(new JsonString(str));
		}
		
		this.dirty = true;
		return this;
	}
	
	public FancyMessage translationReplacements(FancyMessage... replacements){
		for (FancyMessage str : replacements){
			latest().getTranslationReplacements().add(str);
		}
		
		this.dirty = true;
		return this;
	}
	
	public FancyMessage translationReplacements(List<FancyMessage> replacements){		
		return translationReplacements(replacements.toArray(new FancyMessage[replacements.size()]));
	}
	
	public FancyMessage then(String text) {
		return then(TextualComponent.newText(TextKey.RAW_TEXT, text));
	}

	public FancyMessage then(TextualComponent text) {
		if (!latest().hasText()) {
			throw new IllegalStateException("previous message part has no text");
		}
		
		this.messageParts.add(new MessagePart(text));
		this.dirty = true;
		return this;
	}

	public FancyMessage then() {
		if (!latest().hasText()) {
			throw new IllegalStateException("previous message part has no text");
		}
		
		this.messageParts.add(new MessagePart());
		this.dirty = true;
		return this;
	}

	public String toJSONString() {
		if (!this.dirty && this.jsonString != null) {
			return this.jsonString;
		}
		
		StringWriter string = new StringWriter();
		JsonWriter json = new JsonWriter(string);
		
		try {
			writeJson(json);
			json.close();
		} catch (IOException e) {
			throw new RuntimeException("invalid message");
		}
		
		this.jsonString = string.toString();
		this.dirty = false;
		return this.jsonString;
	}

	public void send(Player player){
		this.send((CommandSender) player);
	}
	
	public void send(CommandSender sender) {
		this.send(sender, this.toJSONString());
	}

	public void send(Iterable<? extends CommandSender> senders) {
		for (CommandSender sender : senders) {
			this.send(sender);
		}
	}
        
	public String toOldMessageFormat() {
		StringBuilder result = new StringBuilder();
		
		for (MessagePart part : this) {
			result.append(part.getColor() == null ? "" : part.getColor());
			
			for (ChatColor formatSpecifier : part.getStyles()){
				result.append(formatSpecifier);
			}
			
			result.append(part.getText());
		}
		
		return result.toString();
	}
	
	public FancyMessage onClick(ClickEvent event) {
		MessagePart latest = latest();
		latest.setClickEvent(event);
		this.dirty = true;
		
		return this;
	}

	public FancyMessage onHover(HoverEvent event) {
		MessagePart latest = latest();
		latest.setHoverEvent(event);
		this.dirty = true;
		
		return this;
	}
	
	public FancyMessage onShiftClick(ShiftClickEvent event) {
		MessagePart latest = latest();
		latest.setShiftClickEvent(event);
		this.dirty = true;
		
		return this;
	}

	private MessagePart latest() {
		return this.messageParts.get(this.messageParts.size() - 1);
	}

	private void send(CommandSender sender, String jsonString) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(this.toOldMessageFormat());
			return;
		}
		
		Player player = (Player) sender;
		
		Object nmsPlayer = ReflectionUtil.createObject(MessageReflection.METHOD_GETHANDLE, player, new Object[0]);
		Object playerCon = ReflectionUtil.createObject(MessageReflection.FIELD_CONNECTION, nmsPlayer);
		
		ReflectionUtil.invokeMethod(MessageReflection.METHOD_SENDPACKET, playerCon, this.createChatPacket(jsonString));
	}

	private Object createChatPacket(String json) {
		return ReflectionUtil.createObject(MessageReflection.CONSTRUCTOR_PACKET_CHAT, ReflectionUtil.createStaticObject(MessageReflection.METHOD_A, json));
	}
	
}