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

import java.net.MalformedURLException;
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
import it.bancaditalia.oss.sdmx.exceptions.SdmxExceptionFactory;
import it.bancaditalia.oss.sdmx.exceptions.SdmxInvalidParameterException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxXmlContentException;
import it.bancaditalia.oss.sdmx.parser.v20.DataStructureParser;
import it.bancaditalia.oss.sdmx.parser.v21.Sdmx21Queries;
import it.bancaditalia.oss.sdmx.util.Configuration;
import it.bancaditalia.oss.sdmx.util.RestQueryBuilder;

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
		URL query = buildFlowQuery(dataflow, ALL_KEYWORD, LATEST_VERSION);
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
		URL query = buildFlowQuery("ALL", ALL_KEYWORD, LATEST_VERSION);
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
		if (provider.getEndpoint() != null && dsd != null && !dsd.isEmpty())
		{
			try
			{
				return new RestQueryBuilder(provider.getEndpoint()).addPath("GetDataStructure").addPath(dsd).build();
			}
			catch (MalformedURLException e)
			{
				throw SdmxExceptionFactory.wrap(e);
			}
		}
		else
			throw new SdmxInvalidParameterException("Invalid query parameters: dsd=" + dsd + " endpoint=" + provider.getEndpoint());
	}

	@Override
	protected URL buildDataQuery(Dataflow dataflow, String resource, String startTime, String endTime, boolean serieskeysonly, String updatedAfter, boolean includeHistory) throws SdmxException
	{
		if (provider.getEndpoint() != null && dataflow != null && resource != null && !resource.isEmpty())
		{

			// for OECD use the simple DF id
			Sdmx21Queries query = (Sdmx21Queries) new Sdmx21Queries(provider.getEndpoint()).addPath("GetData").addPath(dataflow.getId()).addPath(resource);

			// query=query+"?";
			// query += "&format=compact_v2";
			return query.addParams(startTime, endTime, serieskeysonly, updatedAfter, includeHistory, format).buildSdmx21Query();
		}
		else
			throw new SdmxInvalidParameterException("Invalid query parameters: dataflow=" + dataflow + " resource=" + resource + " endpoint=" + provider.getEndpoint());
	}
}
