package webserver;

import java.util.HashMap;
import java.util.Map;

public class ControllerMapper {
	private static Map<String, Controller> map = new HashMap<String, Controller>();
	
	static {
		map.put("/user/create", new CreateController());
		map.put("/user/login", new LoginController());
		map.put("/user/list", new ListController());
	}
	
	public Controller getController(String url) {
		return map.get(url);
	}
}
