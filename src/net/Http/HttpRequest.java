package net.Http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;


public class HttpRequest {
	
	private HttpRequestHeader header;
	private HttpBody body;
	
	public HttpRequestHeader Header() { return this.header; }
	public HttpBody Body() { return this.body; }
	
	public HttpRequest() {
		this.header = new HttpRequestHeader();
		this.body = new HttpBody();
	}
	
	public void readHttpRequest(Socket s) throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
		this.header.readHeader(br);
		this.body.readBody(br, this.header.ContentLength());		
	}
	
	public String toString() {
		return this.header.toString() + "\n" + this.body.toString();
	}
	
	public boolean isAccepted(String fileType) {
		return this.header.Accept().contains(fileType);
	}
	
	public boolean isAcceptedCharset(String charset) {
		return this.header.AcceptCharset().contains(charset);
	}
	
	public boolean hasCookies() {
		return !(this.header.Cookie().equals(""));
	}
	
	public HttpURLConnection SendRequest() throws IOException {
		
		URL obj = new URL(this.header.Path());
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		con.setRequestMethod(this.header.Method().toString());

		return con;
		
	}
	
}
