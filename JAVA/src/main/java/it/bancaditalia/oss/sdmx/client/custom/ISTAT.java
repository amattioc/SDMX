package it.bancaditalia.oss.sdmx.client.custom;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

import it.bancaditalia.oss.sdmx.api.PortableTimeSeries;
import it.bancaditalia.oss.sdmx.client.RestSdmxClient;
import it.bancaditalia.oss.sdmx.parser.v21.DataParsingResult;

public class ISTAT extends RestSdmxClient
{

	public ISTAT() throws URISyntaxException
	{
		super("ISTAT", new URI("http://sdmx.istat.it/SDMXWS/rest"), false, false, false);
	}

	@Override
	protected List<PortableTimeSeries<Double>> postProcess(DataParsingResult result)
	{
		for (PortableTimeSeries<Double> ts: result)
			Collections.sort(ts);
		
		return result;
	}
}
