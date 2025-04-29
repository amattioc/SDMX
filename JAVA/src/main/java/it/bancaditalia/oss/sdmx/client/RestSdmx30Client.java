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
package it.bancaditalia.oss.sdmx.client;

import static it.bancaditalia.oss.sdmx.api.SDMXVersion.V3;
import static it.bancaditalia.oss.sdmx.util.QueryRunner.runQuery;
import static it.bancaditalia.oss.sdmx.util.Utils.checkString;

import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import it.bancaditalia.oss.sdmx.api.DataFlowStructure;
import it.bancaditalia.oss.sdmx.api.Dataflow;
import it.bancaditalia.oss.sdmx.api.Dimension;
import it.bancaditalia.oss.sdmx.api.Message;
import it.bancaditalia.oss.sdmx.api.PortableTimeSeries;
import it.bancaditalia.oss.sdmx.client.RestSdmx30Client.Sdmx30Queries;
import it.bancaditalia.oss.sdmx.event.DataFooterMessageEvent;
import it.bancaditalia.oss.sdmx.event.RestSdmxEvent;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxInvalidParameterException;
import it.bancaditalia.oss.sdmx.parser.v21.CompactDataParser;
import it.bancaditalia.oss.sdmx.parser.v21.DataParsingResult;
import it.bancaditalia.oss.sdmx.parser.v30.AvailabilityParser;
import it.bancaditalia.oss.sdmx.parser.v30.SeriesCountParser;
import it.bancaditalia.oss.sdmx.util.QueryRunner;
import it.bancaditalia.oss.sdmx.util.RestQueryBuilder;

/**
 * @author Attilio Mattiocco
 *
 */
public class RestSdmx30Client extends AbstractRestSdmxClient<Sdmx30Queries>
{
	public static class Sdmx30Queries extends RestQueryBuilder<Sdmx30Queries>
	{
		public Sdmx30Queries(URI entryPoint)
		{
			super(entryPoint);
		}
		
		public Sdmx30Queries withTimeFilter(String start, String end)
		{
			String startFilter = start == null || start.isEmpty() ? "" : "ge:" + start;
			String endFilter = end == null || end.isEmpty() ? "" : "le:" + end;
			String sep = startFilter.isEmpty() || endFilter.isEmpty() ? "" : "+";
			
			String timeFilter = startFilter + sep + endFilter;

			return timeFilter.isEmpty() ? this : withParam("c[TIME_PERIOD]", timeFilter);
		}
	}

	public RestSdmx30Client(Provider provider)
	{
		super(provider, V3);
	}
	
	@Override
	protected Sdmx30Queries getBuilder(URI endpoint)
	{
		return new Sdmx30Queries(endpoint);
	}
	
	@Override
	public List<PortableTimeSeries<Double>> getTimeSeries(Dataflow dataflow, DataFlowStructure dsd, String tsKey, 
			String startTime, String endTime,
			boolean serieskeysonly, String updatedAfter, boolean includeHistory) throws SdmxException {
		// old V2 calls can be mapped to V3 ones
		return getTimeSeries(dataflow, dsd, tsKey, null, startTime, endTime, serieskeysonly ? "none" : "all", serieskeysonly ? "none" : "all", updatedAfter, includeHistory);
	}

	@Override
	public List<PortableTimeSeries<Double>> getTimeSeries(Dataflow dataflow, DataFlowStructure dsd, String tsKey, String filter, 
			String startTime, String endTime,
			String attributes, String measures, String updatedAfter, boolean includeHistory) throws SdmxException {
		return postProcess(getData(dataflow, dsd, tsKey, filter, startTime, endTime, attributes, measures, updatedAfter, includeHistory));
	}
	
	@Override
	public Map<String, List<String>> getAvailableCubeRegion(Dataflow dataflow, String filter, String mode) throws SdmxException {
		URL query = buildAvailabilityQuery(dataflow, filter, mode);
		return runQuery(new AvailabilityParser(), query, handleHttpHeaders("application/vnd.sdmx.structure+xml;version=2.1"));
	}

	@Override
	public Map<String, Integer> getAvailableTimeSeriesNumber(Dataflow dataflow, String filter) throws SdmxException {
		URL query = buildAvailabilityQuery(dataflow, filter, "exact");
		return runQuery(new SeriesCountParser(), query, handleHttpHeaders("application/vnd.sdmx.structure+xml;version=2.1"));
	}

