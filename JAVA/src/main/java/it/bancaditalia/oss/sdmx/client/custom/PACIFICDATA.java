package it.bancaditalia.oss.sdmx.client.custom;

import java.util.Map;

import it.bancaditalia.oss.sdmx.client.Provider;
import it.bancaditalia.oss.sdmx.client.RestSdmxClient;

public class PACIFICDATA extends RestSdmxClient
{
	public PACIFICDATA(Provider provider)
	{
		super(provider);
	}

	@Override
	public Map<String, String> handleHttpHeaders(String acceptHeader)
	{
		Map<String, String> headers = super.handleHttpHeaders(acceptHeader);
		headers.put("user-agent", "RJSDMX");
		return headers;
	}
}
