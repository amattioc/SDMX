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

import static it.bancaditalia.oss.sdmx.util.QueryRunner.runQuery;

import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import it.bancaditalia.oss.sdmx.api.Codelist;
import it.bancaditalia.oss.sdmx.api.DataFlowStructure;
import it.bancaditalia.oss.sdmx.api.Dataflow;
import it.bancaditalia.oss.sdmx.api.SDMXReference;
import it.bancaditalia.oss.sdmx.client.Provider;
import it.bancaditalia.oss.sdmx.client.RestSdmx21Client;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxInvalidParameterException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxXmlContentException;
import it.bancaditalia.oss.sdmx.parser.v20.DataStructureParser;
import it.bancaditalia.oss.sdmx.parser.v20.DataflowParser;
import it.bancaditalia.oss.sdmx.parser.v21.CompactDataParser;
import it.bancaditalia.oss.sdmx.parser.v21.DataParsingResult;

public abstract class RestSdmx20Client extends RestSdmx21Client
{

	private String		acceptHdr	= null;
	protected String	format		= "compact_v2";

	public RestSdmx20Client(Provider p, String acceptHdr, String format)
	{
		super(p);
		this.acceptHdr = acceptHdr;
		this.format = format;
	}

	@Override
	public Map<String, Dataflow> getDataflows() throws SdmxException
	{

		URL query = buildFlowQuery("ALL", null, null);
		List<Dataflow> dfs = runQuery(new DataflowParser(), query, null);
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
		List<Dataflow> flows = runQuery(new DataflowParser(), query, null);
		if (flows.size() >= 1)
			for (Dataflow item : flows)
				if (item.getId().equalsIgnoreCase(dataflow))
					return item;

		throw new SdmxXmlContentException("The query returned zero dataflows");
	}

	@Override
	public DataFlowStructure getDataFlowStructure(SDMXReference dsd, boolean full) throws SdmxException
	{
		if (dsd != null)
		{
			URL query = buildDSDQuery(dsd.getId(), dsd.getAgency(), dsd.getVersion(), full);
			return runQuery(new DataStructureParser(), query, null).get(0);
		}
		else
			throw new SdmxInvalidParameterException("Null dsd in input");
	}

	@Override
	public Codelist getCodes(String codeList, String agency, String version) throws SdmxException
	{
		throw new SdmxInvalidParameterException("This method can only be called on SDMX V2.1+ providers.");
	}

	@Override
	protected DataParsingResult getData(Dataflow dataflow, DataFlowStructure dsd, String resource, String startTime, String endTime, boolean serieskeysonly,
			String updatedAfter, boolean includeHistory) throws SdmxException
	{
		URL query = buildDataQuery(dataflow, resource, startTime, endTime, serieskeysonly, updatedAfter, includeHistory, null);
		return runQuery(new CompactDataParser(dsd, dataflow, !serieskeysonly, includeHistory), query, handleHttpHeaders(acceptHdr));
	}
}
