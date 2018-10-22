package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.User;
import util.HttpRequestUtils;
import util.IOUtils;

public class RequestHandler extends Thread {
	private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

	private Socket connection;

	public RequestHandler(Socket connectionSocket) {
		this.connection = connectionSocket;
	}

	public void run() {
		log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
				connection.getPort());

		try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
			// TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.

			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			DataOutputStream dos = new DataOutputStream(out);
			String line = br.readLine();
			log.info("Request line : {}", line);
			if (line == null)
				return;
			String[] tokens = line.split(" ");
			/*
			 * while(!line.equals("")) { line = br.readLine(); log.info("Header : {}",
			 * line); }
			 */
			int contentLength = 0;
			String hostName = null;
			String url = tokens[1];
			Boolean logined = false;
		
			while (!line.equals("")) { // Header 처리
				line = br.readLine();
				tokens = line.split(" ");
				if (tokens[0].equals("Content-Length:"))
					contentLength = Integer.parseInt(tokens[1]);
				if (tokens[0].equals("Host:"))
					hostName = tokens[1];
				if (tokens[0].equals("Cookie:")) {
					Map<String, String> cookieMap = HttpRequestUtils.parseCookies(line.substring(8));
					logined = Boolean.parseBoolean(cookieMap.get("logined"));
				}
				log.info("Header : {}", line);
			}
						
			line = IOUtils.readData(br, contentLength);
			log.info("Body : {}", line);
			Map<String, String> map = HttpRequestUtils.parseQueryString(line);
			
			if ("/user/create".startsWith(url)) {
				if (url.contains("?")) { // GET 회원가입
					String params = url.substring(url.indexOf("?") + 1);
					map = HttpRequestUtils.parseQueryString(params);
				}					
				User user = new User(map.get("userId"), map.get("password"), map.get("name"), map.get("email"));
				DataBase.addUser(user);
				log.info("User info : {}", user);
				response302Header(dos, hostName, "/index.html", false);				
			}
			else if ("/user/login".equals(url)) {
				User user = DataBase.findUserById(map.get("userId"));
				if (user != null && user.getPassword().equals(map.get("password"))) {
					log.info("Login Success !!");
					response302Header(dos, hostName, "/index.html", true);
				}
				else response302Header(dos, hostName, "/user/login_failed.html", false);
			}
			else if ("/user/list".equals(url)) {
				if (logined) {
					Collection<User> userList = DataBase.findAll();
					StringBuilder sb = new StringBuilder();
					sb.append("<table border='1'>");
					for (User user : userList) {
						sb.append("<tr>");
						sb.append("<td>" + user.getUserId() + "</td>");
						sb.append("<td>" + user.getName() + "</td>");
						sb.append("<td>" + user.getEmail() + "</td>");
						sb.append("</tr>");						
					}
					sb.append("</table>");
					byte[] body = sb.toString().getBytes();
					response200Header(dos, body.length, url);
					responseBody(dos, body);
				}
				else {
					resourceResponse(dos, "login.html");
				}
			}
		
			resourceResponse(dos, url);

		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private void resourceResponse(DataOutputStream dos, String url) throws IOException {
		byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
		response200Header(dos, body.length, url);
		responseBody(dos, body);
	}

	private void response200Header(DataOutputStream dos, int lengthOfBodyContent, String url) {
		try {
			dos.writeBytes("HTTP/1.1 200 OK \r\n");
			if (url.endsWith(".css")) dos.writeBytes("Content-Type: text/css\r\n");
			else dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
			dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private void response302Header(DataOutputStream dos, String hostName, String fileName, Boolean logined) {
		try {
			dos.writeBytes("HTTP/1.1 302 Found \r\n");
			dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
			dos.writeBytes("Location: http://" + hostName + fileName + "\r\n");
			if (logined)
				dos.writeBytes("Set-Cookie: logined=true\r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private void responseBody(DataOutputStream dos, byte[] body) {
		try {
			dos.write(body, 0, body.length);
			dos.flush();
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
}
