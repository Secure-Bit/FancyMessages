package lib.securebit.messages;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.Achievement;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import lib.securebit.ReflectionUtil;

public class MessageReflection {
	
	public static final Class<?> CLASS_CHATBASE = ReflectionUtil.getNMSClass("IChatBaseComponent");
	public static final Class<?> CLASS_CHATSERIALIZER = ReflectionUtil.getNMSClass("IChatBaseComponent$ChatSerializer");
	public static final Class<?> CLASS_CRAFTPLAYER = ReflectionUtil.getCraftBukkitClass("entity.CraftPlayer");
	public static final Class<?> CLASS_ENTITYPLAYER = ReflectionUtil.getNMSClass("EntityPlayer");
	public static final Class<?> CLASS_PLAYERCONNECTION = ReflectionUtil.getNMSClass("PlayerConnection");
	public static final Class<?> CLASS_PACKET = ReflectionUtil.getNMSClass("Packet");
	public static final Class<?> CLASS_PACKET_CHAT = ReflectionUtil.getNMSClass("PacketPlayOutChat");
	public static final Class<?> CLASS_CRAFTITEM = ReflectionUtil.getCraftBukkitClass("inventory.CraftItemStack");
	public static final Class<?> CLASS_ITEM = ReflectionUtil.getNMSClass("ItemStack");
	public static final Class<?> CLASS_NBT = ReflectionUtil.getNMSClass("NBTTagCompound");
	public static final Class<?> CLASS_CRAFTSTATISTIC = ReflectionUtil.getCraftBukkitClass("CraftStatistic");
	public static final Class<?> CLASS_ACHIEVEMENT = ReflectionUtil.getNMSClass("Achievement");
	public static final Class<?> CLASS_STATISTIC = ReflectionUtil.getNMSClass("Statistic");
	
	public static final Constructor<?> CONSTRUCTOR_PACKET_CHAT = ReflectionUtil.getConstructor(MessageReflection.CLASS_PACKET_CHAT,
			new Class<?>[] { MessageReflection.CLASS_CHATBASE });
	
	public static final Field FIELD_CONNECTION = ReflectionUtil.getDeclaredField(MessageReflection.CLASS_ENTITYPLAYER, "playerConnection");
	public static final Field FIELD_NAME_ACHIEVEMENT = ReflectionUtil.getField(MessageReflection.CLASS_ACHIEVEMENT, "name");
	public static final Field FIELD_NAME_STATISTIC = ReflectionUtil.getField(MessageReflection.CLASS_STATISTIC, "name");
	
	public static final Method METHOD_A = ReflectionUtil.getMethod(MessageReflection.CLASS_CHATSERIALIZER, "a", new Class<?>[] { String.class });
	public static final Method METHOD_NMS_COPY = ReflectionUtil.getMethod(MessageReflection.CLASS_CRAFTITEM, "asNMSCopy", new Class<?>[] { ItemStack.class });
	public static final Method METHOD_GETHANDLE = ReflectionUtil.getMethod(MessageReflection.CLASS_CRAFTPLAYER, "getHandle", ReflectionUtil.emtyClassArray());
	public static final Method METHOD_SAVE = ReflectionUtil.getMethod(MessageReflection.CLASS_ITEM, "save", new Class<?>[] { MessageReflection.CLASS_NBT });
	public static final Method METHOD_NMS_ACHIEVEMENT = ReflectionUtil.getMethod(MessageReflection.CLASS_CRAFTSTATISTIC, "getNMSAchievement",
			new Class<?>[] { Achievement.class });
	public static final Method METHOD_NMS_STATISTIC = ReflectionUtil.getMethod(MessageReflection.CLASS_CRAFTSTATISTIC, "getNMSStatistic",
			new Class<?>[] { Statistic.class });
	public static final Method METHOD_NMS_STATISTIC_MATERIAL = ReflectionUtil.getMethod(MessageReflection.CLASS_CRAFTSTATISTIC, "getMaterialStatistic",
			new Class<?>[] { Statistic.class, Material.class });
	public static final Method METHOD_NMS_STATISTIC_ENTITY = ReflectionUtil.getMethod(MessageReflection.CLASS_CRAFTSTATISTIC, "getEntityStatistic",
			new Class<?>[] { Statistic.class, EntityType.class });
	public static final Method METHOD_SENDPACKET = ReflectionUtil.getMethod(MessageReflection.CLASS_PLAYERCONNECTION, "sendPacket",
			new Class<?>[] { MessageReflection.CLASS_PACKET });
	
}
