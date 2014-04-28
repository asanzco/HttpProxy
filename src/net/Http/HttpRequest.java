package net.Http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.util.Arrays;


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
		this.header.readHeader(s);
		this.body.readBody(s);		
	}
	
	@Override
	public String toString() {
		
		String request = this.header.toString();
		if(this.body.toString().length() > 0) request += "\n" + this.body.toString();
		
		return request;
	}
	
	public byte[] toByteArray() throws IOException {
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		baos.write(this.header.toByteArray());
		if(this.body.toByteArray().length > 0) {
			baos.write("\n".getBytes());
			baos.write(this.body.toByteArray());
		}
		
		return baos.toByteArray();
	}
	
	public boolean hasCookies() {
		return !(this.header.Cookie().equals(""));
	}
	
	public void parseRequest(byte[] request) throws Exception {
		
		String requestString = new String(request);
		
		String[] lines = requestString.split("\n");
		StringBuilder sb = new StringBuilder();
		for(String line : lines) {
			if(line.equals("")) break;
			sb.append(line + "\n");
		}
		
		String headerString = sb.toString();
		this.header.parseHeader(headerString);

		byte[] header = headerString.getBytes();
		if(request.length > header.length) {
			byte[] body = Arrays.copyOfRange(request, header.length, request.length);
			this.body.Content(body);
		}
		
	}
	
	public HttpURLConnection sendRequest() throws IOException {
		
		System.out.println(this.header.toString());
		System.out.println(this.header.Path());
		URL obj = new URL(this.header.Path());
		System.out.println("B");
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
System.out.println("C");
		System.out.println(this.header.Method().toString());
		con.setRequestMethod(this.header.Method().toString());
		System.out.println("ME");
		for(HttpHeaderProperty property : this.header.getProperties()) {
			System.out.println(property);
			con.setRequestProperty(property.Key(), property.Value());
		}
System.out.println("SENT");
		return con;
		
	}
	
}
