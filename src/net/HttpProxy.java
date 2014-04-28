package net;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Hashtable;

import org.ccnx.ccn.CCNContentHandler;
import org.ccnx.ccn.CCNHandle;
import org.ccnx.ccn.config.ConfigurationException;
import org.ccnx.ccn.protocol.ContentName;
import org.ccnx.ccn.protocol.ContentObject;
import org.ccnx.ccn.protocol.Interest;
import org.ccnx.ccn.protocol.MalformedContentNameStringException;

import net.Utils;
import net.Http.HttpRequest;


public class HttpProxy {

	private static final String ListFileName = "HttpProxy.list";
	
	private ArrayList<HttpProxyListElement>HttpProxyList = new ArrayList<HttpProxyListElement>();
	
	public static final int PortServer = 8080;
	private static ServerSocket ss;
	
	public static String DEFAULT_URI = "ccnx:/";	
	private ContentName _prefix;
	private ContentHandler _handler;
	
	
	public HttpProxy() throws MalformedContentNameStringException, Exception{
		this(DEFAULT_URI);
	}
	
	public HttpProxy(String ccnxURI) throws MalformedContentNameStringException, Exception {
		try {
			// Get HttpProxy list 
			getHttpProxyList();
		} catch (IOException e) {
			throw new Exception("IOException reading HttpProxy.list: " + e.getMessage());
		} catch (Exception e) {
			throw new Exception("Exception geting HttpProxy.list: " + e.getMessage());
		}
		
		try {
			// Initialize server socket
			ss = new ServerSocket(PortServer);
		} catch (IOException e) {
			throw new Exception("IOException initializing ServerSocket: " + e.getMessage());
		}
		
		_prefix = ContentName.fromURI(ccnxURI);
		_handler = new ContentHandler();

	}
	
	private void getHttpProxyList() throws IOException, Exception {
		
		
		String fileName = (new File(".")).getAbsolutePath() + "/src/net/" + ListFileName;
		File file = new File(fileName);
		if(!file.exists()) throw new Exception("Not found 'HttpProxy.list");
		BufferedReader br = new BufferedReader(new FileReader(file));
		
		String line;
		while((line = br.readLine()) != null) {
			if(line.length() == 0 || line.startsWith("#")) continue;
			HttpProxyList.add(new HttpProxyListElement(line));
		}
		
		br.close();
		
	}
	
	public void launch() throws IOException{
		for(;;) {
			
			try {
				Socket s = ss.accept();
				(new ServeRequest(s)).start();
			} catch (IOException e) {
				throw new IOException("IOException accepting new client: " + e.getMessage());
			}
			
		}
	}
	
	public static void main(String[] args) {
		
		HttpProxy proxy;
		try {
			proxy = new HttpProxy();
			proxy.launch();
		} catch (Exception e) {
			System.err.println(">" + e.getMessage());
		}
		
	}

	private class ServeRequest extends Thread {
		
		private Socket s;
		
		public ServeRequest(Socket s) {
			this.s = s;
		}
		
		public void run() {
			
			try {
				// Get the HttpRequest from the net
				HttpRequest httpRequest = new HttpRequest();
				httpRequest.readHttpRequest(s);
								
				// If it can be send it will be sent to netfetch via CCN
				if(!canBeSent(httpRequest)) throw new Exception("Url not supported");
				SendInterest(httpRequest);
				
			} catch (Exception e) {
				System.err.println(">Error handling httpRequest: " + e.getMessage());
			}
			
		}
		
		private void SendInterest(HttpRequest httpRequest) throws MalformedContentNameStringException, ConfigurationException, IOException {

			Interest interest = Utils.parseInterest(_prefix, httpRequest);
			System.out.println(".............................");
			System.out.println("REGISTERING REQUEST");
			System.out.println(httpRequest.toString());
			System.out.println(".............................");
			_handler.registerInterest(interest, s);
						
		}
		
