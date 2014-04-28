package net.Http;

public class HttpRequestHeader extends HttpHeader {
	
	public enum Method {
		OPTIONS, GET, HEAD, POST, PUT, DELETE, TRACE, CONNECT, PATCH, UNKNOWN
	}
		
	private Method method = Method.UNKNOWN;
	private String path = "";
	
	private int httpVersion = 1;
	private int httpSubVersion = 0;
	
	private String accept = "";
	private String acceptCharset = "";
	private String acceptDatetime = "";
	private String acceptEncoding = "";
	private String acceptLanguage = "";
	private String authorization = "";
	private String cacheControl = "";
	private String connection = "";
	private String cookie = "";
	private int contentLength = -1;
	private String contentMD5 = "";
	private String contentType = "";
	private String date = "";
	private String expect = "";
	private String from = "";
	private String host = "";
	private String ifMatch = "";
	private String ifModifiedSince = "";
	private String ifNoneMatch = "";
	private String ifRange = "";
	private String ifUnmodifiedSince = "";
	private int maxForwards = -1;
	private String origin = "";
	private String pragma = "";
	private String proxyAuthorization = "";
	private String range= "";
	private String referer= "";
	private String te= "";
	private String userAgent= "";
	private String via= "";
	private String warning= "";
	
	public Method Method() { return this.method; }
	public void Method(Method value) { this.method = value; }
	public void Method(String value) { this.method = getMethod(value); }
	public String Path() { return this.path; }
	public void Path(String value) { this.path = value; }

	public String Protocol() {
		return "HTTP/" + this.httpVersion + "." + this.httpSubVersion;
	}
	public void HttpVersion(int value) { this.httpVersion = value; }
	public void HttpSubVersion(int value) { this.httpSubVersion = value; }
	
	public String Accept() { return this.accept; }
	public void Accept(String value) { this.accept = value.trim(); }
	public String AcceptCharset() { return this.acceptCharset; }
	public void AcceptCharset(String value) { this.acceptCharset = value.trim(); }
	public String AcceptDatetime() { return this.acceptDatetime; }
	public void AcceptDatetime(String value) { this.acceptDatetime = value.trim(); }
	public String AcceptEncoding() { return this.acceptEncoding; }
	public void AcceptEncoding(String value) { this.acceptEncoding = value.trim(); }
	public String AcceptLanguage() { return this.acceptLanguage; }
	public void AcceptLanguage(String value) { this.acceptLanguage = value.trim(); }
	public String Authorization() { return this.authorization; }
	public void Authorization(String value) { this.authorization = value.trim(); }
	public String CacheControl() { return this.cacheControl; }
	public void CacheControl(String value) { this.cacheControl = value.trim(); }
	public String Connection() { return this.connection; }
	public void Connection(String value) { this.connection = value.trim(); }
	public String Cookie() { return this.cookie; }
	public void Cookie(String value) { this.cookie = value.trim(); }
	public int ContentLength() { return this.contentLength; }
	public void ContentLength(int value) { this.contentLength = value; }
	public String ContentMD5() { return this.contentMD5; }
	public void ContentMD5(String value) { this.contentMD5 = value.trim(); }
	public String ContentType() { return this.contentType; }
	public void ContentType(String value) { this.contentType = value.trim(); }
	public String Date() { return this.date; }
	public void Date(String value) { this.date = value.trim(); }
	public String Expect() { return this.expect; }
	public void Expect(String value) { this.expect = value.trim(); }
	public String From() { return this.from; }
	public void From(String value) { this.from = value.trim(); }
	public String Host() { return this.host; }
	public void Host(String value) { this.host = value.trim(); }
	public String IfMatch() { return this.ifMatch; }
	public void IfMatch(String value) { this.ifMatch = value.trim(); }
	public String IfModifiedSince() { return this.ifModifiedSince; }
	public void IfModifiedSince(String value) { this.ifModifiedSince = value.trim(); }
	public String IfNoneMatch() { return this.ifNoneMatch; }
	public void IfNoneMatch(String value) { this.ifNoneMatch = value.trim(); }
	public String IfRange() { return this.ifRange; }
	public void IfRange(String value) { this.ifRange = value.trim(); }
	public String IfUnmodifiedSince() { return this.ifUnmodifiedSince; }
	public void IfUnmodifiedSince(String value) { this.ifUnmodifiedSince = value.trim(); }
	public int MaxForwards() { return this.maxForwards; }
	public void MaxForwards(int value) { this.maxForwards = value; }
	public String Origin() { return this.origin; }
	public void Origin(String value) { this.origin = value.trim(); }
	public String Pragma() { return this.pragma; }
	public void Pragma(String value) { this.pragma = value.trim(); }
	public String ProxyAuthorization() { return this.proxyAuthorization; }
	public void ProxyAuthorization(String value) { this.proxyAuthorization = value.trim(); }
	public String Range() { return this.range; }
	public void Range(String value) { this.range = value.trim(); }
	public String Referer() { return this.referer; }
	public void Referer(String value) { this.referer = value.trim(); }
	public String Te() { return this.te; }
	public void Te(String value) { this.te = value.trim(); }
	public String UserAgent() { return this.userAgent; }
	public void UserAgent(String value) { this.userAgent = value.trim(); }
	public String Via() { return this.via; }
	public void Via(String value) { this.via = value.trim(); }
	public String Warning() { return this.warning; }
	public void Warning(String value) { this.warning = value.trim(); }
	
