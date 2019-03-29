package org.fogbowcloud.blowout.helpers;

import com.google.gson.Gson;

public class TestsUtils {

	private static final Gson gson = new Gson();

	public static boolean isJSONValid(String jsonInString) {
		try {
			gson.fromJson(jsonInString, Object.class);
			return true;
		} catch(com.google.gson.JsonSyntaxException ex) {
			System.out.println(ex);
			return false;
		}
	}
}
