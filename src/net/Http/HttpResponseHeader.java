package net.Http;

public class HttpResponseHeader extends HttpHeader {
	
	private int httpVersion = 1;
	private int httpSubVersion = 1;
	
	private int nCode = 200;
	private String sCode = "OK";
	
	private String accessControlAllowOrigin = "";
	private String acceptRanges = "";
	private int age = -1;
	private String allow = "";
	private String cacheControl = "";
	private String connection = "";
	private String contentEncoding = "";
	private String contentLanguage = "";
	private int contentLength = -1;
	private String contentLocation = "";
	private String contentMD5 = "";
	private String contentDisposition = "";
	private String contentRange = ""; 
	private String contentType = "";
	private String date = "";
	private String eTag = "";
	private String expires = "";
	private String lastModified = "";
	private String link = "";
	private String location = "";
	private String p3p = "";
	private String pragma = "";
	private String proxyAuthenticate = "";
	private String refresh = "";
	private String retryAfter = "";
	private String server = "";
	private String setCookie = "";
	private String status = "";
	private String strictTransportSecurity = "";
	private String trailer = "";
	private String transferEncoding = "";
	private String upgrade = "";
	private String vary = "";
	private String via = "";
	private String warning = "";
	private String wwwAuthenticate = "";
	private String xFrameOptions = "";
	
	public String Protocol() {
		return "HTTP/" + this.httpVersion + "." + this.httpSubVersion;
	}
	public void HttpVersion(int value) { this.httpVersion = value; }
	public void HttpSubVersion(int value) { this.httpSubVersion = value; }
	
	public int NCode() { return this.nCode; }
	public void NCode(int value) { this.nCode = value; }
	public String SCode() { return this.sCode.trim(); }
	public void SCode(String value) { this.sCode = value; }
	
	public String AccessControlAllowOrigin() { return this.accessControlAllowOrigin; }
	public void AccessControlAllowOrigin(String value) { this.accessControlAllowOrigin = value.trim(); }
	public String AcceptRanges() { return this.acceptRanges; }
	public void AcceptRanges(String value) { this.acceptRanges = value.trim(); }
	public int Age() { return this.age; }
	public void Age(int value) { this.age = value; }
	public String Allow() { return this.allow; }
	public void Allow(String value) { this.allow = value.trim(); }
	public String CacheControl() { return this.cacheControl; }
	public void CacheControl(String value) { this.cacheControl = value.trim(); }
	public String Connection() { return this.connection; }
	public void Connection(String value) { this.connection = value.trim(); }
	public String ContentEncoding() { return this.contentEncoding; }
	public void ContentEncoding(String value) { this.contentEncoding = value.trim(); }
	public String ContentLanguage() { return this.contentLanguage; }
	public void ContentLanguage(String value) { this.contentLanguage = value.trim(); }
	public int ContentLength() { return this.contentLength; }
	public void ContentLength(int value) { this.contentLength = value; }
	public String ContentLocation() { return this.contentLocation; }
	public void ContentLocation(String value) { this.contentLocation = value.trim(); }
	public String ContentMD5() { return this.contentMD5; }
	public void ContentMD5(String value) { this.contentMD5 = value.trim(); }
	public String ContentDisposition() { return this.contentDisposition; }
	public void ContentDisposition(String value) { this.contentDisposition = value.trim(); }
	public String ContentRange() { return this.contentRange; }
	public void ContentRange(String value) { this.contentRange = value.trim(); }
	public String ContentType() { return this.contentType; }
	public void ContentType(String value) { this.contentType = value.trim(); }
	public String Date() { return this.date; }
	public void Date(String value) { this.date = value.trim(); }
	public String ETag() { return this.eTag; }
	public void ETag(String value) { this.eTag = value.trim(); }
	public String Expires() { return this.expires; }
	public void Expires(String value) { this.expires = value.trim(); }
	public String LastModified() { return this.lastModified; }
	public void LastModified(String value) { this.lastModified = value.trim(); }
	public String Link() { return this.link; }
	public void Link(String value) { this.link = value.trim(); }
	public String Location() { return this.location; }
	public void Location(String value) { this.location = value.trim(); }
	public String P3p() { return this.p3p; }
	public void P3p(String value) { this.p3p = value.trim(); }
	public String Pragma() { return this.pragma; }
	public void Pragma(String value) { this.pragma = value.trim(); }
	public String ProxyAuthenticate() { return this.proxyAuthenticate; }
	public void ProxyAuthenticate(String value) { this.proxyAuthenticate = value.trim(); }
	public String Refresh() { return this.refresh; }
	public void Refresh(String value) { this.refresh = value.trim(); }
	public String RetryAfter() { return this.retryAfter; }
	public void RetryAfter(String value) { this.retryAfter = value.trim(); }
	public String Server() { return this.server; }
	public void Server(String value) { this.server = value.trim(); }
	public String SetCookie() { return this.setCookie; }
	public void SetCookie(String value) { this.setCookie = value.trim(); }
	public String Status() { return this.status; }
	public void Status(String value) { this.status = value.trim(); }
	public String StrictTransportSecurity() { return this.strictTransportSecurity; }
	public void StrictTransportSecurity(String value) { this.strictTransportSecurity = value.trim(); }
	public String Trailer() { return this.trailer; }
	public void Trailer(String value) { this.trailer = value.trim(); }
	public String TransferEncoding() { return this.transferEncoding; }
	public void TransferEncoding(String value) { this.transferEncoding = value.trim(); }
	public String Upgrade() { return this.upgrade; }
	public void Upgrade(String value) { this.upgrade = value.trim(); }
	public String Vary() { return this.vary; }
	public void Vary(String value) { this.vary = value.trim(); }
	public String Via() { return this.via; }
	public void Via(String value) { this.via = value.trim(); }
	public String Warning() { return this.warning; }
	public void Warning(String value) { this.warning = value.trim(); }
	public String WwwAuthenticate() { return this.wwwAuthenticate; }
	public void WwwAuthenticate(String value) { this.wwwAuthenticate = value.trim(); }
	public String XFrameOptions() { return this.xFrameOptions; }
	public void XFrameOptions(String value) { this.xFrameOptions = value.trim(); }

