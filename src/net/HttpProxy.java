package net;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Hashtable;

import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpConnectionFactory;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpServerConnection;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.impl.DefaultBHttpServerConnection;
import org.apache.http.impl.DefaultBHttpServerConnectionFactory;
import org.ccnx.ccn.CCNContentHandler;
import org.ccnx.ccn.CCNHandle;
import org.ccnx.ccn.config.ConfigurationException;
import org.ccnx.ccn.protocol.ContentName;
import org.ccnx.ccn.protocol.ContentObject;
import org.ccnx.ccn.protocol.Interest;
import org.ccnx.ccn.protocol.MalformedContentNameStringException;

import net.Utils;

public class HttpProxy {
	
	// HttpProxy.list
	private static final String ListFileName = "HttpProxy.list";
	private ArrayList<HttpProxyListElement>HttpProxyList = new ArrayList<HttpProxyListElement>();
	
	// HTTP
	public static final int PortServer = 8080;
	
	// CCNX
	public static String DEFAULT_URI = "ccnx:/httpproxy";	
	private ContentName _prefix;
	private ContentHandler _handler;
	
	public static void main(String[] args) {
		
		HttpProxy proxy;
		try {
			proxy = new HttpProxy();
			proxy.launch();
		} catch (Exception e) {
			System.err.println(">" + e.getMessage());
		}
		
	}
	
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
			throw new Exception("Exception getting HttpProxy.list: " + e.getMessage());
		}
		
		_prefix = ContentName.fromURI(ccnxURI);

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
		
		// Set Content handler
		_handler = new ContentHandler();
			    
	  	Thread t = new RequestListenerThread(PortServer);
	  	t.setDaemon(false);
	  	t.start();
	    
	}

	private class RequestListenerThread extends Thread {

        private final HttpConnectionFactory<DefaultBHttpServerConnection> connFactory;
        private final ServerSocket serversocket;

        public RequestListenerThread(int port) throws IOException {
            this.connFactory = DefaultBHttpServerConnectionFactory.INSTANCE;
            this.serversocket = new ServerSocket(port);
        }

        @Override
        public void run() {
            System.out.println(">Listening on port " + this.serversocket.getLocalPort());
            while (!Thread.interrupted()) {
                try {
                    // Set up HTTP connection
                    Socket socket = this.serversocket.accept();
                    System.out.println(">Incoming connection from " + socket.getInetAddress());
                    HttpServerConnection conn = this.connFactory.createConnection(socket);
                    
                    System.out.println(">New connection thread");
                    try {
                    	HttpRequest request = conn.receiveRequestHeader();
                    	String method = request.getRequestLine().getMethod();
        				if (!method.equals("GET")){
        					throw new MethodNotSupportedException(method + " method not supported");
        				}
        				// If it can be send it will be sent to netfetch via CCN
        				if(!canBeSent(request)) throw new Exception("Url not supported");
        				SendInterest(request, conn);
                    } catch (ConnectionClosedException ex) {
                        System.err.println("Client closed connection");
                    } catch (IOException ex) {
                        System.err.println("I/O error: " + ex.getMessage());
                    } catch (HttpException ex) {
                        System.err.println("Unrecoverable HTTP protocol violation: " + ex.getMessage());
                    } catch (Exception ex) {
                        System.err.println("Error: " + ex.getMessage());
        			} 
                } catch (InterruptedIOException ex) {
                    break;
                } catch (IOException e) {
                    System.err.println("I/O error initialising connection thread: " + e.getMessage());
                    break;
                }
            }
            
        }
        
        private boolean canBeSent(HttpRequest hr) throws Exception {
			for(HttpProxyListElement element : HttpProxyList) 
				for(TrailingSwitch.TrailingSwitches trailingSwitch : element.getTrailingSwitches())
					if(TrailingSwitch.checkTrailingSwitch(hr, element.getUrl(), trailingSwitch)) return true;
			return false;
		}
		
		private void SendInterest(HttpRequest request, HttpServerConnection conn) throws MalformedContentNameStringException, ConfigurationException, IOException {

			Interest interest = Utils.parseInterest(_prefix, request);
			System.out.println(">REGISTERING REQUEST: " + request.toString());
						
			_handler.registerInterest(interest, conn);
						
		}
    }
    
	private class ContentHandler implements CCNContentHandler {
		
		private Hashtable<String, ArrayList<HttpServerConnection>> petitions = new Hashtable<String, ArrayList<HttpServerConnection>>();
		private Hashtable<String, String> responsesEncoded = new Hashtable<String, String>();
		private Hashtable<String, HttpResponse> responses = new Hashtable<String, HttpResponse>();
				
		@Override
		public Interest handleContent(ContentObject contentObject, Interest interest) {
			
			boolean sendData = false;
			
			String name = "";
			int segment = 0;
			int length = 0;
			
			String nameData = contentObject.name().toString();
			Pattern p = Pattern.compile("^((.*)/length(\\d+))(/=([\\dA-F]+))?$");
			
			Matcher matcher = p.matcher(nameData);
			if (matcher.find()) {
			    nameData = matcher.group(1);
			    name = matcher.group(2);
			    length = Integer.parseInt(matcher.group(3));
			    segment = Integer.parseInt(matcher.group(5), 16);	    
			}

			synchronized (responsesEncoded) {
				String response;
				if(responsesEncoded.containsKey(name)) {
					response = responsesEncoded.get(name);
					responsesEncoded.remove(name);
					// Add new data recieved
					String recieved = new String(contentObject.content());
					response += recieved;
				} else {
					response = new String(contentObject.content());
				}
				try {
					if(response.length() >= length) {
						System.out.println(">DATA RECIEVED: " + segment + ": COMPLETED! '" + nameData + "' #for interest : " + name);
						sendData = true;
						synchronized (responses) {
							responses.put(name, Utils.decodeResponse(response));
						}
					} else {
						System.out.println(">DATA RECIEVED: " + segment + ": INCOMPLETED " + response.length() + "/" + length + " '"+ nameData + "' #for interest : " + name);
						responsesEncoded.put(name, response);
					}			
				
				} catch (Exception e) {
					responsesEncoded.put(name, response);
				}				
			}
				
			
			if (sendData) {
				 SendRespose(name);
				 return null;
			} else {
				
				try {
					String newSegment = String.format("%0$"+4+"s", Integer.toHexString(segment+1)).replace(" ", "0");
					interest.name(ContentName.fromURI(nameData + "/=" + newSegment));
					System.out.println(">GENERATED NEW INTEREST: " + newSegment + " : " + interest.name().toString() + " #for interest : " + name);
					return interest;
				} catch (MalformedContentNameStringException e) {
				}
				return null;
			}
			
		}
		
		private void SendRespose(String name) {
			
			HttpResponse response;
			synchronized (responses) {
				response = responses.get(name);
				responses.remove(name);
			}
			synchronized (petitions) {
				for(HttpServerConnection conn : petitions.get(name)) {
					try {
						System.out.println(">SENDING TO [" + conn.toString() + "] :" + name);
						System.out.println(response.toString());
						conn.sendResponseHeader(response);
						conn.sendResponseEntity(response);
						conn.close();
					} catch (HttpException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				petitions.remove(name);
			}
		}
		
		public void registerInterest(Interest interest, HttpServerConnection conn) throws IOException {
			
			System.out.println(">REGISTERING NEW INTEREST: " + interest.name());
			synchronized (petitions) {
				if(petitions.containsKey(interest.name())) {
					petitions.get(interest.name()).add(conn);
				}else{
					ArrayList<HttpServerConnection> connList = new ArrayList<HttpServerConnection>();
					connList.add(conn);
					petitions.put(interest.name().toString(), connList);
					CCNHandle.getHandle().expressInterest(interest, this);
				}
			}
			
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
			if (p.matcher(httpRequest.getRequestLine().getUri()).matches()){
				switch(trailingSwitch){
				case NONE:
					result = true;
					break;
				case NO_COOKIE:
					result = (httpRequest.getHeaders("Cookie").length == 0);
					break;
				case NO_REFERER:
					result = (httpRequest.getHeaders("Referer").length == 0);
					break;
				case NEED_DOT:
					result = httpRequest.getRequestLine().getUri().substring(
							httpRequest.getRequestLine().getUri().indexOf(
									httpRequest.getFirstHeader("Host").getValue() + httpRequest.getFirstHeader("Host").getValue().length()))
							.contains(".");
					break;
				case NO_QUERY:
					result = !httpRequest.getRequestLine().getUri().contains("?");
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
