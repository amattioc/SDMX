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

import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import it.bancaditalia.oss.sdmx.api.DSDIdentifier;
import it.bancaditalia.oss.sdmx.api.DataFlowStructure;
import it.bancaditalia.oss.sdmx.api.Dataflow;
import it.bancaditalia.oss.sdmx.api.PortableTimeSeries;
import it.bancaditalia.oss.sdmx.client.RestSdmxClient;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxExceptionFactory;
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
		String query=null;
		Reader xmlStream = null;
		Map<String, Dataflow> result = new HashMap<String, Dataflow>();
		query = buildFlowQuery("ALL", null, null);
		xmlStream = runQuery(query, null);
		try {
			List<Dataflow> dfs = DataflowParser.parse(xmlStream);
			if(dfs.size() > 0){
				result = new HashMap<String, Dataflow>();
				for (Iterator<Dataflow> iterator = dfs.iterator(); iterator.hasNext();) {
					Dataflow df = (Dataflow) iterator.next();
					result.put(df.getId(), df);
				}
			}
			else{
				throw new SdmxXmlContentException("The query returned zero dataflows");
			}
		} catch (XMLStreamException e) {
			throw SdmxExceptionFactory.wrap(e);
		} finally{
			try {
				xmlStream.close();
			} catch (IOException e) {
				logger.severe("Exception caught closing stream.");
			}
		}
		return result;
	}

	@Override
	public Dataflow getDataflow(String dataflow, String agency, String version) throws SdmxException {
		String query=null;
		Reader xmlStream = null;
		Dataflow df = null;
		query = buildFlowQuery(dataflow, agency, version);
		xmlStream = runQuery(query, null);
		try {
			List<Dataflow> flows = DataflowParser.parse(xmlStream);
			if(flows.size() >= 1){
				for (Iterator<Dataflow> iterator = flows.iterator(); iterator.hasNext();) {
					Dataflow item = (Dataflow) iterator.next();
					if(item.getId().equalsIgnoreCase(dataflow)){
						df = item;
					}
				}
			}
			else{
				throw new SdmxXmlContentException("The query returned zero dataflows");
			}
		} catch (XMLStreamException e) {
			throw SdmxExceptionFactory.wrap(e);
		} finally{
			try {
				xmlStream.close();
			} catch (IOException e) {
				logger.severe("Exception caught closing stream.");
			}
		}
		return df;
	}

	@Override
	public DataFlowStructure getDataFlowStructure(DSDIdentifier dsd, boolean full) throws SdmxException {
		String query=null;
		Reader xmlStream = null;
		DataFlowStructure str = new DataFlowStructure();
		if(dsd!=null){
			query = buildDSDQuery(dsd.getId(), dsd.getAgency(), dsd.getVersion(), full);
			xmlStream = runQuery(query, null);
			try {
				str = DataStructureParser.parse(xmlStream).get(0);
			} catch (XMLStreamException e) {
				throw SdmxExceptionFactory.wrap(e);
			} finally{
				try {
					xmlStream.close();
				} catch (IOException e) {
					logger.severe("Exception caught closing stream.");
				}
			}
		}
		else{
			throw new InvalidParameterException("Null dsd in input");
		}
		return str;
	
	}

	@Override
	public Map<String,String> getCodes(String codeList, String agency, String version) throws SdmxException {
		String query=null;
		Reader xmlStream = null;
		Map<String, String> result = null;
		query = buildCodelistQuery(codeList, agency, version);
		xmlStream = runQuery(query, null);
		try {
			result = CodelistParser.parse(xmlStream);
		} catch (XMLStreamException e) {
			throw SdmxExceptionFactory.wrap(e);
		} finally{
			try {
				xmlStream.close();
			} catch (IOException e) {
				logger.severe("Exception caught closing stream.");
			}
		}
		return result;
	}

	@Override
	public List<PortableTimeSeries> getTimeSeries(Dataflow dataflow, DataFlowStructure dsd, String resource, String startTime, String endTime, boolean serieskeysonly, String updatedAfter, boolean includeHistory) throws SdmxException {
		DataParsingResult ts = getData(dataflow, dsd, resource, startTime, endTime, serieskeysonly, updatedAfter, includeHistory);
		return ts.getData();
	}

	protected DataParsingResult getData(Dataflow dataflow, DataFlowStructure dsd, String resource, String startTime, String endTime, boolean serieskeysonly, String updatedAfter, boolean includeHistory) throws SdmxException {
		String query=null;
		Reader xmlStream = null;
		DataParsingResult ts = new DataParsingResult();
		query = buildDataQuery(dataflow, resource, startTime, endTime, serieskeysonly, updatedAfter, includeHistory);
		try {
			xmlStream = runQuery(query, acceptHdr);
			if(format != null){
				ts = CompactDataParser.parse(xmlStream, dsd, dataflow.getId(), !serieskeysonly);
			}
			else{
				// just for WB, to be removed ASAP
				ts = GenericDataParser.parse(xmlStream, dsd, dataflow.getId(), !serieskeysonly);
			}
		} catch (XMLStreamException e) {
			throw SdmxExceptionFactory.wrap(e);
		} finally{
			if (xmlStream != null) {
				try {
					xmlStream.close();
				} catch (IOException e) {
					logger.severe("Exception caught closing stream.");
				}
			}
		}
		return ts;
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
