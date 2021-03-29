package it.bancaditalia.oss.sdmx.event;

import it.bancaditalia.oss.sdmx.util.LanguagePriorityList;
import java.net.Proxy;
import java.net.URL;

public class OpenEvent implements RestSdmxEvent
{
	private final URL url;
	private final String mediaType;
	private final LanguagePriorityList languages;
	private final Proxy proxy;

	public OpenEvent(URL url, String mediaType, LanguagePriorityList languages, Proxy proxy)
	{
		this.url = url;
		this.mediaType = mediaType;
		this.languages = languages;
		this.proxy = proxy;
	}

	public URL getUrl()
	{
		return url;
	}

	public String getMediaType()
	{
		return mediaType;
	}

	public LanguagePriorityList getLanguages()
	{
		return languages;
	}

	public Proxy getProxy()
	{
		return proxy;
	}
}
