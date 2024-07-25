package it.bancaditalia.oss.sdmx.client.custom;

import java.net.URL;

import it.bancaditalia.oss.sdmx.api.Dataflow;
import it.bancaditalia.oss.sdmx.client.Provider;
import it.bancaditalia.oss.sdmx.client.RestSdmx21Client;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxInvalidParameterException;

public class EUROSTAT extends RestSdmx21Client
{
	public EUROSTAT(Provider p)
	{
		super(p);
	}

	protected URL buildAvailabilityQueryByKey(Dataflow dataflow, String key) throws SdmxException
	{
		if (dataflow != null)
			// key is ignored
			return getBuilder().addPath("contentconstraint").addPath(dataflow.getAgency()).addPath(dataflow.getId()).build();
		else
			throw new SdmxInvalidParameterException("Invalid query parameters: dataflow=" + dataflow + " filter=" + key);
	}
}
