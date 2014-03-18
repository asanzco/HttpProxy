package net.Http;

import java.io.BufferedReader;
import java.io.IOException;

public abstract class HttpHeader {
	
	public void readHeader(BufferedReader br) throws Exception {
		String headerString = getHeaderString(br);
		parseHeader(headerString);
	}
	
	private String getHeaderString(BufferedReader br) throws IOException {
		
		StringBuilder builder = new StringBuilder();
		
		String line = br.readLine();
		while(line != null && line.length() > 0) {
			builder.append(line + "\n");
			line = br.readLine();
		}
		
		return builder.toString();
		
	}
	
	public void parseHeader(String headerString) throws Exception {
		   
		String[] lines = headerString.split("\n");
		processFirstLine(lines[0]);
		for(int i=1; i<lines.length; i++)
			processParameterLine(lines[i]);
	   
	}
	
	protected abstract void processFirstLine(String line) throws Exception;
	
	protected abstract void processParameterLine(String line);
	
	public abstract String toString();
	
}
