package webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.User;

public class CreateController extends MainController {
	private static final Logger log = LoggerFactory.getLogger(CreateController.class);
	
	@Override
	public void doGet(HttpRequest request, HttpResponse response) {
		User user = new User(request.getParameter("userId"), 
				             request.getParameter("password"), 
				             request.getParameter("name"), 
				             request.getParameter("email"));
		DataBase.addUser(user);
		log.info("Created user info : {}", user);
		response.redirect("/index.html");
	}
}