	protected DataParsingResult getData(Dataflow dataflow, DataFlowStructure dsd, String tsKey, String filter, String startTime, String endTime, 
			String attributes, String measures, String updatedAfter, boolean includeHistory) throws SdmxException
	{
		if(tsKey != null && !tsKey.isEmpty()){ 
			if(tsKey.contains("+")){
				//if the key contains a plus it is an old sdmx2 query and the only way to map it is with a filter
				String mappedFilter = mapSDMX2KeytoSDMX3FIlter(tsKey, dsd);
				if(filter != null && !filter.isEmpty())
					filter += "&" + mappedFilter;
				else
					filter = mappedFilter;
				tsKey = null;
			}
			else{
				//just make sure wildcard character is ip to date  
				tsKey = tsKey.replace("..", ".*.").replace("..", ".*."); //twice for three dots
			}
		}

		URL query = buildDataQuery(dataflow, tsKey, filter, startTime, endTime, attributes, measures, updatedAfter, includeHistory);
		String dumpName = "data_" + dataflow.getId() + "_" + filter; //.replaceAll("\\p{Punct}", "_");
		DataParsingResult ts = runQuery(new CompactDataParser(dsd, dataflow, 
				!("none".equals(attributes) && "none".equals(measures)), includeHistory), query,
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
	
	private String mapSDMX2KeytoSDMX3FIlter(String tsKey, DataFlowStructure dsd) throws SdmxInvalidParameterException {
		String filter = "";
		Dimension[] dims = dsd.getDimensions().toArray(new Dimension[0]);
		String delims = "[.]";
		String[] tokens = tsKey.split(delims);
		if(tokens.length > dims.length)
			throw new SdmxInvalidParameterException("The ts key is not valid: the dataflow does not contain so many dimensions.");
		for (int i = 0; i < tokens.length; i++) {
			if(!tokens[i].isEmpty() &&  !tokens[i].equals("*")){
				filter  += (!filter.isEmpty() ? "&" : "") + "c[" + dims[i].getId() + "]=" + tokens[i].replace("+", ","); // EXR  A..EUR+USD..A' will be mapped to:  c[FREQ]=A&c[CURRENCY_DENOM]=EUR,USD&c[EXR_SUFFIX]=A
			}
		}
		return filter;
	}
	
	private URL buildDataQuery(Dataflow dataflow, String tsKey, String filter, String start, String end, 
			String attributes, String measures, String updatedAfter, boolean includeHistory) throws SdmxException
	{
		if (getProvider().getEndpoint() != null && dataflow != null)
		{
			Sdmx30Queries builder = getBuilder()
					.withFilter(filter)
					.withTimeFilter(start, end)
					.withParam("attributes", attributes)
					.withParam("measures", measures)
					.withParam("updatedAfter", updatedAfter)
					.withHistory(includeHistory)
					.addPath("data")
					.addPath("dataflow")
					.addPath(dataflow.getFullIdentifier().replace(",", "/"));
			
			if(tsKey != null && !tsKey.isEmpty())
				builder = builder.addPath(tsKey);
			
			return builder.build();
		}
		else 
			throw new RuntimeException("Invalid query parameters: dataflow=" + dataflow + " endpoint=" + getProvider().getEndpoint());
	}

	protected URL buildDSDQuery(String dsd, String agency, String version, boolean full) throws SdmxException
	{
		checkString(agency, "The name of the agency cannot be null");
		checkString(dsd, "The name of the data structure cannot be null");

		Sdmx30Queries query = getBuilder()
				.addPath("structure")
				.addPath("datastructure")
				.addPath(agency)
				.addPath(dsd);
		
		if (version != null && !version.isEmpty())
			query.addPath(version);
		if (full)
			query.withParam("references", "descendants");

		return query.build();
	}

	protected URL buildFlowQuery(String dataflow, String agency, String version) throws SdmxException
	{
		return getBuilder().addPath("structure").addPath("dataflow").withRef(agency, dataflow, version).build();
	}

	protected URL buildCodelistQuery(String codeList, String agency, String version) throws SdmxException
	{
		checkString(codeList, "The name of the codelist cannot be null");

		return getBuilder()
				.addPath("structure")
				.addPath("codelist")
				.withRef(agency, codeList, version)
				.build();
	}
	
	protected URL buildAvailabilityQuery(Dataflow dataflow, String filter, String mode) throws SdmxException
	{
		if (dataflow == null)
			throw new SdmxInvalidParameterException("Invalid query parameters: dataflow=" + dataflow + " filter=" + filter);

		return getBuilder()
				.withFilter(filter)
				.withParam("mode", mode)
				.addPath("availability")
				.addPath("dataflow")
				.addPath(dataflow.getFullIdentifier().replace(",", "/"))
				.build();
	}

	protected URL buildAvailabilityQueryByKey(Dataflow dataflow, String key, String mode) throws SdmxException
	{
		if (dataflow != null)
			return getBuilder()
					.withParam("mode", mode)
					.addPath("availability")
					.addPath("dataflow")
					.addPath(dataflow.getFullIdentifier().replace(",", "/"))
					.addPath(key)
					.build();
		else
			throw new SdmxInvalidParameterException("Invalid query parameters: dataflow=" + dataflow + " filter=" + key);
	}
}
