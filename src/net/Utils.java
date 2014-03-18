package net;

import net.Http.HttpRequest;
import net.Http.HttpResponse;

import org.ccnx.ccn.protocol.Component;
import org.ccnx.ccn.protocol.ContentName;
import org.ccnx.ccn.protocol.ContentObject;
import org.ccnx.ccn.protocol.Interest;
import org.ccnx.ccn.protocol.MalformedContentNameStringException;

public class Utils {
	
	public static Interest parseInterest(ContentName prefix, HttpRequest httpRequest) throws MalformedContentNameStringException {
		
		String path = httpRequest.Header().Path();
		if (path.startsWith("http://")) path = path.substring(7);
		if (path.startsWith("https://")) path = path.substring(8);
		path = "/" + path;
		
		String method = httpRequest.Header().Method().toString();
		
		Interest interest = new Interest(prefix + method + path);
		interest.answerOriginKind(0);
		
		return interest;
		
	}

	public static HttpRequest parseHttpRequest(ContentName prefix, Interest interest) {
		
		HttpRequest httpRequest = new HttpRequest();
		
		String aux = interest.name().toString().substring(prefix.toString().length());
		String[] auxSplited = aux.split("/", 2);
		
		httpRequest.Header().Method(auxSplited[0]);
		httpRequest.Header().Path("http://" + auxSplited[1]);
				
		return httpRequest;
		
	}
	
	public static HttpResponse parseHttpRespose(String httpResponseString) throws Exception {
		
		HttpResponse httpResponse = new HttpResponse();
		
		String[] splited = httpResponseString.split("\n");
	
		int i = 0;
		
		StringBuilder builderHeader = new StringBuilder();
		for(;!splited[i].equals(""); i++)
			builderHeader.append(splited[i] + "\n");
		httpResponse.Header().parseHeader(builderHeader.toString());
		
		StringBuilder builderBody = new StringBuilder();
		for(; i < splited.length; i++)
			builderBody.append(splited[i] + "\n");
		httpResponse.Body().Content(builderBody.toString());
		
		return httpResponse;

	}
	
	public static HttpResponse parseHttpResponse(ContentObject contentObject) throws Exception {
		
		HttpResponse httpResponse = new HttpResponse();
		
		String[] contentSplited = Component.printNative(contentObject.content()).split("\n");
	
		int i = 0;
		
		StringBuilder builderHeader = new StringBuilder();
		for(;!contentSplited[i].equals(""); i++)
			builderHeader.append(contentSplited[i] + "\n");
		httpResponse.Header().parseHeader(builderHeader.toString());
		
		StringBuilder builderBody = new StringBuilder();
		for(; i < contentSplited.length; i++)
			builderBody.append(contentSplited[i] + "\n");
		httpResponse.Body().Content(builderBody.toString());
		
		return httpResponse;
		
	}
	
	public static HttpResponse appendContentHttpResponse(ContentObject contentObject, HttpResponse httpResponse) {
		
		String newContent = Component.printNative(contentObject.content());
		
		httpResponse.Body().Content(httpResponse.Body().Content() + newContent);
		
		return httpResponse;
		
	}
}

