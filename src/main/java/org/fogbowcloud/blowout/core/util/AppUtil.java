package org.fogbowcloud.blowout.core.util;

import org.json.JSONObject;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AppUtil {

    public static boolean isStringEmpty(String... values) {
        for (String s : values) {
            if (s == null || s.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public static Map<String, String> toMap(String jsonStr) {
        Map<String, String> newMap = new HashMap<String, String>();
        jsonStr = jsonStr.replace("{", "").replace("}", "");
        String[] blocks = jsonStr.split(",");
        for (int i = 0; i < blocks.length; i++) {
            String block = blocks[i];
            int indexOfCarac = block.indexOf("=");
            if (indexOfCarac < 0) {
                continue;
            }
            String key = block.substring(0, indexOfCarac).trim();
            String value = block.substring(indexOfCarac + 1).trim();
            newMap.put(key, value);
        }
        return newMap;
    }

    public static Map<String, Object> parseJSONStringToMap(String response) throws ScriptException {
        ScriptEngine engine;
        ScriptEngineManager sem = new ScriptEngineManager();
        engine = sem.getEngineByName("javascript");

        final String script = "Java.asJSONCompatible(" + response + ")";
        Object result = engine.eval(script);

        final Map<String, Object> contents = (Map<String, Object>) result;

        return new HashMap<>(contents);
    }

    public static String getValueFromJsonStr(String key, String jsonStr) {
        JSONObject json = new JSONObject(jsonStr);
        String value = json.getString(key);
        return value;
    }

    public static void makeBodyField(JSONObject json, String propKey, String prop) {
        if (prop != null && !prop.isEmpty()) {
            json.put(propKey, prop);
        }
    }

    public static String generateIdentifier() {
        return String.valueOf(UUID.randomUUID());
    }

}
