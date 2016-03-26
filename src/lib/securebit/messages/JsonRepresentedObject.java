package lib.securebit.messages;

import java.io.IOException;

import com.google.gson.stream.JsonWriter;

public abstract interface JsonRepresentedObject {

	public abstract void writeJson(JsonWriter writer) throws IOException;
	
}