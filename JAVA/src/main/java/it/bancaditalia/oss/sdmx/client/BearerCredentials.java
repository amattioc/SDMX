package it.bancaditalia.oss.sdmx.client;

public class BearerCredentials implements Credentials
{
	private final String header;
	
	public BearerCredentials(String token)
	{
		header = "Bearer " + token;
	}
	
	@Override
	public String getHeader()
	{
		return header;
	}
}
