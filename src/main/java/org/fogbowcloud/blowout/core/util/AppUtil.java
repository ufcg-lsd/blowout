package org.fogbowcloud.blowout.core.util;

import org.json.JSONObject;

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

	public static void makeBodyField(JSONObject json, String propKey, String prop) {
		if (prop != null && !prop.isEmpty()) {
			json.put(propKey, prop);
		}
	}

	public static String generateRandomIdentifier() {
		return String.valueOf(UUID.randomUUID());
	}

}
