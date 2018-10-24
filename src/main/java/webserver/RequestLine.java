package webserver;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.HttpRequestUtils;

public class RequestLine {
	private static final Logger log = LoggerFactory.getLogger(RequestLine.class);
	private HttpMethod method;
	private String path;
	private Map<String, String> params = new HashMap<String, String>();
	
	public RequestLine(String requestLine) {
		// TODO Auto-generated constructor stub
		log.info("Request : {}", requestLine);
		
		String[] tokens = requestLine.split(" ");
		method = HttpMethod.valueOf(tokens[0]);
		path = tokens[1];
		if (method == HttpMethod.POST) return;
		
		if (tokens[1].contains("?")) { 
			int index = tokens[1].indexOf("?");
			path = tokens[1].substring(0, index);
			params = HttpRequestUtils.parseQueryString(tokens[1].substring(index + 1));
		}
	}
	
	public String getPath() {
		return path;
	}
	
	public HttpMethod getMethod() {
		return method;
	}
	
	public Map<String, String> getParams() {
		return params;
	}
}
