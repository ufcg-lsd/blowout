package org.fogbowcloud.blowout.core.util;

import java.util.HashMap;
import java.util.Map;

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
			String value = block.substring(indexOfCarac + 1, block.length()).trim();
			newMap.put(key, value);
		}
		return newMap;
	}

}
