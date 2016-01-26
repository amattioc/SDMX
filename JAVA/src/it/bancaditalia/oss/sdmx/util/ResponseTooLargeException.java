package it.bancaditalia.oss.sdmx.util;

public class ResponseTooLargeException extends SdmxException {

	private static final long serialVersionUID = 1L;
	private String url = null;
	public ResponseTooLargeException(String message, String url) {
		super(message);
		this.url = url;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
}
