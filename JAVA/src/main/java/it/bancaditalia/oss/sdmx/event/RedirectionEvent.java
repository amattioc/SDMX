package it.bancaditalia.oss.sdmx.event;

import java.net.URL;

public class RedirectionEvent implements RestSdmxEvent
{
	private final URL url;
	private final URL redirection;

	public RedirectionEvent(URL url, URL redirection)
	{
		this.url = url;
		this.redirection = redirection;
	}

	public URL getUrl()
	{
		return url;
	}

	public URL getRedirection()
	{
		return redirection;
	}
}