		private boolean canBeSent(HttpRequest hr) throws Exception {
			for(HttpProxyListElement element : HttpProxyList) 
				for(TrailingSwitch.TrailingSwitches trailingSwitch : element.getTrailingSwitches())
					if(TrailingSwitch.checkTrailingSwitch(hr, element.getUrl(), trailingSwitch)) return true;
			return false;
		}
		
	}
	
	private class ContentHandler implements CCNContentHandler {
		
		private Hashtable<String, ArrayList<Socket>> sockets = new Hashtable<String, ArrayList<Socket>>();
		private Hashtable<String, ByteArrayOutputStream> responsesRaw = new Hashtable<String, ByteArrayOutputStream>();
		private Hashtable<String, byte[]> responses = new Hashtable<String, byte[]>();
		
		@Override
		public Interest handleContent(ContentObject contentObject, Interest interest) {
			
			boolean sendData = false;
			
			String name = "";
			int segment = 0;
			int size = 0;
			
			String nameData = contentObject.name().toString();
			Pattern p = Pattern.compile("^((.*)/size(\\d+))(/=([\\dA-F]+))?$");
			
			Matcher matcher = p.matcher(nameData);
			if (matcher.find()) {
			    nameData = matcher.group(1);
			    name = matcher.group(2);
			    size = Integer.parseInt(matcher.group(3));
			    segment = Integer.parseInt(matcher.group(5), 16);	    
			}

			synchronized (responsesRaw) {
				ByteArrayOutputStream response;
				if(responsesRaw.containsKey(name)) {
					response = responsesRaw.get(name);
					responsesRaw.remove(name);
					try {
						response.write(contentObject.content());
					} catch (IOException e) {
					}
				} else {
					response = new ByteArrayOutputStream();
					try {
						response.write(contentObject.content());
					} catch (IOException e) {
					}
				}
				try {
					if(response.size() >= size) {
						System.out.println(">DATA RECIEVED: '" + nameData + "' : " + segment + ": COMPLETED!" + " #for interest : " + name);
						sendData = true;
						synchronized (responses) {
							responses.put(name, response.toByteArray());
						}
					} else {
						System.out.println(">DATA RECIEVED: '" + nameData + "' : " + segment + ": INCOMPLETED " + response.size() + "/" + size + " #for interest : " + name);
						responsesRaw.put(name, response);
					}			
				
				} catch (Exception e) {
					responsesRaw.put(name, response);
				}				
			}
				
			
			if (sendData) {
				 SendRespose(name);
				 return null;
			} else {
				
				try {
					String newSegment = String.format("%0$"+4+"s", Integer.toHexString(segment+1)).replace(" ", "0");
					interest.name(ContentName.fromURI(nameData + "/=" + newSegment));
					System.out.println(">GENERATED NEW INTEREST: " + interest.name().toString() + " #for interest : " + name);
					return interest;
				} catch (MalformedContentNameStringException e) {
				}
				return null;
			}
			
		}
		
		private void SendRespose(String name) {
			
			byte[] response;
			synchronized (responses) {
				response = responses.get(name);
				responses.remove(name);
			}
			synchronized (sockets) {
				for(Socket s : sockets.get(name)) {
					try {
						BufferedOutputStream bos = new BufferedOutputStream(s.getOutputStream());
						System.out.println(">SENDING to " + s + "...... " + name + " (" +response.length + ")");
						bos.write(response);
						bos.close();
						s.close();
					} catch (IOException e) {
					}
				}
				sockets.remove(name);
			}
		}
		
		public void registerInterest(Interest interest, Socket socket) throws IOException {
			
			System.out.println(">REGISTERING NEW INTEREST: " + interest.name());
			
			synchronized (sockets) {
				if(sockets.containsKey(interest.name())) {
					sockets.get(interest.name()).add(socket);
				}else{
					ArrayList<Socket> socketsList = new ArrayList<Socket>();
					socketsList.add(socket);
					sockets.put(interest.name().toString(), socketsList);
				}
			}
			
			CCNHandle.getHandle().expressInterest(interest, this);
			
		}
		
	}
		