	@Override
	protected void processStatusLine(String line) throws Exception {
		
		String[] fields = line.trim().split(" ");
		
		String protocol = "";
		
		if (fields.length == 3) {
			protocol = fields[0];
			NCode(Integer.parseInt(fields[1].trim()));
			SCode(fields[2]);
		}
		
		if(!(protocol.equals("HTTP/1.1") || protocol.equals("HTTP/1.0")))
			throw new Exception("Not a valid HTTP response");
		
		HttpVersion(Integer.parseInt(protocol.split("/")[1].split("\\.")[0].trim()));
		HttpSubVersion(Integer.parseInt(protocol.split("/")[1].split("\\.")[1].trim()));
		
	}

	@Override
	protected void processParameterLine(String line) {
	
		String[] fields = line.trim().split(":", 2);
		if(fields.length == 2) {
			if(fields[0].toLowerCase().equals("access-control-allow-origin"))
				AccessControlAllowOrigin(fields[1]);
			else if(fields[0].toLowerCase().equals("accept-ranges"))
				AcceptRanges(fields[1]);
			else if(fields[0].toLowerCase().equals("age"))
				Age(Integer.parseInt(fields[1].trim()));
			else if(fields[0].toLowerCase().equals("allow"))
				Allow(fields[1]);
			else if(fields[0].toLowerCase().equals("cache-control"))
				CacheControl(fields[1]);
			else if(fields[0].toLowerCase().equals("connection"))
				Connection(fields[1]);
			else if(fields[0].toLowerCase().equals("content-encoding"))
				ContentEncoding(fields[1]);
			else if(fields[0].toLowerCase().equals("content-language"))
				ContentLanguage(fields[1]);
			else if(fields[0].toLowerCase().equals("content-length"))
				ContentLength(Integer.parseInt(fields[1].trim()));
			else if(fields[0].toLowerCase().equals("content-location"))
				ContentLocation(fields[1]);
			else if(fields[0].toLowerCase().equals("content-md5"))
				ContentMD5(fields[1]);
			else if(fields[0].toLowerCase().equals("content-disposition"))
				ContentDisposition(fields[1]);
			else if(fields[0].toLowerCase().equals("content-range"))
				ContentRange(fields[1]);
			else if(fields[0].toLowerCase().equals("content-type"))
				ContentType(fields[1]);
			else if(fields[0].toLowerCase().equals("date"))
				Date(fields[1]);
			else if(fields[0].toLowerCase().equals("etag"))
				ETag(fields[1]);
			else if(fields[0].toLowerCase().equals("expires"))
				Expires(fields[1]);
			else if(fields[0].toLowerCase().equals("last-modified"))
				LastModified(fields[1]);
			else if(fields[0].toLowerCase().equals("link"))
				Link(fields[1]);
			else if(fields[0].toLowerCase().equals("location"))
				Location(fields[1]);
			else if(fields[0].toLowerCase().equals("p3p"))
				P3p(fields[1]);
			else if(fields[0].toLowerCase().equals("pragma"))
				Pragma(fields[1]);
			else if(fields[0].toLowerCase().equals("proxy-authenticate"))
				ProxyAuthenticate(fields[1]);
			else if(fields[0].toLowerCase().equals("refresh"))
				Refresh(fields[1]);
			else if(fields[0].toLowerCase().equals("retry-after"))
				RetryAfter(fields[1]);
			else if(fields[0].toLowerCase().equals("server"))
				Server(fields[1]);
			else if(fields[0].toLowerCase().equals("set-cookie"))
				SetCookie(fields[1]);
			else if(fields[0].toLowerCase().equals("status"))
				Status(fields[1]);
			else if(fields[0].toLowerCase().equals("strict-transport-security"))
				StrictTransportSecurity(fields[1]);
			else if(fields[0].toLowerCase().equals("trailer"))
				Trailer(fields[1]);
			else if(fields[0].toLowerCase().equals("transfer-encoding"))
				TransferEncoding(fields[1]);
			else if(fields[0].toLowerCase().equals("upgrade"))
				Upgrade(fields[1]);
			else if(fields[0].toLowerCase().equals("vary"))
				Vary(fields[1]);
			else if(fields[0].toLowerCase().equals("via"))
				Via(fields[1]);
			else if(fields[0].toLowerCase().equals("warning"))
				Warning(fields[1]);
			else if(fields[0].toLowerCase().equals("www-authenticate"))
				WwwAuthenticate(fields[1]);
			else if(fields[0].toLowerCase().equals("x-frame-options"))
				XFrameOptions(fields[1]);
			
		}
		
	}

