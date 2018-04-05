package it.bancaditalia.oss.sdmx.event;

import java.net.URL;

import it.bancaditalia.oss.sdmx.api.Message;

/**
 * 
 * @author Valentino Pinna
 *
 */
public class DataFooterMessageEvent implements RestSdmxEvent
{
	private final Message msg;
	private final URL query;

	public DataFooterMessageEvent(URL query, Message msg)
	{
		this.query = query;
		this.msg = msg;
	}
	
	public Message getMsg()
	{
		return msg;
	}

	public URL getQuery()
	{
		return query;
	}
}