	@Override
	protected void processStatusLine(String line) throws Exception {
		
		String[] fields = line.trim().split(" ");
		
		String protocol = "";
		
		if (fields.length == 3) {
			Method(fields[0]);
			Path(fields[1]);
			protocol = fields[2];
		}
		
		if(!(protocol.equals("HTTP/1.1") || protocol.equals("HTTP/1.0")))
			throw new Exception("Not a valid HTTP request");
		
		HttpVersion(Integer.parseInt(protocol.split("/")[1].split("\\.")[0]));
		HttpSubVersion(Integer.parseInt(protocol.split("/")[1].split("\\.")[1]));
		
	}
	
	private Method getMethod(String methodString) {
		
		Method method = Method.UNKNOWN;
		
		if(methodString.toUpperCase().equals("OPTIONS")) 
			method = Method.OPTIONS;
		else if(methodString.toUpperCase().equals("GET")) 
			method = Method.GET;
		else if(methodString.toUpperCase().equals("HEAD")) 
			method = Method.HEAD;
		else if(methodString.toUpperCase().equals("POST")) 
			method = Method.POST;
		else if(methodString.toUpperCase().equals("PUT")) 
			method = Method.PUT;
		else if(methodString.toUpperCase().equals("DELETE")) 
			method = Method.DELETE;
		else if(methodString.toUpperCase().equals("TRACE")) 
			method = Method.TRACE;
		else if(methodString.toUpperCase().equals("CONNECT")) 
			method = Method.CONNECT;
		else if(methodString.toUpperCase().equals("PATCH")) 
			method = Method.PATCH;
		
		return method;
		
	}
	
	@Override
	protected void processParameterLine(String line) {
		
		String[] fields = line.trim().split(":", 2);
		if(fields.length == 2) {
			if(fields[0].toLowerCase().equals("accept"))
				Accept(fields[1]);
			else if(fields[0].toLowerCase().equals("accept-charset"))
				AcceptCharset(fields[1]);
			else if(fields[0].toLowerCase().equals("accept-encoding"))
				AcceptEncoding(fields[1]);
			else if(fields[0].toLowerCase().equals("accept-language"))
				AcceptLanguage(fields[1]);
			else if(fields[0].toLowerCase().equals("accept-datetime"))
				AcceptDatetime(fields[1]);
			else if(fields[0].toLowerCase().equals("authorization"))
				Authorization(fields[1]);
			else if(fields[0].toLowerCase().equals("cache-control"))
				CacheControl(fields[1]);
			else if(fields[0].toLowerCase().equals("connection"))
				Connection(fields[1]);
			else if(fields[0].toLowerCase().equals("cookie"))
				Cookie(fields[1]);
			else if(fields[0].toLowerCase().equals("content-length"))
				ContentLength(Integer.parseInt(fields[1]));
			else if(fields[0].toLowerCase().equals("content-md5"))
				ContentMD5(fields[1]);
			else if(fields[0].toLowerCase().equals("content-type"))
				ContentType(fields[1]);
			else if(fields[0].toLowerCase().equals("date"))
				Date(fields[1]);
			else if(fields[0].toLowerCase().equals("expect"))
				Expect(fields[1]);
			else if(fields[0].toLowerCase().equals("from"))
				From(fields[1]);
			else if(fields[0].toLowerCase().equals("host"))
				Host(fields[1]);
			else if(fields[0].toLowerCase().equals("if-match"))
				IfMatch(fields[1]);
			else if(fields[0].toLowerCase().equals("if-modified-since"))
				IfModifiedSince(fields[1]);
			else if(fields[0].toLowerCase().equals("if-none-match"))
				IfNoneMatch(fields[1]);
			else if(fields[0].toLowerCase().equals("if-range"))
				IfRange(fields[1]);
			else if(fields[0].toLowerCase().equals("if-unmodified-since"))
				IfUnmodifiedSince(fields[1]);
			else if(fields[0].toLowerCase().equals("max-forwards"))
				MaxForwards(Integer.parseInt(fields[1]));
			else if(fields[0].toLowerCase().equals("origin"))
				Origin(fields[1]);
			else if(fields[0].toLowerCase().equals("pragma"))
				Pragma(fields[1]);
			else if(fields[0].toLowerCase().equals("proxy-authorization"))
				ProxyAuthorization(fields[1]);
			else if(fields[0].toLowerCase().equals("range"))
				Range(fields[1]);
			else if(fields[0].toLowerCase().equals("referer"))
				Referer(fields[1]);
			else if(fields[0].toLowerCase().equals("te"))
				Te(fields[1]);
			else if(fields[0].toLowerCase().equals("user-agent"))
				UserAgent(fields[1]);
			else if(fields[0].toLowerCase().equals("via"))
				Via(fields[1]);
			else if(fields[0].toLowerCase().equals("warning"))
				Warning(fields[1]);
		}
		
	}
	
