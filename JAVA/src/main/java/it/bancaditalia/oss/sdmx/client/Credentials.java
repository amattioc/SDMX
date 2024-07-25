package it.bancaditalia.oss.sdmx.client;

import java.util.Base64;

public class Credentials
{
	private String header = null;

	public String getHeader()
	{
		return header;
	}

	public void fillCredentials(String user, String pw)
	{
		header = Base64.getEncoder().encodeToString((user + ":" + pw).getBytes());
	}
}
