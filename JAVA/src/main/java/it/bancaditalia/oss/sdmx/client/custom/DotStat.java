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
import static it.bancaditalia.oss.sdmx.util.Utils.checkString;

import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import it.bancaditalia.oss.sdmx.api.DataFlowStructure;
import it.bancaditalia.oss.sdmx.api.Dataflow;
import it.bancaditalia.oss.sdmx.client.Provider;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxInvalidParameterException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxXmlContentException;
import it.bancaditalia.oss.sdmx.parser.v20.DataStructureParser;
import it.bancaditalia.oss.sdmx.util.Configuration;

/**
 * @author Attilio Mattiocco
 *
 */
public abstract class DotStat extends RestSdmx20Client
{

	protected static Logger logger = Configuration.getSdmxLogger();

	public DotStat(Provider p, String format)
	{
		super(p, null, format);
	}

	public DotStat(Provider p)
	{
		this(p, "compact_v2");
	}

	@Override
	public Dataflow getDataflow(String dataflow, String agency, String version) throws SdmxException
	{
		// OECD (and .Stat infrastructure) does not handle flows. We simulate it
		URL query = buildFlowQuery(dataflow, getAllKeyword(), getLatestKeyword());
		List<DataFlowStructure> dsds = runQuery(new DataStructureParser(), query, null);
		if (dsds.size() > 0)
		{
			DataFlowStructure dsd = dsds.get(0);
			Dataflow result = new Dataflow(dsd, dsd.getName());
			result.setDsdIdentifier(dsd);
			return result;
		}
		else
			throw new SdmxXmlContentException("The query returned zero dataflows");
	}

	@Override
	public Map<String, Dataflow> getDataflows() throws SdmxException
	{
		// OECD (and .Stat infrastructure) does not handle flows. We simulate it
		URL query = buildFlowQuery("ALL", getAllKeyword(), getLatestKeyword());
		List<DataFlowStructure> dsds = runQuery(new DataStructureParser(), query, null);
		if (dsds.size() > 0)
		{
			Map<String, Dataflow> result = new HashMap<>();
			for (Iterator<DataFlowStructure> iterator = dsds.iterator(); iterator.hasNext();)
			{
				DataFlowStructure dsd = (DataFlowStructure) iterator.next();
				Dataflow df = new Dataflow(dsd, dsd.getName());
				df.setDsdIdentifier(dsd);
				result.put(dsd.getId(), df);
			}

			return result;
		}
		else
			throw new SdmxXmlContentException("The query returned zero dataflows");
	}

	@Override
	protected URL buildFlowQuery(String flow, String agency, String version) throws SdmxException
	{
		return (buildDSDQuery(flow, agency, version, false));
	}

	@Override
	protected URL buildDSDQuery(String dsd, String agency, String version, boolean full) throws SdmxException
	{
		checkString(dsd, "The name of the data structure cannot be null");

		return getBuilder().addPath("GetDataStructure").addPath(dsd).build();
	}

	@Override
	protected URL buildDataQuery(Dataflow dataflow, String tsKey, String startTime, String endTime, boolean serieskeysonly, String updatedAfter, boolean includeHistory, String format) throws SdmxException
	{
		checkString(tsKey, "The ts key must have valid values");

		// for OECD use the simple DF id
		if (dataflow != null)
			return getBuilder()
					.addPath("GetData")
					.addPath(dataflow.getId())
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
