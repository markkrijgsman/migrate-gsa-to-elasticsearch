package nl.luminis.blog.gsa.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class JsonUtil {

    public static String pretty(String json) {
        JsonElement query = new JsonParser().parse(json);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(query);
    }
}

