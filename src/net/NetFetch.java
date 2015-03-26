package net;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpProcessorBuilder;
import org.apache.http.protocol.HttpRequestExecutor;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;
import org.ccnx.ccn.CCNHandle;
import org.ccnx.ccn.CCNInterestHandler;
import org.ccnx.ccn.config.ConfigurationException;
import org.ccnx.ccn.io.CCNWriter;
import org.ccnx.ccn.profiles.SegmentationProfile;
import org.ccnx.ccn.protocol.ContentName;
import org.ccnx.ccn.protocol.Interest;
import org.ccnx.ccn.protocol.MalformedContentNameStringException;

public class NetFetch implements CCNInterestHandler {
	
	// HTTP
    protected HttpRequestExecutor httpExecutor;
	protected HttpProcessor httpproc;
	
	// CCNX
	public static final String DEFAULT_URI = "ccnx:/httpproxy";
	protected ContentName _prefix;
	protected CCNHandle _handle;
	protected boolean _finished = false;
	
	public NetFetch() throws MalformedContentNameStringException, ConfigurationException, IOException {
		this(DEFAULT_URI);
	}
	
	public NetFetch(String ccnxURI) throws MalformedContentNameStringException, ConfigurationException, IOException {
		
		_prefix = ContentName.fromURI(ccnxURI);
		_handle = CCNHandle.open();
		
	}
	
	public void launch() throws ConfigurationException, MalformedContentNameStringException, IOException, URISyntaxException, Exception {
		_handle.registerFilter(_prefix, this);
		
		// Set up HTTP protocol processor for outgoing connections
		httpproc = HttpProcessorBuilder.create()
	    		.add(new ResponseDate())
	    		.add(new ResponseServer())
	    		.add(new ResponseContent())
	    		.add(new ResponseConnControl()).build();

        // Set up outgoing request executor
        httpExecutor = new HttpRequestExecutor();

	}
	
	public void shutdown() {
		_finished = true;
		if (null != _handle)
			_handle.unregisterFilter(_prefix, this);
	}
	
	public boolean handleInterest(Interest interest) {
		
		System.out.println(">NetFetch: got new interest: " + interest);
		
		// Test to see if we need to respond to it.
		if (!_prefix.isPrefixOf(interest.name())) {
			System.out.println(">Unexpected: got an interest not matching our prefix (which is " + _prefix + ")");
			return false;
		}
		
		if (SegmentationProfile.isSegment(interest.name()) && !SegmentationProfile.isFirstSegment(interest.name())) {
			System.out.println(">Got an interest for something other than a first segment, ignoring " + interest.name() + ".");
			return false;
		} 
		
		try {
			HttpRequest httpRequest = Utils.parseHttpRequest(_prefix, interest);
			System.out.println(">REQUEST: " + httpRequest.toString());
			
			// Remove hop-by-hop headers
			httpRequest.removeHeaders(HTTP.CONTENT_LEN);
			httpRequest.removeHeaders(HTTP.TRANSFER_ENCODING);
			httpRequest.removeHeaders(HTTP.CONN_DIRECTIVE);
            
			CloseableHttpClient httpclient = HttpClients.createDefault();
			HttpUriRequest hur = Utils.parseHttpUriRequest(httpRequest);
			HttpResponse httpResponse = httpclient.execute(hur);
			System.out.println(">RESPONSE: " + httpResponse.toString());
			
			// Remove hop-by-hop headers
			httpResponse.removeHeaders(HTTP.CONTENT_LEN);
			httpResponse.removeHeaders(HTTP.TRANSFER_ENCODING);
			httpResponse.removeHeaders(HTTP.CONN_DIRECTIVE);
            
			String response = Utils.encodeResponse(httpResponse);
			CCNWriter cw;
			try {
				cw = new CCNWriter(interest.getContentName(), _handle);
				cw.addOutstandingInterest(interest);
				cw.put(ContentName.fromNative(interest.name().toString() + "/length" + response.length()), response);
				cw.close();
				return true;
			} catch (IOException e) {
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}			

		return false;
		
	}
		
	public boolean finished() { return _finished; }
	
	public static void main(String[] args) {
		NetFetch netfetch = null;
		try {
			netfetch = new NetFetch();
			netfetch.launch();
			
			while (!netfetch.finished()) {
				// we really want to wait until someone ^C's us.
				try {
					Thread.sleep(100000);
				} catch (InterruptedException e) {
					// do nothing
				}
			}
		} catch (MalformedContentNameStringException e) {
			System.out.println(">bad ccn URI: " + e.getMessage());
		} catch (ConfigurationException e) {
			System.err.println(">Configuration exception running netfetch: " + e.getMessage());
		} catch (IOException e) {
			System.err.println(">IOException handling ccn packages: " + e.getMessage());
		} catch (URISyntaxException e) {
			System.err.println(">URISyntaxException: " + e.getMessage());
		} catch (Exception e) {
			System.err.println(">Exception: " + e.getMessage());
		} finally {
			if (null != netfetch) 
				netfetch.shutdown();
		}			
	}
}
