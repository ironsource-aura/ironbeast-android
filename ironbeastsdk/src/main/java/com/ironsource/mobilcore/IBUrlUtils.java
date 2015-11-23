package com.ironsource.mobilcore;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Set;

class IBUrlUtils {

	private final static String CHARSET_NAME = "utf-8";

	//=======================================================
	//					Public Methods
	//=======================================================

	public static String generateParamsString(HashMap<String, Object> requestParamsMap) {

		if (requestParamsMap == null) {
			return "";
		}

		Set<String> keySet = requestParamsMap.keySet();
		StringBuilder paramsBuilder = new StringBuilder();

        boolean isFirstParam = true;

		for (String key : keySet) {

			Object value = requestParamsMap.get(key);

			if (value != null) {

				if (value.getClass().isArray()) {

					int length = Array.getLength(value);

					if (length > 0) {

						//append first item
						String valueStr = utf8EncodeStr(Array.get(value, 0)
						                                     .toString());
						paramsBuilder.append(isFirstParam ? "" : "&")
						             .append(key)
						             .append("=")
						             .append(valueStr);

                        isFirstParam = false;

						//append the rest of the items
						for (int i = 1 ; i < length ; i++) {
							valueStr = utf8EncodeStr(Array.get(value, i).toString());
							paramsBuilder.append(",").append(valueStr);
						}
					}

				}
				else {
					String valueStr = utf8EncodeStr(value.toString());
					paramsBuilder.append(isFirstParam ? "" : "&")
					             .append(key)
					             .append("=")
					             .append(valueStr);

                    isFirstParam = false;
				}
			}
		}

		return paramsBuilder.toString();
	}

	public static String utf8EncodeStr(String str) {
		try {
			return URLEncoder.encode(str, CHARSET_NAME);
		} catch (UnsupportedEncodingException e) {
			return str;
		}
	}

}
