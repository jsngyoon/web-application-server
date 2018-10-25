package webserver;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpResponse {
	private static final Logger log = LoggerFactory.getLogger(HttpResponse.class);
	private Map<String, String> headers = new HashMap<String, String>();
	private DataOutputStream dos = null;
	
	public HttpResponse(OutputStream out) {
		// TODO Auto-generated constructor stub
		dos = new DataOutputStream(out);
	}
	
	public void addHeader(String key, String value) {
		headers.put(key,  value);
	}
	
	public void forward(String url) {
		log.info("forward({})", url);
		try {
			byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
			log.info("body length {}", body.length);
			if (url.endsWith(".css")) {
				headers.put("Content-Type", "text/css");
			}
			else if (url.endsWith(".js")) {
				headers.put("Content-Type",  "application/javascript");
			}
			else {
				headers.put("Content-Type",  "text/html;charset=utf-8");
			}
			headers.put("Content-Length", body.length + "");
			response200header();
			responseBody(body);
		}
		catch (IOException e) {
			log.error(e.getMessage());
		}
	}
	
	public void forwardBody(String data) {
		log.info("forwardBody()");
		byte[] body = data.getBytes();
		headers.put("Content-Type",  "text/html;charset=utf-8");
		headers.put("Content-Length", body.length + "");			
		response200header();
		responseBody(body);		
	}
	
	public void redirect(String url) {
		log.info("redirect()");
		try {
			dos.writeBytes("HTTP/1.1 302 Found \r\n");
			processHeaders();
			dos.writeBytes("Location: " + url + "\r\n");			
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		
	}
	
	private void response200header() {
		// TODO Auto-generated method stub
		log.info("response200header()");
		try {
			dos.writeBytes("HTTP/1.1 200 OK \r\n");
			processHeaders();
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}		
	}
	
	private void responseBody(byte[] body) {
		log.info("responseBody()");
		// TODO Auto-generated method stub
		try {
			dos.write(body, 0, body.length);
			dos.writeBytes("\r\n");
			dos.flush();
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		
	}

	private void processHeaders() {
		// TODO Auto-generated method stub
		log.info("processHeaders()");
		try {
			Set<String> keys = headers.keySet();
			for(String key : keys) {
				dos.writeBytes(key + ": " + headers.get(key) + "\r\n");
				log.info("{}: {}",key, headers.get(key));
			}
		}
		catch (IOException e) {
			log.error(e.getMessage());
		}
		
	}
}
