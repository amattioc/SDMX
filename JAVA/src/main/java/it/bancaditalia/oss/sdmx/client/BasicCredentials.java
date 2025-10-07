package it.bancaditalia.oss.sdmx.client;

import java.util.Base64;

public class BasicCredentials implements Credentials
{
	private final String header;
	
	public BasicCredentials(String user, String pw)
	{
		header = "Basic " + Base64.getEncoder().encodeToString((user + ":" + pw).getBytes());
	}

	@Override
	public String getHeader()
	{
		return header;
	}
}