	@Override
	public String getHeaderString() {
		
		String s = "";
		
		if(!Accept().isEmpty()) s += "Accept: " + Accept() + "\n";
		if(!AcceptCharset().isEmpty()) s += "Accept-Charset: " + AcceptCharset() + "\n";
		if(!AcceptDatetime().isEmpty()) s += "Accept-Datetime: " + AcceptDatetime() + "\n";
		if(!AcceptEncoding().isEmpty()) s += "Accept-Encoding: " + AcceptEncoding() + "\n";
		if(!AcceptLanguage().isEmpty()) s += "Accept-Language: " + AcceptLanguage() + "\n";
		if(!Authorization().isEmpty()) s += "Authorization: " + Authorization() + "\n";
		//if(!CacheControl().isEmpty()) s += "Cache-Control: " + CacheControl() + "\n";
		//if(!Connection().isEmpty()) s += "Connection: " + Connection() + "\n";
		if(ContentLength() >= 0) s += "Content-Length: " + ContentLength() + "\n";
		if(!ContentMD5().isEmpty()) s += "Content-MD5: " + ContentMD5() + "\n";
		if(!ContentType().isEmpty()) s += "Content-Type: " + ContentType() + "\n";
		if(!Cookie().isEmpty()) s += "Cookie: " + Cookie() + "\n";
		if(!Date().isEmpty()) s += "Date: " + Date() + "\n";
		if(!Expect().isEmpty()) s += "Expect: " + Expect() + "\n";
		if(!From().isEmpty()) s += "From: " + From() + "\n";
		if(!Host().isEmpty()) s += "Host: " + Host() + "\n";
		if(!IfMatch().isEmpty()) s += "If-Match: " + IfMatch() + "\n";
		if(!IfModifiedSince().isEmpty()) s += "If-Modified-Since: " + IfModifiedSince() + "\n";
		if(!IfNoneMatch().isEmpty()) s += "If-None-Match: " + IfNoneMatch() + "\n";
		if(!IfRange().isEmpty()) s += "If-Range: " + IfRange() + "\n";
		if(!IfUnmodifiedSince().isEmpty()) s += "If-Unmodified-Since: " + IfUnmodifiedSince() + "\n";
		if(MaxForwards() >= 0) s += "Max-Forwards: " + MaxForwards() + "\n";
		if(!Origin().isEmpty()) s += "Origin: " + Origin() + "\n";
		if(!Pragma().isEmpty()) s += "Pragma: " + Pragma() + "\n";
		if(!ProxyAuthorization().isEmpty()) s += "Proxy-Authorization: " + ProxyAuthorization() + "\n";
		if(!Range().isEmpty()) s += "Range: " + Range() + "\n";
		if(!Referer().isEmpty()) s += "Referer: " + Referer() + "\n";
		if(!Te().isEmpty()) s += "TE: " + Te() + "\n";
		if(!UserAgent().isEmpty()) s += "User-Agent: " + UserAgent() + "\n";
		if(!Via().isEmpty()) s += "Via: " + Via() + "\n";
		if(!Warning().isEmpty()) s += "Warning: " + Warning() + "\n";
		
		return s;
		
	}
	
	@Override
	public String toString() {
		
		String s = "";
		
		s += Method() + " " + Path() + " " + Protocol() + "\n";
		s += getHeaderString();
		
		return s;
		
	}
	
	@Override
	public byte[] toByteArray() {
		return toString().getBytes();
	}

}
