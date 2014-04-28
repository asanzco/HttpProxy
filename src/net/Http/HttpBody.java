package net.Http;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Socket;

public class HttpBody {

	private static final int BUFFER_SIZE = 1024 * 4;
	
	private ByteArrayOutputStream baos;
	
	public byte[] ContentByte() { return this.baos.toByteArray(); }
	public void Content(byte[] content) throws IOException { 
		this.baos.reset();
		this.baos.write(content);
	}
	public String ContentString() { return this.baos.toString(); }
	
	public HttpBody() {
		baos = new ByteArrayOutputStream();
	}
	
	public void readBody(Socket s) throws IOException {
		readBody(new BufferedInputStream(s.getInputStream()));
	}
	
	public void readBody(HttpURLConnection con) throws IOException {
		readBody(new BufferedInputStream(con.getInputStream()));
	}
	
	public void readBody(BufferedInputStream bis) throws IOException {
		
		byte[] buffer = new byte[BUFFER_SIZE];
		int nread = 0;
		
		while(bis.available() > 0) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
			nread = bis.read(buffer);
			if(nread <= 0) break;
			baos.write(buffer, 0, nread);
		}
	}
	
	public String toString() {
		return ContentString();
	}
	
	public byte[] toByteArray() {
		return ContentByte();
	}
	
}
