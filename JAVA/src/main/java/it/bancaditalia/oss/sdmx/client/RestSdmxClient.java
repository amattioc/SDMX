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
* WITHOUT WARRANTIES OR CONDITIONS OF ANY_LANGUAGE KIND, either
* express or implied.
* See the Licence for the specific language governing
* permissions and limitations under the Licence.
*/
package it.bancaditalia.oss.sdmx.client;

import static it.bancaditalia.oss.sdmx.api.SDMXVersion.V2;
import static it.bancaditalia.oss.sdmx.util.QueryRunner.runQuery;
import static java.util.stream.Collectors.joining;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import it.bancaditalia.oss.sdmx.api.Codelist;
import it.bancaditalia.oss.sdmx.api.DataFlowStructure;
import it.bancaditalia.oss.sdmx.api.Dataflow;
import it.bancaditalia.oss.sdmx.api.GenericSDMXClient;
import it.bancaditalia.oss.sdmx.api.Message;
import it.bancaditalia.oss.sdmx.api.PortableTimeSeries;
import it.bancaditalia.oss.sdmx.api.SDMXReference;
import it.bancaditalia.oss.sdmx.api.SDMXVersion;
import it.bancaditalia.oss.sdmx.event.DataFooterMessageEvent;
import it.bancaditalia.oss.sdmx.event.RestSdmxEvent;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxInvalidParameterException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxXmlContentException;
import it.bancaditalia.oss.sdmx.parser.v21.CodelistParser;
import it.bancaditalia.oss.sdmx.parser.v21.CompactDataParser;
import it.bancaditalia.oss.sdmx.parser.v21.DataParsingResult;
import it.bancaditalia.oss.sdmx.parser.v21.DataStructureParser;
import it.bancaditalia.oss.sdmx.parser.v21.DataflowParser;
import it.bancaditalia.oss.sdmx.parser.v21.Sdmx21Queries;
import it.bancaditalia.oss.sdmx.util.Configuration;
import it.bancaditalia.oss.sdmx.util.QueryRunner;

/**
 * @author Attilio Mattiocco
 *
 */
public class RestSdmxClient implements GenericSDMXClient
{
	protected static final Logger	LOGGER = Configuration.getSdmxLogger();

	protected SDMXVersion			sdmxVersion = V2;

	protected boolean				needsCredentials				= false;
	protected boolean				containsCredentials				= false;
	protected String				user							= null;
	protected String				pw								= null;
	
	protected final Provider provider;
	protected String LATEST_VERSION	= "latest";
	protected String ALL_KEYWORD	= "all";

	public RestSdmxClient(Provider provider)
	{
		this.provider = provider;
		this.needsCredentials = provider.isNeedsCredentials();
	}

	@Override
	public Map<String, Dataflow> getDataflows() throws SdmxException
	{
		Map<String, Dataflow> result = null;
		URL query = buildFlowQuery(ALL_KEYWORD, ALL_KEYWORD, LATEST_VERSION);
		List<Dataflow> flows = runQuery(new DataflowParser(), query, "dataflow_all", getName(), handleHttpHeaders("application/vnd.sdmx.structure+xml;version=2.1"));
		if (flows.size() > 0)
		{
			result = new HashMap<>();
			for (Dataflow dataflow : flows)
				result.put(dataflow.getFullIdentifier(), dataflow);
		}
		else
			throw new SdmxXmlContentException("The query returned zero dataflows");

		return result;
	}

	@Override
	public Dataflow getDataflow(String dataflow, String agency, String version) throws SdmxException
	{
		Dataflow result = null;
		if(agency == null) agency = ALL_KEYWORD;
		if(version == null) version = this.LATEST_VERSION;
		URL query = buildFlowQuery(dataflow, agency, version);
		List<Dataflow> flows = runQuery(new DataflowParser(), query, getName(), "dataflow_" + dataflow, handleHttpHeaders("application/vnd.sdmx.structure+xml;version=2.1"));
		if (flows.size() >= 1)
			result = flows.get(0);
		else
			throw new SdmxXmlContentException("The query returned zero dataflows");

		return result;
	}

	@Override
	public DataFlowStructure getDataFlowStructure(SDMXReference dsd, boolean full) throws SdmxException
	{
		if (dsd == null)
			throw new SdmxInvalidParameterException("getDataFlowStructure(): Null dsd in input");
		else
		{
			URL query = buildDSDQuery(dsd.getId(), dsd.getAgency(), dsd.getVersion(), full);
			return runQuery(new DataStructureParser(), query, getName(), "datastructure_" + dsd.getId(), handleHttpHeaders("application/vnd.sdmx.structure+xml;version=2.1")).get(0);
		}
	}

	@Override
	public Codelist getCodes(String codeList, String agency, String version) throws SdmxException
	{
		URL query = buildCodelistQuery(codeList, agency, version);
		return runQuery(new CodelistParser(), query, getName(), "codelist_" + codeList, handleHttpHeaders("application/vnd.sdmx.structure+xml;version=2.1"));
	}

	@Override
	public List<PortableTimeSeries<Double>> getTimeSeries(Dataflow dataflow, DataFlowStructure dsd, String resource, 
			String startTime, String endTime,
			boolean serieskeysonly, String updatedAfter, boolean includeHistory) throws SdmxException
	{
		return postProcess(getData(dataflow, dsd, resource, startTime, endTime, serieskeysonly, updatedAfter, includeHistory));
	}

	@Override
	public List<PortableTimeSeries<Double>> getTimeSeries(Dataflow dataflow, DataFlowStructure dsd, String resource, String filter, 
			String startTime, String endTime, 
			String attributes, String measures, String updatedAfter, boolean includeHistory) throws SdmxException {
		throw new SdmxInvalidParameterException("This method can only be called on SDMX V3 providers.");
	}

