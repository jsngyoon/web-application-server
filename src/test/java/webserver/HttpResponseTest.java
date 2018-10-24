package webserver;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.junit.Test;

public class HttpResponseTest {
	private String testDir = "./src/test/resources/";

	@Test
	public void responseForward() throws Exception {
		HttpResponse response = new HttpResponse(CreateOutputStream("Http_Forward.txt"));
		response.forward("/index.html");		
	}

	@Test
	public void responseRedirect() throws Exception {
		HttpResponse response = new HttpResponse(CreateOutputStream("Http_Redirect.txt"));
		response.redirect("/index.html");		
	}
	
	@Test
	public void responseCookies() throws Exception {
		HttpResponse response = new HttpResponse(CreateOutputStream("Http_Cookies.txt"));
		response.addHeader("Set-Cookie", "logined=true");
		response.redirect("/index.html");
	}
	
	private OutputStream CreateOutputStream(String name) throws FileNotFoundException {
		// TODO Auto-generated method stub
		return new FileOutputStream(new File(testDir + name));
	}

}
