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

import static it.bancaditalia.oss.sdmx.api.SDMXVersion.V3;
import static it.bancaditalia.oss.sdmx.client.Provider.AuthenticationMethods.NONE;
import static it.bancaditalia.oss.sdmx.util.QueryRunner.runQuery;
import static it.bancaditalia.oss.sdmx.util.Utils.checkString;
import static java.util.stream.Collectors.joining;

import java.net.URI;
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
import it.bancaditalia.oss.sdmx.client.Provider.AuthenticationMethods;
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
import it.bancaditalia.oss.sdmx.parser.v30.AvailabilityParser;
import it.bancaditalia.oss.sdmx.util.Configuration;
import it.bancaditalia.oss.sdmx.util.QueryRunner;
import it.bancaditalia.oss.sdmx.util.RestQueryBuilder;

/**
 * @author Attilio Mattiocco
 *
 */
public abstract class AbstractRestSdmxClient<T extends RestQueryBuilder<T>> implements GenericSDMXClient
{
	protected static final Logger LOGGER = Configuration.getSdmxLogger();
	
	private final SDMXVersion sdmxVersion;
	private final Provider provider;
	private final String latestKeyword;
	private final String allKeyword;

	protected AuthenticationMethods authMethod = NONE;
	private Credentials credentials = null;

	public AbstractRestSdmxClient(Provider provider, SDMXVersion sdmxVersion)
	{
		this.provider = provider;
		this.authMethod = provider.getAuthMethod();
		this.sdmxVersion = sdmxVersion;
		
		if (sdmxVersion == V3)
		{
			latestKeyword = "+";
			allKeyword = "*";
		}
		else
		{
			latestKeyword = "latest";
			allKeyword = "all";
		}
	}

	public Provider getProvider()
	{
		return provider;
	}
	
	public boolean isAuthDone(){
		return credentials != null;
	}

	@Override
	public Map<String, Dataflow> getDataflows() throws SdmxException
	{
		Map<String, Dataflow> result = null;
		URL query = buildFlowQuery(allKeyword, allKeyword, latestKeyword);
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
		if(agency == null) agency = allKeyword;
		if(version == null) version = latestKeyword;
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
	public Map<String, List<String>> getAvailableCubeRegion(Dataflow dataflow, String filter, String mode) throws SdmxException
	{
		// workaround for 2.1 specs defect 
		if(filter.equals(".."))
			filter = "ALL";
		URL query = buildAvailabilityQueryByKey(dataflow, filter);
		return runQuery(new AvailabilityParser(), query, handleHttpHeaders("application/vnd.sdmx.structure+xml;version=2.1"));
	}

	@Override
	public Map<String, Integer> getAvailableTimeSeriesNumber(Dataflow dataflow, String filter) throws SdmxException {
		throw new SdmxInvalidParameterException("This method can only be called on SDMX V3 providers.");
	}

	protected DataParsingResult getData(Dataflow dataflow, DataFlowStructure dsd, String resource, String startTime, String endTime, boolean serieskeysonly,
			String updatedAfter, boolean includeHistory) throws SdmxException
	{
		URL query = buildDataQuery(dataflow, resource, startTime, endTime, serieskeysonly, updatedAfter, includeHistory, null);
		String dumpName = "data_" + dataflow.getId() + "_" + resource; //.replaceAll("\\p{Punct}", "_");
		DataParsingResult ts = runQuery(new CompactDataParser(dsd, dataflow, !serieskeysonly, includeHistory), query,
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
	public AuthenticationMethods getAuthMethod()
	{
		return authMethod;
	}

	@Override
	public void setCredentials(Credentials credentials)
	{
		this.authMethod = NONE;
		this.credentials = credentials;
	}

	@Override
	public String getName()
	{
		return getProvider().getName();
	}

	public String getSdmxVersion()
	{
		return sdmxVersion.toString();
	}

	public final String buildDataURL(Dataflow dataflow, String resource, String startTime, String endTime, boolean seriesKeyOnly, String updatedAfter,
			boolean includeHistory) throws SdmxException
	{
		return buildDataQuery(dataflow, resource, startTime, endTime, seriesKeyOnly, updatedAfter, includeHistory, null).toString();
	}

	protected URL buildAvailabilityQueryByKey(Dataflow dataflow, String key) throws SdmxException
	{
		if (dataflow != null)
			return getBuilder().addPath("availableconstraint").addPath(dataflow.getId()).addPath(key).build();
		else
			throw new SdmxInvalidParameterException("Invalid query parameters: dataflow=" + dataflow + " filter=" + key);
	}

	protected URL buildDataQuery(Dataflow dataflow, String tsKey, String start, String end, boolean serieskeysonly, String updatedAfter, boolean includeHistory, String format) throws SdmxException
	{
		checkString(tsKey, "The timeslot for an observation cannot be null or empty.");
		
		if (dataflow != null)
			return getBuilder()
					.withParam("startPeriod", start)
					.withParam("endPeriod", end)
					.withDetail(serieskeysonly)
					.withParam("updatedAfter", updatedAfter)
					.withHistory(includeHistory)
					.withParam("format", format)
					.addPath("data")
					.addPath(dataflow.getFullIdentifier())
					.addPath(tsKey)
					.build();
		else
			throw new SdmxInvalidParameterException("Invalid query parameters: dataflow=" + dataflow + " tsKey=" + tsKey + " endpoint=" + getProvider().getEndpoint());
	}

	protected URL buildDSDQuery(String dsd, String agency, String version, boolean full) throws SdmxException
	{
		checkString(agency, "The name of the agency cannot be null");
		checkString(dsd, "The name of the data structure cannot be null");

		if (agency != null && !agency.isEmpty() && dsd != null && !dsd.isEmpty())
		{
			RestQueryBuilder<?> query = getBuilder()
					.addPath("datastructure")
					.addPath(agency)
					.addPath(dsd);
		
			if (version != null && !version.isEmpty())
				query = query.addPath(version);
			if (full)
				query = query.withParam("references", "descendants");
			
			return query.build();
		}
		else
			throw new RuntimeException("Invalid query parameters: agency=" + agency + " dsd=" + dsd + " endpoint=" + getProvider().getEndpoint());
	}
	
	protected URL buildFlowQuery(String dataflow, String agency, String version) throws SdmxException
	{
		return getBuilder()
				.addPath("dataflow")
				.withRef(agency, dataflow, version)
				.build();
	}

	protected URL buildCodelistQuery(String codeList, String agency, String version) throws SdmxException
	{
		return getBuilder().addPath("codelist").withRef(agency, codeList, version).build();
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

		if (credentials != null)
		{
			LOGGER.fine("Setting http authorization");
			headers.put("Authorization", credentials.getHeader());
		}
		
		if (getProvider().isSupportsCompression())
			headers.put("Accept-Encoding", "gzip,deflate");

		if (acceptHeader != null && !"".equals(acceptHeader))
			headers.put("Accept", acceptHeader);
		else
			headers.put("Accept", "*/*");
		headers.put("user-agent", "RJSDMX");
		return headers;
	}

	public String getLatestKeyword()
	{
		return latestKeyword;
	}

	public String getAllKeyword()
	{
		return allKeyword;
	}

	public T getBuilder()
	{
		return getBuilder(provider.getEndpoint());
	}

	protected abstract T getBuilder(URI endpoint);
}
