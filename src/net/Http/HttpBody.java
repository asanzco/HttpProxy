package net.Http;

import java.io.BufferedReader;
import java.io.IOException;

public class HttpBody {

	private String content = "";
	
	public String Content() { return this.content.trim(); }
	public void Content(String value) { this.content = value.trim(); }
	
	public void readBody(BufferedReader br) throws IOException {
		Content(getBodyString(br));
	}
	
	public void readBody(BufferedReader br, int contentLength) throws IOException {
		Content(getBodyString(br, contentLength));
	}
	
	private String getBodyString(BufferedReader br) throws IOException {
		
		StringBuilder builder = new StringBuilder();
		
		String line = br.readLine();
		while(line != null) {
			builder.append(line + "\n");
			line = br.readLine();
		}
		
		return builder.toString();
		
	}
	
	private String getBodyString(BufferedReader br,  int contentLength) throws IOException {
		
		String result = "";
		
		if(contentLength > 0) {
			StringBuilder builder = new StringBuilder();
			int nr = 0;
			String line = "";
			while(nr < contentLength) {
				line = br.readLine();
				builder.append(line + "\n");
				nr += line.length();
			}
			result = builder.toString();
		}
					
		return result;
	}
	
	public String toString() {
		return Content();
	}
	
}
