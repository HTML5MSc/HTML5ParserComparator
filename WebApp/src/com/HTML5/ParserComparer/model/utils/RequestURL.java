package com.HTML5.ParserComparer.model.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class RequestURL {

	public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0";
	public static final int CONNECT_TIMEOUT = 10000;

	public static InputStream getResponse(String urlString) throws IOException {
		if (urlString == null || urlString.isEmpty()) {
			return null;
		}

		if (!urlString.startsWith("http://"))
			urlString = "http://".concat(urlString);
		URL url = new URL(urlString);
		URLConnection urlConnection = url.openConnection();
		urlConnection.addRequestProperty("User-Agent", USER_AGENT);
		urlConnection.setConnectTimeout(CONNECT_TIMEOUT);
		InputStream inputStream = urlConnection.getInputStream();
		return inputStream;
	}
}