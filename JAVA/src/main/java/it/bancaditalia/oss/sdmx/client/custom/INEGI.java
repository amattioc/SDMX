/* Copyright 2010,2014 Bank Of Italy
*
* Licensed under the EUPL, Version 1.1 or - as soon they
* will be approved by the European Commission - subsequent
* versions of the EUPL (the "Licence");
* You may not use this work except in compliance with the
* Licence.
* You may obtain a copy of the Licence at:
*
*
* http://ec.europa.eu/idabc/eupl
*
* Unless required by applicable law or agreed to in
* writing, software distributed under the Licence is
* distributed on an "AS IS" basis,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
* express or implied.
* See the Licence for the specific language governing
* permissions and limitations under the Licence.
*/
package it.bancaditalia.oss.sdmx.client.custom;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import it.bancaditalia.oss.sdmx.api.Dataflow;
import it.bancaditalia.oss.sdmx.client.Provider;
import it.bancaditalia.oss.sdmx.client.RestSdmxClient;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxExceptionFactory;
import it.bancaditalia.oss.sdmx.exceptions.SdmxInvalidParameterException;
import it.bancaditalia.oss.sdmx.util.RestQueryBuilder;

/**
 * @author Attilio Mattiocco
 *
 */
public class INEGI extends RestSdmxClient
{
	public INEGI(Provider p) throws URISyntaxException
	{
		super(p);
	}

	@Override
	protected URL buildDataQuery(Dataflow dataflow, String resource, String startTime, String endTime, boolean serieskeysonly, String updatedAfter, boolean includeHistory) throws SdmxException
	{
		// TODO Auto-generated method stub
		return super.buildDataQuery(dataflow, resource + "/", startTime, endTime, serieskeysonly, updatedAfter, includeHistory);
	}
	
	@Override
	protected URL buildDSDQuery(String dsd, String agency, String version, boolean full) throws SdmxException
	{
		if (provider.getEndpoint() != null && dsd != null && !dsd.isEmpty())
		{
			try
			{
				return new RestQueryBuilder(provider.getEndpoint()).addPath("DataStructure").addPath(agency).addPath(dsd).addPath(version).build();
			}
			catch (MalformedURLException e)
			{
				throw SdmxExceptionFactory.wrap(e);
			}
		}
		else
			throw new SdmxInvalidParameterException("Invalid query parameters: dsd=" + dsd + " endpoint=" + provider.getEndpoint());
	}
}
