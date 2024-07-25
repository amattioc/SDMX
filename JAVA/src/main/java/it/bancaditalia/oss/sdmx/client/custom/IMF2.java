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

import static it.bancaditalia.oss.sdmx.util.Utils.checkString;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Logger;

import it.bancaditalia.oss.sdmx.api.Dataflow;
import it.bancaditalia.oss.sdmx.client.Provider;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxInvalidParameterException;
import it.bancaditalia.oss.sdmx.util.Configuration;

/**
 * @author Attilio Mattiocco
 *
 */
public class IMF2 extends RestSdmx20Client
{
	protected static Logger logger = Configuration.getSdmxLogger();

	public IMF2(Provider p) throws URISyntaxException
	{
		super(p, "", null);
	}

	@Override
	protected URL buildFlowQuery(String flow, String agency, String version) throws SdmxException
	{
		if (getProvider().getEndpoint() != null)
			return getBuilder().addPath("Dataflow").build();
		else
			throw new SdmxInvalidParameterException("Invalid query parameters: endpoint=" + getProvider().getEndpoint());
	}

	@Override
	protected URL buildDSDQuery(String dsd, String agency, String version, boolean full) throws SdmxException
	{
		checkString(dsd, "The name of the data structure cannot be null");

		return getBuilder().addPath("DataStructure").addPath(dsd).build();
	}

	@Override
	protected URL buildDataQuery(Dataflow dataflow, String tsKey, String startTime, String endTime, boolean serieskeysonly, String updatedAfter, boolean includeHistory, String format) throws SdmxException
	{
		checkString(tsKey, "The ts key must have valid values");

		if (dataflow != null)
			return getBuilder()
					.addPath("CompactData")
					.addPath(dataflow.getDsdIdentifier().getId())
					.addPath(tsKey)
					.withParam("startPeriod", startTime)
					.withParam("endPeriod", endTime)
					.withDetail(serieskeysonly)
					.withParam("updatedAfter", updatedAfter)
					.withHistory(includeHistory)
					.withParam("format", format)
					.build();
		else
			throw new SdmxInvalidParameterException("Invalid query parameters: dataflow=" + dataflow + " resource=" + tsKey + " endpoint=" + getProvider().getEndpoint());
	}

}