	@Override
	public Map<String, List<String>> getAvailableCubeRegion(Dataflow dataflow, String filter, String mode) throws SdmxException {
		throw new SdmxInvalidParameterException("This method can only be called on SDMX V3 providers.");
	}

	@Override
	public Integer getAvailableTimeSeriesNumber(Dataflow dataflow, String filter) throws SdmxException {
		throw new SdmxInvalidParameterException("This method can only be called on SDMX V3 providers.");
	}

	protected DataParsingResult getData(Dataflow dataflow, DataFlowStructure dsd, String resource, String startTime, String endTime, boolean serieskeysonly,
			String updatedAfter, boolean includeHistory) throws SdmxException
	{
		URL query = buildDataQuery(dataflow, resource, startTime, endTime, serieskeysonly, updatedAfter, includeHistory);
		String dumpName = "data_" + dataflow.getId() + "_" + resource; //.replaceAll("\\p{Punct}", "_");
		DataParsingResult ts = runQuery(new CompactDataParser(dsd, dataflow, !serieskeysonly), query,
				getName(), dumpName, handleHttpHeaders("application/vnd.sdmx.structurespecificdata+xml;version=2.1"));
		
		Message msg = ts.getMessage();
		if (msg != null)
		{
			LOGGER.log(Level.INFO, "The sdmx call returned messages in the footer:\n {0}", msg);
			RestSdmxEvent event = new DataFooterMessageEvent(query, msg);
			QueryRunner.getDataFooterMessageEventListener().onSdmxEvent(event);
		}
		return ts;
	}

	@Override
	public boolean needsCredentials()
	{
		return needsCredentials;
	}

	@Override
	public void setCredentials(String user, String pw)
	{
		this.user = user;
		this.pw = pw;
		this.needsCredentials = false;
		this.containsCredentials = true;
	}

	@Override
	public String getName()
	{
		return provider.getName();
	}

	public String getSdmxVersion()
	{
		return sdmxVersion.toString();
	}

	@Override
	public String buildDataURL(Dataflow dataflow, String resource, String startTime, String endTime, boolean seriesKeyOnly, String updatedAfter,
			boolean includeHistory) throws SdmxException
	{
		return buildDataQuery(dataflow, resource, startTime, endTime, seriesKeyOnly, updatedAfter, includeHistory).toString();
	}

	protected URL buildDataQuery(Dataflow dataflow, String resource, String startTime, String endTime, boolean serieskeysonly, String updatedAfter, boolean includeHistory) throws SdmxException
	{
		if (provider.getEndpoint() != null && dataflow != null && resource != null && !resource.isEmpty())
			return Sdmx21Queries
					.createDataQuery(provider.getEndpoint(), dataflow.getFullIdentifier(), resource, startTime, endTime, serieskeysonly, updatedAfter, includeHistory, null)
					.buildSdmx21Query();
		else
			throw new SdmxInvalidParameterException("Invalid query parameters: dataflow=" + dataflow + " resource=" + resource + " endpoint=" + provider.getEndpoint());
	}

	protected URL buildDSDQuery(String dsd, String agency, String version, boolean full) throws SdmxException
	{
		if (provider.getEndpoint() != null && agency != null && !agency.isEmpty() && dsd != null && !dsd.isEmpty())
			return Sdmx21Queries.createStructureQuery(provider.getEndpoint(), dsd, agency, version, full).buildSdmx21Query();
		else
			throw new RuntimeException("Invalid query parameters: agency=" + agency + " dsd=" + dsd + " endpoint=" + provider.getEndpoint());
	}

	protected URL buildFlowQuery(String dataflow, String agency, String version) throws SdmxException
	{
		return Sdmx21Queries.createDataflowQuery(provider.getEndpoint(), dataflow, agency, version).buildSdmx21Query();
	}

	protected URL buildCodelistQuery(String codeList, String agency, String version) throws SdmxException
	{
		return Sdmx21Queries.createCodelistQuery(provider.getEndpoint(), codeList, agency, version).buildSdmx21Query();
	}

	/**
	 * Override this method in subclasses to perform some post processing of the retrieved series.
	 * 
	 * @param result The retrieved series for a given query.
	 * @return The processed series.
	 */
	protected List<PortableTimeSeries<Double>> postProcess(DataParsingResult result)
	{
		return result;
	}

	public Map<String, String> handleHttpHeaders(String acceptHeader)
	{
		Map<String, String> headers = new HashMap<>();
		String lList = Configuration.getLanguages().stream()
			.map(lr -> String.format(Locale.US, "%s;q=%.1f", lr.getRange(), lr.getWeight()))
			.collect(joining(","));
		
		headers.put("Accept-Language", lList);
		if (containsCredentials)
		{
			LOGGER.fine("Setting http authorization");
			// https://stackoverflow.com/questions/1968416/how-to-do-http-authentication-in-android/1968873#1968873
			//String auth = Base64.encodeToString((user + ":" + pw).getBytes(), Base64.NO_WRAP);
			String auth = java.util.Base64.getEncoder().encodeToString((user + ":" + pw).getBytes());
			headers.put("Authorization", "Basic " + auth);
		}
		
		if (provider.isSupportsCompression())
			headers.put("Accept-Encoding", "gzip,deflate");

		if (acceptHeader != null && !"".equals(acceptHeader))
			headers.put("Accept", acceptHeader);
		else
			headers.put("Accept", "*/*");
		
		return headers;
	}
}
