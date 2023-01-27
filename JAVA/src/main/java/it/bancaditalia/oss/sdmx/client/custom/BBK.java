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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;

import it.bancaditalia.oss.sdmx.api.DataFlowStructure;
import it.bancaditalia.oss.sdmx.api.Dataflow;
import it.bancaditalia.oss.sdmx.api.Message;
import it.bancaditalia.oss.sdmx.client.RestSdmxClient;
import it.bancaditalia.oss.sdmx.client.SdmxClientHandler;
import it.bancaditalia.oss.sdmx.event.DataFooterMessageEvent;
import it.bancaditalia.oss.sdmx.event.RestSdmxEvent;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxInvalidParameterException;
import it.bancaditalia.oss.sdmx.parser.v21.DataParsingResult;
import it.bancaditalia.oss.sdmx.parser.v21.GenericDataParser;

/**
 * @author Attilio Mattiocco
 *
 */
public class BBK extends RestSdmxClient{
	private static final String BBK_PROVIDER = "https://api.statistiken.bundesbank.de/rest";	

	public BBK() throws URISyntaxException {
		super("Eurostat", new URI(BBK_PROVIDER), false, false, false);
	}

	@Override
	protected URL buildFlowQuery(String dataflow, String agency, String version) throws SdmxException{
		try {
			return new URL(endpoint + "/metadata/dataflow/BBK" + (dataflow.equalsIgnoreCase("all") ? "" : ("/" + dataflow)));
		} catch (MalformedURLException e) {
			throw new SdmxInvalidParameterException("Invalid query parameters: dataflow: " + dataflow + ", endpoint=" + endpoint);
		}
	}

	@Override
	protected URL buildDSDQuery(String dsd, String agency, String version, boolean full) throws SdmxException {
		try {
			return new URL(endpoint + "/metadata/datastructure/BBK" + (dsd.equalsIgnoreCase("all") ? "" : ("/" + dsd)) + "?references=children");
		} catch (MalformedURLException e) {
			throw new SdmxInvalidParameterException("Invalid query parameters: dsd: " + dsd + ", endpoint=" + endpoint);
		}
	}
	
	@Override
	protected URL buildCodelistQuery(String codeList, String agency, String version) throws SdmxException
	{
		try {
			return new URL(endpoint + "/metadata/codelist/BBK" + (codeList.equalsIgnoreCase("all") ? "" : ("/" + codeList)));
		} catch (MalformedURLException e) {
			throw new SdmxInvalidParameterException("Invalid query parameters: codelist: " + codeList + ", endpoint=" + endpoint);
		}
	}
	
	@Override
	protected DataParsingResult getData(Dataflow dataflow, DataFlowStructure dsd, String resource, String startTime, String endTime, boolean serieskeysonly,
			String updatedAfter, boolean includeHistory) throws SdmxException
	{
		URL query = buildDataQuery(new Dataflow(dataflow.getId(), null, null, null), resource, startTime, endTime, false, null, false);
		DataParsingResult ts = runQuery(new GenericDataParser(dsd, dataflow, !serieskeysonly), query,
				"application/xml", null);
		Message msg = ts.getMessage();
		if (msg != null)
		{
			LOGGER.log(Level.INFO, "The sdmx call returned messages in the footer:\n {0}", msg);
			RestSdmxEvent event = new DataFooterMessageEvent(query, msg);
			dataFooterMessageEventListener.onSdmxEvent(event);
		}
		return ts;
	}
	
	public static void main(String[] args) throws SdmxException {
		System.err.println(SdmxClientHandler.getDataFlowStructure("BBK", "BBAI3"));
		//System.err.println(SdmxClientHandler.getCodes("BBK", "BBAI3", "BBK_STD_FREQ"));
		//System.err.println(SdmxClientHandler.getTimeSeries("BBK", "BBAI3/Q............", null,null));
	}
}
