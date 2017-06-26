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

import java.net.URL;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import it.bancaditalia.oss.sdmx.api.DSDIdentifier;
import it.bancaditalia.oss.sdmx.api.DataFlowStructure;
import it.bancaditalia.oss.sdmx.api.Dataflow;
import it.bancaditalia.oss.sdmx.api.PortableTimeSeries;
import it.bancaditalia.oss.sdmx.client.RestSdmxClient;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxXmlContentException;
import it.bancaditalia.oss.sdmx.parser.v20.CodelistParser;
import it.bancaditalia.oss.sdmx.parser.v20.DataStructureParser;
import it.bancaditalia.oss.sdmx.parser.v20.DataflowParser;
import it.bancaditalia.oss.sdmx.parser.v20.GenericDataParser;
import it.bancaditalia.oss.sdmx.parser.v21.CompactDataParser;
import it.bancaditalia.oss.sdmx.parser.v21.DataParsingResult;
import it.bancaditalia.oss.sdmx.parser.v21.RestQueryBuilder;

public abstract class RestSdmx20Client extends RestSdmxClient{
	
	private String acceptHdr = null;
	protected String format = "compact_v2";

	public RestSdmx20Client(String name, URL endpoint, boolean needsCredentials, String acceptHdr, String format) {
		super(name, endpoint, needsCredentials, false, false);
		this.acceptHdr = acceptHdr;
		this.format = format;
	}
	
	@Override
	public Map<String, Dataflow> getDataflows() throws SdmxException {
		
		String query = buildFlowQuery("ALL", null, null);
		List<Dataflow> dfs = runQuery(new DataflowParser(), query, null);
		if(dfs.size() > 0)
		{
			Map<String, Dataflow> result = new HashMap<String, Dataflow>();
			for (Iterator<Dataflow> iterator = dfs.iterator(); iterator.hasNext();) {
				Dataflow df = (Dataflow) iterator.next();
				result.put(df.getId(), df);
			}
			return result;
		}
		else
			throw new SdmxXmlContentException("The query returned zero dataflows");
	}

	@Override
	public Dataflow getDataflow(String dataflow, String agency, String version) throws SdmxException 
	{
		String query = buildFlowQuery(dataflow, agency, version);
		List<Dataflow> flows = runQuery(new DataflowParser(), query, null);
		if(flows.size() >= 1)
			for (Dataflow item: flows)
				if(item.getId().equalsIgnoreCase(dataflow))
					return item;

		throw new SdmxXmlContentException("The query returned zero dataflows");
	}

	@Override
	public DataFlowStructure getDataFlowStructure(DSDIdentifier dsd, boolean full) throws SdmxException {
		if(dsd!=null){
			String query = buildDSDQuery(dsd.getId(), dsd.getAgency(), dsd.getVersion(), full);
			return runQuery(new DataStructureParser(), query, null).get(0);
		}
		else
			throw new InvalidParameterException("Null dsd in input");
	}

	@Override
	public Map<String,String> getCodes(String codeList, String agency, String version) throws SdmxException {
		String query = buildCodelistQuery(codeList, agency, version);
		return runQuery(new CodelistParser(), query, null);
	}

	@Override
	public List<PortableTimeSeries> getTimeSeries(Dataflow dataflow, DataFlowStructure dsd, String resource, String startTime, String endTime, boolean serieskeysonly, String updatedAfter, boolean includeHistory) throws SdmxException {
		return getData(dataflow, dsd, resource, startTime, endTime, serieskeysonly, updatedAfter, includeHistory).getData();
	}

	protected DataParsingResult getData(Dataflow dataflow, DataFlowStructure dsd, String resource, String startTime, String endTime, boolean serieskeysonly, String updatedAfter, boolean includeHistory) throws SdmxException {
		String query = buildDataQuery(dataflow, resource, startTime, endTime, serieskeysonly, updatedAfter, includeHistory);
		return runQuery(format != null ? new CompactDataParser(dsd, dataflow.getId(), !serieskeysonly) : 
				new GenericDataParser(dsd, dataflow.getId(), !serieskeysonly), query, acceptHdr);
	}

	@Override
	protected String buildDataQuery(Dataflow dataflow, String resource, String startTime, String endTime, 
			boolean serieskeysonly, String updatedAfter, boolean includeHistory) throws SdmxException {
		if( endpoint!=null && 
				dataflow!=null &&
				resource!=null && !resource.isEmpty()){

			String query = RestQueryBuilder.getDataQuery(endpoint, dataflow.getFullIdentifier(), resource, 
					startTime, endTime, serieskeysonly, updatedAfter, includeHistory, format);
			return query;
		}
		else{
			throw new RuntimeException("Invalid query parameters: dataflow=" + dataflow + 
					" resource=" + resource + " endpoint=" + endpoint);
		}
	}
		
}