	public static class TrailingSwitch {
		
		//-noCookie		GET request must have no Cookie: field
		//-noReferer	GET request must have no Referer: field
		//-needDot		full name must contain a "." character after the host name
		//-noQuery		full name must not contain a "?" character
		//-single		must only use one socket at a time for the host
		//-fail			request will immediately fail (closes socket without reply)
		private enum TrailingSwitches { 
			NO_COOKIE, NO_REFERER, NEED_DOT, NO_QUERY, SINGLE, FAIL, NONE
		}
		
		public static TrailingSwitches getTrailingSwitch(String trailingSwitch) throws Exception {
			if(trailingSwitch.toLowerCase().equals(""))
				return TrailingSwitches.NONE;
			else if(trailingSwitch.toLowerCase().equals("-nocookie"))
				return TrailingSwitches.NO_COOKIE;
			else if(trailingSwitch.toLowerCase().equals("-noreferer"))
				return TrailingSwitches.NO_REFERER;
			else if(trailingSwitch.toLowerCase().equals("-needdot"))
				return TrailingSwitches.NEED_DOT;
			else if(trailingSwitch.toLowerCase().equals("-noquery"))
				return TrailingSwitches.NO_QUERY;
			else if(trailingSwitch.toLowerCase().equals("-single"))
				return TrailingSwitches.SINGLE;
			else if(trailingSwitch.toLowerCase().equals("-fail"))
				return TrailingSwitches.FAIL;
			
			throw new Exception("Trailing switch '" + trailingSwitch + "' not found in the list");
		}
		
		public static ArrayList<TrailingSwitches> getTrailingSwitches(String trailingSwitches) throws Exception {
			
			ArrayList<TrailingSwitches> list = new ArrayList<TrailingSwitches>();
			
			String[] splited = trailingSwitches.split(" ");
			for(int i=0; i < splited.length; i++)
				list.add(getTrailingSwitch(splited[i]));
			
			return list;
			
		}
		
		public static boolean checkTrailingSwitch(HttpRequest httpRequest, String regex, TrailingSwitches trailingSwitch) throws Exception {
			boolean result = false;
			Pattern p = Pattern.compile(regex.replace(".", "\\.").replace("*", ".*"));
			if (p.matcher(httpRequest.Header().Path()).matches()){
				switch(trailingSwitch){
				case NONE:
					result = true;
					break;
				case NO_COOKIE:
					result = !httpRequest.hasCookies();
					break;
				case NO_REFERER:
					result = true;
					break;
				case NEED_DOT:
					result = true;
					break;
				case NO_QUERY:
					result = true;
					break;
				case SINGLE:
					result = true;
					break;
				case FAIL:
					throw new Exception("TrailingSwitch is FAIL");
				}
			}
			return result;
		}
	}

	private class HttpProxyListElement {
		
		private ArrayList<TrailingSwitch.TrailingSwitches> trailingSwitches = new ArrayList<TrailingSwitch.TrailingSwitches>();
		private String url = "";
		
		public ArrayList<TrailingSwitch.TrailingSwitches> getTrailingSwitches() { return this.trailingSwitches; }
		public String getUrl() { return this.url; }
		
		public HttpProxyListElement(String rawLine) throws Exception {
			this(rawLine.trim().split(" ")[0], (rawLine.trim().split(" ", 2).length == 2) ? rawLine.trim().split(" ", 2)[1] : "");
		}
		
		public HttpProxyListElement(String url, String trailingSwitches) throws Exception {
			this(url, TrailingSwitch.getTrailingSwitches(trailingSwitches));
		}

		public HttpProxyListElement(String url, ArrayList<TrailingSwitch.TrailingSwitches> trailingSwitches) {
			this.url = url;
			this.trailingSwitches = trailingSwitches;
		}
	}
}
