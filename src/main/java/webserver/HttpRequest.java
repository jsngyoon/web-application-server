package webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.HttpRequestUtils;
import util.IOUtils;

public class HttpRequest {
	private static final Logger log = LoggerFactory.getLogger(HttpRequest.class);
	private Map<String, String> headers = new HashMap<String, String>();
	private Map<String, String> params = new HashMap<String, String>();
	private RequestLine request;
	
	public HttpRequest(InputStream in) {
		// TODO Auto-generated constructor stub
		try(BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));) {
			String line = br.readLine();
			if (line == null) return;
			request = new RequestLine(line);
			
			line = br.readLine();
			while (!line.equals("")) { 
				log.info("Header : {}", line);
				String[] tokens = line.split(": ");
				headers.put(tokens[0].trim(), tokens[1].trim());
				line = br.readLine();
			}
			if (request.getMethod() == HttpMethod.POST) {
				line = IOUtils.readData(br, Integer.parseInt(headers.get("Content-Length")));
				log.info("Body : {}", line);
				params = HttpRequestUtils.parseQueryString(line);
			}
			else params = request.getParams();
		}
		catch (IOException e) {
			log.error(e.getMessage());
		}
	}
	
	public String getPath() {
		return request.getPath();
	}
	
	public HttpMethod getMethod() {
		return request.getMethod();
	}
	
	public String getParameter(String key) {
		return params.get(key);
	}
	
	public String getHeader(String key) {
		return headers.get(key);
	}

}
