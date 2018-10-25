package webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.User;

public class LoginController extends MainController {
	private static final Logger log = LoggerFactory.getLogger(LoginController.class);

	public void doPost(HttpRequest request, HttpResponse response) {
		User user = DataBase.findUserById(request.getParameter("userId"));
		if (user != null && user.getPassword().equals(request.getParameter("password"))) {
			log.info("Login Success !!");
			response.addHeader("Set-Cookie", "logined=true");
			response.redirect("/index.html");
		}
		else response.redirect("/user/login_failed.html");
	}

}
