package it.bancaditalia.oss.sdmx.client;

import static it.bancaditalia.oss.sdmx.api.SDMXVersion.V2;

import java.net.URI;

import it.bancaditalia.oss.sdmx.client.RestSdmx21Client.Sdmx21QueryBuilder;
import it.bancaditalia.oss.sdmx.util.RestQueryBuilder;

public class RestSdmx21Client extends AbstractRestSdmxClient<Sdmx21QueryBuilder>
{
	public RestSdmx21Client(Provider provider)
	{
		super(provider, V2);
	}

	public static class Sdmx21QueryBuilder extends RestQueryBuilder<Sdmx21QueryBuilder>
	{
		protected Sdmx21QueryBuilder(URI entryPoint)
		{
			super(entryPoint);
		}
	}

	@Override
	protected Sdmx21QueryBuilder getBuilder(URI endpoint)
	{
		return new Sdmx21QueryBuilder(endpoint);
	}
}
