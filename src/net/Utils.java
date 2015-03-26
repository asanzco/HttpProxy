package net;

import java.io.IOException;
import java.io.StringReader;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.ccnx.ccn.protocol.ContentName;
import org.ccnx.ccn.protocol.Interest;
import org.ccnx.ccn.protocol.MalformedContentNameStringException;
import org.springframework.security.crypto.codec.Base64;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class Utils {
	
	public static Interest parseInterest(ContentName prefix, HttpRequest httpRequest) throws MalformedContentNameStringException, IOException {
				
		XStream xStream = new XStream(new DomDriver("UTF-8"));
		String requestXml = xStream.toXML(httpRequest).replace("&#x0;", "");
		
		String requestBase64 = new String(Base64.encode(requestXml.getBytes()));
		String requestEncoded = requestBase64.replace("=", "/1").replace("+", "/2");
		
		Interest interest = new Interest(prefix + requestEncoded);
		interest.answerOriginKind(0); 
		
		return interest;
		
	}

	public static HttpRequest parseHttpRequest(ContentName prefix, Interest interest) {
		
		String requestEncoded = interest.name().toString().substring(prefix.toString().length());
		String requestBase64 = requestEncoded.replace("/1", "=").replace("/2", "+");
		
		String requestXml = new String(Base64.decode(requestBase64.getBytes())).replace("&#x0;", "");
		
		XStream xStream = new XStream(new DomDriver("UTF-8"));
		Object pack = xStream.fromXML(new StringReader(requestXml));
		
		return (HttpRequest)pack;
		
	}
	
	public static String encodeResponse(HttpResponse httpResponse) {
		
		XStream xStream = new XStream(new DomDriver("UTF-8"));
		String responseXml = xStream.toXML(httpResponse).replace("&#x0;", "");
		
		String responseBase64 = new String(Base64.encode(responseXml.getBytes()));
		String responseEncoded = responseBase64.replace("=", "/1").replace("+", "/2");

		return responseEncoded;
		
	}

	public static HttpResponse decodeResponse(String response) {
		
		String responseBase64 = response.replace("/1", "=").replace("/2", "+");
		
		String requestXml = new String(Base64.decode(responseBase64.getBytes())).replace("&#x0;", "");
		
		XStream xStream = new XStream(new DomDriver("UTF-8"));
		Object request = xStream.fromXML(new StringReader(requestXml));
		
		return (HttpResponse)request;
		
	}
	
	public static HttpUriRequest parseHttpUriRequest(HttpRequest httpRequest) {
		
		HttpUriRequest httpUriRequest = null;
		
		String method = httpRequest.getRequestLine().getMethod();

		if (method.equalsIgnoreCase("GET"))
			httpUriRequest = new HttpGet(httpRequest.getRequestLine().getUri());
		
		if (httpUriRequest != null)
			httpUriRequest.setHeaders(httpRequest.getAllHeaders());
		
		return httpUriRequest;
	}

}

