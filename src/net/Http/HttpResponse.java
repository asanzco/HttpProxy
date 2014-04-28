package net.Http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
		this.header.readHeader(s);
		this.body.readBody(s);
	}
	
	public void readHttpResponse(HttpURLConnection con) throws Exception {

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
		
		try {
			this.body.readBody(con);
		} catch (IOException e) {
		}

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
}
