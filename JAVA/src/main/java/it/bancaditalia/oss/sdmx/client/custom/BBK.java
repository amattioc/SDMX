/* Copyright 2023,2023 Bank Of Italy
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

import static it.bancaditalia.oss.sdmx.util.QueryRunner.runQuery;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;

import it.bancaditalia.oss.sdmx.api.DataFlowStructure;
import it.bancaditalia.oss.sdmx.api.Dataflow;
import it.bancaditalia.oss.sdmx.api.Message;
import it.bancaditalia.oss.sdmx.client.Provider;
import it.bancaditalia.oss.sdmx.client.RestSdmx21Client;
import it.bancaditalia.oss.sdmx.event.DataFooterMessageEvent;
import it.bancaditalia.oss.sdmx.event.RestSdmxEvent;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxInvalidParameterException;
import it.bancaditalia.oss.sdmx.parser.v21.DataParsingResult;
import it.bancaditalia.oss.sdmx.parser.v21.GenericDataParser;
import it.bancaditalia.oss.sdmx.util.QueryRunner;

/**
 * @author Attilio Mattiocco
 *
 */
public class BBK extends RestSdmx21Client
{
	public BBK(Provider p) throws URISyntaxException
	{
		super(p);
	}

	@Override
	protected URL buildFlowQuery(String dataflow, String agency, String version) throws SdmxException
	{
		try
		{
			return new URL(getProvider().getEndpoint() + "/metadata/dataflow/BBK" + (dataflow.equalsIgnoreCase("all") ? "" : ("/" + dataflow)));
		}
		catch (MalformedURLException e)
		{
			throw new SdmxInvalidParameterException("Invalid query parameters: dataflow: " + dataflow + ", endpoint=" + getProvider().getEndpoint());
		}
	}

	@Override
	protected URL buildDSDQuery(String dsd, String agency, String version, boolean full) throws SdmxException
	{
		try
		{
			return new URL(getProvider().getEndpoint() + "/metadata/datastructure/BBK" + (dsd.equalsIgnoreCase("all") ? "" : ("/" + dsd)) + "?references=children");
		}
		catch (MalformedURLException e)
		{
			throw new SdmxInvalidParameterException("Invalid query parameters: dsd: " + dsd + ", endpoint=" + getProvider().getEndpoint());
		}
	}

	@Override
	protected URL buildCodelistQuery(String codeList, String agency, String version) throws SdmxException
	{
		try
		{
			return new URL(getProvider().getEndpoint() + "/metadata/codelist/BBK" + (codeList.equalsIgnoreCase("all") ? "" : ("/" + codeList)));
		}
		catch (MalformedURLException e)
		{
			throw new SdmxInvalidParameterException("Invalid query parameters: codelist: " + codeList + ", endpoint=" + getProvider().getEndpoint());
		}
	}

	@Override
	protected DataParsingResult getData(Dataflow dataflow, DataFlowStructure dsd, String resource, String startTime, String endTime, boolean serieskeysonly, String updatedAfter,
			boolean includeHistory) throws SdmxException
	{
		URL query = buildDataQuery(new Dataflow(dataflow.getId(), null, null, null), resource, startTime, endTime, false, null, false, null);
		DataParsingResult ts = runQuery(new GenericDataParser(dsd, dataflow, !serieskeysonly), query, handleHttpHeaders("application/xml"));
		Message msg = ts.getMessage();
		if (msg != null)
		{
			LOGGER.log(Level.INFO, "The sdmx call returned messages in the footer:\n {0}", msg);
			RestSdmxEvent event = new DataFooterMessageEvent(query, msg);
			QueryRunner.getDataFooterMessageEventListener().onSdmxEvent(event);
		}
		return ts;
	}
}