	@Override
	public String getHeaderString() {
	
		String s = "";
	
		if(!AccessControlAllowOrigin().equals("")) s += "Access-Control-Allow-Origin: " + AccessControlAllowOrigin() + "\n"; 
		if(!AcceptRanges().equals("")) s += "Accept-Ranges: " + AcceptRanges() + "\n";
		if(Age() >= 0) s += "Age: " + Age() + "\n";
		if(!Allow().equals("")) s += "Allow: " + Allow() + "\n";
		if(!CacheControl().equals("")) s += "Cache-Control: " + CacheControl() + "\n";
		//if(!Connection().equals("")) s += "Connection: " + Connection() + "\n";
		if(!ContentEncoding().equals("")) s += "Content-Encoding: " + ContentEncoding() + "\n";
		if(!ContentLanguage().equals("")) s += "Content-Language: " + ContentLanguage() + "\n";
		if(ContentLength() >= 0) s += "Content-Length: " + ContentLength() + "\n";
		if(!ContentLocation().equals("")) s += "Content-Location: " + ContentLocation() + "\n";
		if(!ContentMD5().equals("")) s += "Content-MD5: " + ContentMD5() + "\n";
		if(!ContentDisposition().equals("")) s += "Content-Disposition: " + ContentDisposition() + "\n";
		if(!ContentRange().equals("")) s += "Content-Range: " + ContentRange() + "\n";
		if(!ContentType().equals("")) s += "Content-Type: " + ContentType() + "\n";
		if(!Date().equals("")) s += "Date: " + Date() + "\n";
		if(!ETag().equals("")) s += "ETag: " + ETag() + "\n";
		if(!Expires().equals("")) s += "Expires: " + Expires() + "\n";
		if(!LastModified().equals("")) s += "Last-Modified: " + LastModified() + "\n";
		if(!Link().equals("")) s += "Link: " + Link() + "\n";
		if(!Location().equals("")) s += "Location: " + Location() + "\n";
		if(!P3p().equals("")) s += "P3P: " + P3p() + "\n";
		if(!Pragma().equals("")) s += "Pragma: " + Pragma() + "\n";
		if(!ProxyAuthenticate().equals("")) s += "Proxy-Authenticate: " + ProxyAuthenticate() + "\n";
		if(!Refresh().equals("")) s += "Refresh: " + Refresh() + "\n";
		if(!RetryAfter().equals("")) s += "Retry-After: " + RetryAfter() + "\n";
		if(!Server().equals("")) s += "Server: " + Server() + "\n";
		if(!SetCookie().equals("")) s += "Set-Cookie: " + SetCookie() + "\n";
		if(!Status().equals("")) s += "Status: " + Status() + "\n";
		if(!StrictTransportSecurity().equals("")) s += "Strict-Transport-Security: " + StrictTransportSecurity() + "\n";
		if(!Trailer().equals("")) s += "Trailer: " + Trailer() + "\n";
		//if(!TransferEncoding().equals("")) s += "Transfer-Encoding: " + TransferEncoding() + "\n";
		if(!Upgrade().equals("")) s += "Upgrade: " + Upgrade() + "\n";
		if(!Vary().equals("")) s += "Vary: " + Vary() + "\n";
		if(!Via().equals("")) s += "Via: " + Via() + "\n";
		if(!Warning().equals("")) s += "Warning: " + Warning() + "\n";
		if(!WwwAuthenticate().equals("")) s += "Www-Authenticate: " + WwwAuthenticate() + "\n";
		if(!XFrameOptions().equals("")) s += "X-Frame-Options: " + XFrameOptions() + "\n";
		
		return s;
		
	}
	
	@Override
	public String toString() {
		
		String s = "";
		
		s += Protocol() + " " + NCode() + " " + SCode() + "\n";
		s += getHeaderString();
		
		return s;
	
	}
	
	@Override
	public byte[] toByteArray() {
		return toString().getBytes();
	}

}
