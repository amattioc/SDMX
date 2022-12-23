package it.bancaditalia.oss.sdmx.event;

import java.net.Proxy;
import java.net.URL;
import java.util.List;
import java.util.Locale.LanguageRange;

public class OpenEvent implements RestSdmxEvent
{
	private final URL url;
	private final String mediaType;
	private final List<LanguageRange> languages;
	private final Proxy proxy;

	public OpenEvent(URL url, String mediaType, List<LanguageRange> languages, Proxy proxy)
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

	public List<LanguageRange> getLanguages()
	{
		return languages;
	}

	public Proxy getProxy()
	{
		return proxy;
	}
}
