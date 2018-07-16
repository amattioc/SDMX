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

import java.net.URI;
import java.net.URL;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import it.bancaditalia.oss.sdmx.api.DSDIdentifier;
import it.bancaditalia.oss.sdmx.api.DataFlowStructure;
import it.bancaditalia.oss.sdmx.api.Dataflow;
import it.bancaditalia.oss.sdmx.client.RestSdmxClient;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxXmlContentException;
import it.bancaditalia.oss.sdmx.parser.v20.CodelistParser;
import it.bancaditalia.oss.sdmx.parser.v20.DataStructureParser;
import it.bancaditalia.oss.sdmx.parser.v20.DataflowParser;
import it.bancaditalia.oss.sdmx.parser.v21.CompactDataParser;
import it.bancaditalia.oss.sdmx.parser.v21.DataParsingResult;
import it.bancaditalia.oss.sdmx.parser.v21.Sdmx21Queries;

public abstract class RestSdmx20Client extends RestSdmxClient
{

	private String		acceptHdr	= null;
	protected String	format		= "compact_v2";

	public RestSdmx20Client(String name, URI endpoint, boolean needsCredentials, String acceptHdr, String format)
	{
		super(name, endpoint, needsCredentials, false, false);
		this.acceptHdr = acceptHdr;
		this.format = format;
	}

	@Override
	public Map<String, Dataflow> getDataflows() throws SdmxException
	{

		URL query = buildFlowQuery("ALL", null, null);
		List<Dataflow> dfs = runQuery(new DataflowParser(), query, null, null);
		if (dfs.size() > 0)
		{
			Map<String, Dataflow> result = new HashMap<>();
			for (Iterator<Dataflow> iterator = dfs.iterator(); iterator.hasNext();)
			{
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
		URL query = buildFlowQuery(dataflow, agency, version);
		List<Dataflow> flows = runQuery(new DataflowParser(), query, null, null);
		if (flows.size() >= 1)
			for (Dataflow item : flows)
				if (item.getId().equalsIgnoreCase(dataflow))
					return item;

		throw new SdmxXmlContentException("The query returned zero dataflows");
	}

	@Override
	public DataFlowStructure getDataFlowStructure(DSDIdentifier dsd, boolean full) throws SdmxException
	{
		if (dsd != null)
		{
			URL query = buildDSDQuery(dsd.getId(), dsd.getAgency(), dsd.getVersion(), full);
			return runQuery(new DataStructureParser(), query, null, null).get(0);
		}
		else
			throw new InvalidParameterException("Null dsd in input");
	}

	@Override
	public Map<String, String> getCodes(String codeList, String agency, String version) throws SdmxException
	{
		URL query = buildCodelistQuery(codeList, agency, version);
		return runQuery(new CodelistParser(), query, null, null);
	}

	@Override
	protected DataParsingResult getData(Dataflow dataflow, DataFlowStructure dsd, String resource, String startTime, String endTime, boolean serieskeysonly,
			String updatedAfter, boolean includeHistory) throws SdmxException
	{
		URL query = buildDataQuery(dataflow, resource, startTime, endTime, serieskeysonly, updatedAfter, includeHistory);
		// 20/09/2017: GenericDataParser deleted
		return runQuery(/* format != null ? */new CompactDataParser(dsd, dataflow, !serieskeysonly) 
				/* : new GenericDataParser(dsd, dataflow, !serieskeysonly) */, query, acceptHdr, null);
	}

	@Override
	protected URL buildDataQuery(Dataflow dataflow, String resource, String startTime, String endTime, boolean serieskeysonly, String updatedAfter,
			boolean includeHistory) throws SdmxException
	{
		if (endpoint != null && dataflow != null && resource != null && !resource.isEmpty())
		{

			return Sdmx21Queries
					.createDataQuery(endpoint, dataflow.getFullIdentifier(), resource, startTime, endTime, serieskeysonly, updatedAfter, includeHistory, format)
					.buildSdmx21Query();
		}
		else
		{
			throw new RuntimeException("Invalid query parameters: dataflow=" + dataflow + " resource=" + resource + " endpoint=" + endpoint);
		}
	}

}
