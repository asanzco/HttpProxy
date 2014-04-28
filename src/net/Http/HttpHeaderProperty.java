package net.Http;

public class HttpHeaderProperty {

	private String key;
	private String value;
	
	public String Key() { return key; }
	public void Key(String value) { this.key = value; }
	
	public String Value() { return value; }
	public void Value(String value) { this.value = value; }
	
	public HttpHeaderProperty(String key, String value) {
		this.key = key;
		this.value = value;
	}
	
	@Override
	public String toString() {
		return Key() + ": " + Value();
	}
	
}
