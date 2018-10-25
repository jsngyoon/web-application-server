package webserver;

import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.User;
import util.HttpRequestUtils;

public class ListController extends MainController {
	private static final Logger log = LoggerFactory.getLogger(ListController.class);

	public void doGet(HttpRequest request, HttpResponse response) {
		String cookies = request.getHeader("Cookie");
		Map<String, String> cookieMap = HttpRequestUtils.parseCookies(cookies);
		Boolean logined = Boolean.parseBoolean(cookieMap.get("logined"));
		if (logined) {
			log.info("List request accepted");
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
			String data = sb.toString();
			response.forwardBody(data);			
		}
		else {
			log.info("Must be logined first to get list");
			response.redirect("/user/login.html");
		}
	}

}
