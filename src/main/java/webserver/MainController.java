package webserver;

public class MainController implements Controller {

	@Override
	public void service(HttpRequest request, HttpResponse response) {
		// TODO Auto-generated method stub
		if (request.getMethod() == HttpMethod.POST) {
			doPost(request, response);
		}
		else {
			doGet(request, response);
		}
	}
	
	public void doPost(HttpRequest request, HttpResponse response) {
		
	}
	
	public void doGet(HttpRequest request, HttpResponse response) {
		
	}

}
