package net;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.SignatureException;

import net.Http.HttpRequest;
import net.Http.HttpResponse;

import org.ccnx.ccn.CCNHandle;
import org.ccnx.ccn.CCNInterestHandler;
import org.ccnx.ccn.config.ConfigurationException;
import org.ccnx.ccn.io.CCNWriter;
import org.ccnx.ccn.profiles.SegmentationProfile;
import org.ccnx.ccn.protocol.ContentName;
import org.ccnx.ccn.protocol.Interest;
import org.ccnx.ccn.protocol.MalformedContentNameStringException;

public class NetFetch implements CCNInterestHandler {
	
	public static String DEFAULT_URI = "ccnx:/";
	
	protected boolean _finished = false;
	protected ContentName _prefix;
	protected CCNHandle _handle;
	
	public NetFetch() throws MalformedContentNameStringException, ConfigurationException, IOException {
		this(DEFAULT_URI);
	}
	
	public NetFetch(String ccnxURI) throws MalformedContentNameStringException, ConfigurationException, IOException {
		
		_prefix = ContentName.fromURI(ccnxURI);
		_handle = CCNHandle.open();
		
	}
	
	public void launch() throws ConfigurationException, MalformedContentNameStringException, IOException  {
		_handle.registerFilter(_prefix, this);
	}
	
	public void shutdown() {
		_finished = true;
		if (null != _handle)
			_handle.unregisterFilter(_prefix, this);
	}
	
	public boolean handleInterest(Interest interest) {
		
		System.out.println("NetFetch: got new interest: " + interest);
		
		// Test to see if we need to respond to it.
		if (!_prefix.isPrefixOf(interest.name())) {
			System.out.println("Unexpected: got an interest not matching our prefix (which is " + _prefix + ")");
			return false;
		}
		
		if (SegmentationProfile.isSegment(interest.name()) && !SegmentationProfile.isFirstSegment(interest.name())) {
			System.out.println("Got an interest for something other than a first segment, ignoring " + interest.name() + ".");
			return false;
		} 
		
		HttpRequest httpRequest = Utils.parseHttpRequest(_prefix, interest);
		try {
			HttpURLConnection con = httpRequest.SendRequest();
			HttpResponse httpResponse = new HttpResponse();
			httpResponse.readHttpResponse(con);

			CCNWriter cw;
			try {
				cw = new CCNWriter(interest.getContentName(), _handle);
				cw.addOutstandingInterest(interest);
				cw.put(ContentName.fromNative(interest.name().toString() + "/length" + httpResponse.toString().length()), httpResponse.toString());
				cw.close();
				return true;
			} catch (IOException e) {
			} catch (SignatureException e) {
			} catch (MalformedContentNameStringException e) {
			}
		} catch (IOException e) {
		} catch (Exception e) {
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
			System.out.println("bad ccn URI: " + e.getMessage());
		} catch (ConfigurationException e) {
			System.err.println("Configuration exception running netfetch: " + e.getMessage());
		} catch (IOException e) {
			System.err.println("IOException handling ccn packages: " + e.getMessage());
		} finally {
			if (null != netfetch) 
				netfetch.shutdown();
		}			
	}
}
