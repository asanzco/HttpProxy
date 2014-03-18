package net.Http;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.util.List;
import java.util.Map;

public class HttpResponse {

	private HttpResponseHeader header;
	private HttpBody body;
	
	public HttpResponseHeader Header() { return this.header; }
	public HttpBody Body() { return this.body; }
	
	public HttpResponse() {
		this.header = new HttpResponseHeader();
		this.body = new HttpBody();
	}
	
	public void readHttpResponse(Socket s) throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
		this.header.readHeader(br);
		this.body.readBody(br, this.header.ContentLength());		
	}
	
	public void readHttpResponse(HttpURLConnection con) throws Exception {
		
		BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
	
		this.body.readBody(br);
		
		this.header.NCode(con.getResponseCode());
		this.header.SCode(con.getResponseMessage());
		
		String headers = "";
		Map<String, List<String>> headerFields = con.getHeaderFields();
		for(String field : headerFields.keySet()) {
			headers += field + ": ";
			List<String> values = headerFields.get(field);
			for(String value : values)
				headers += value + "; ";
			if(values.size() > 0) 
				headers = headers.substring(0, headers.length() - 2);
			headers += "\n";
		}
		
		for(String line : headers.split("\n"))
			this.header.processParameterLine(line);
		
	}
	
	public String toString() {
		return this.header.toString() + "\n" + this.body.toString();
	}
	
}
