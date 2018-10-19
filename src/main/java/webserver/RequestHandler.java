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
			if (tokens[0].equals("POST")) { // POST 회원가입
				while (!line.equals("")) {
					line = br.readLine();
					tokens = line.split(" ");
					if (tokens[0].equals("Content-Length:"))
						contentLength = Integer.parseInt(tokens[1]);
					if (tokens[0].equals("Host:"))
						hostName = tokens[1];
					if (tokens[0].equals("Cookie")) {
						Map<String, String> cookieMap = HttpRequestUtils.parseCookies(line.substring(8));
						logined = Boolean.parseBoolean(cookieMap.get("logined"));
					}
					log.info("Header : {}", line);
				}
				
				// br.readLine();
				line = IOUtils.readData(br, contentLength);
				log.info("Body : {}", line);
				Map<String, String> map = HttpRequestUtils.parseQueryString(line);
				if ("/user/create".equals(url)) {
					User user = new User(map.get("userID"), map.get("password"), map.get("name"), map.get("email"));
					DataBase.addUser(user);
					log.info("User info : {}", user);
					response302Header(dos, hostName, "/index.html", false);
				}
				else if ("/user/login".equals(url)) {
					User user = DataBase.findUserById(map.get("userID"));
					if (user != null && user.getPassword().equals(map.get("password")))
						response302Header(dos, hostName, "/index.html", true);
					else response302Header(dos, hostName, "/user/login_failed.html", false);
				}				
			}
			if (tokens[0].equals("GET")) {
				if (tokens[1].contains("?")) { // GET 회원가입
					String params = tokens[1].substring(tokens[1].indexOf("?") + 1);
					Map<String, String> map = HttpRequestUtils.parseQueryString(params);
					User user = new User(map.get("userID"), map.get("password"), map.get("name"), map.get("email"));
					log.info("User info : {}", user);
				}
				else if (tokens[1].equals("/user/list")) {
					
				}
				else { // webapp html file 처리
					byte[] body = Files.readAllBytes(new File("./webapp" + tokens[1]).toPath());
					response200Header(dos, body.length);
					responseBody(dos, body);
				}
			}
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
		try {
			dos.writeBytes("HTTP/1.1 200 OK \r\n");
			dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
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
