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

import it.bancaditalia.oss.sdmx.api.DSDIdentifier;
import it.bancaditalia.oss.sdmx.api.DataFlowStructure;
import it.bancaditalia.oss.sdmx.api.Dataflow;
import it.bancaditalia.oss.sdmx.client.SdmxClientHandler;
import it.bancaditalia.oss.sdmx.parser.v20.DataStructureParser;
import it.bancaditalia.oss.sdmx.parser.v21.RestQueryBuilder;
import it.bancaditalia.oss.sdmx.util.Configuration;
import it.bancaditalia.oss.sdmx.util.SdmxException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Attilio Mattiocco
 *
 */
public abstract class DotStat extends RestSdmx20Client{
		
	protected static Logger logger = Configuration.getSdmxLogger();
	
	public DotStat(String name, URL endpoint, boolean needsCredentials, String format) throws MalformedURLException{
		super(name, endpoint, needsCredentials, null, format);
	}
	public DotStat(String name, URL endpoint, boolean needsCredentials) throws MalformedURLException{
		super(name, endpoint, needsCredentials, null, "compact_v2");
	}


	@Override
	public Dataflow getDataflow(String dataflow, String agency, String version) throws SdmxException {
		// OECD (and .Stat infrastructure) does not handle flows. We simulate it
		String query=null;
		InputStreamReader xmlStream = null;
		Dataflow result = null;
		query = buildFlowQuery(dataflow, SdmxClientHandler.ALL_AGENCIES, SdmxClientHandler.LATEST_VERSION );
		xmlStream = runQuery(query, null);
		if(xmlStream!=null){
			try {
				List<DataFlowStructure> dsds = DataStructureParser.parse(xmlStream);
				if(dsds.size() > 0){
					DataFlowStructure dsd = dsds.get(0);
					result = new Dataflow();
					result.setAgency(dsd.getAgency());
					result.setId(dsd.getId());
					result.setVersion(dsd.getVersion());
					result.setName(dsd.getName());
					DSDIdentifier dsdId = new  DSDIdentifier();
					dsdId.setAgency(dsd.getAgency());
					dsdId.setId(dsd.getId());
					dsdId.setVersion(dsd.getVersion());
					result.setDsdIdentifier(dsdId);
				}
				else{
					throw new SdmxException("The query returned zero dataflows");
				}
			} catch (Exception e) {
				logger.severe("Exception caught parsing results from call to provider " + name);
				logger.log(Level.FINER, "Exception: ", e);
				throw new SdmxException("Exception. Class: " + e.getClass().getName() + " .Message: " + e.getMessage());
			} finally{
				try {
					xmlStream.close();
				} catch (IOException e) {
					logger.severe("Exception caught closing stream.");
				}
			}
		}
		else{
			throw new SdmxException("The query returned a null stream");
		}
		return result;

	}

	@Override
	public Map<String, Dataflow> getDataflows() throws SdmxException {
		// OECD (and .Stat infrastructure) does not handle flows. We simulate it
		String query=null;
		InputStreamReader xmlStream = null;
		Map<String, Dataflow> result = new HashMap<String, Dataflow>();
		query = buildFlowQuery("ALL", SdmxClientHandler.ALL_AGENCIES, SdmxClientHandler.LATEST_VERSION );
		xmlStream = runQuery(query, null);
		if(xmlStream!=null){
			try {
				List<DataFlowStructure> dsds = DataStructureParser.parse(xmlStream);
				if(dsds.size() > 0){
					result = new HashMap<String, Dataflow>();
					for (Iterator<DataFlowStructure> iterator = dsds.iterator(); iterator.hasNext();) {
						DataFlowStructure dsd = (DataFlowStructure) iterator.next();
						Dataflow df = new Dataflow();
						df.setAgency(dsd.getAgency());
						df.setId(dsd.getId());
						df.setVersion(dsd.getVersion());
						df.setName(dsd.getName());
						DSDIdentifier dsdId = new  DSDIdentifier();
						dsdId.setAgency(dsd.getAgency());
						dsdId.setId(dsd.getId());
						dsdId.setVersion(dsd.getVersion());
						df.setDsdIdentifier(dsdId);
						result.put(dsd.getId(), df);
					}
				}
				else{
					throw new SdmxException("The query returned zero dataflows");
				}
			} catch (Exception e) {
				logger.severe("Exception caught parsing results from call to provider " + name);
				logger.log(Level.FINER, "Exception: ", e);
				throw new SdmxException("Exception. Class: " + e.getClass().getName() + " .Message: " + e.getMessage());
			} finally{
				try {
					xmlStream.close();
				} catch (IOException e) {
					logger.severe("Exception caught closing stream.");
				}
			}
		}
		else{
			throw new SdmxException("The query returned a null stream");
		}
		return result;
	}
	
	@Override
	protected String buildFlowQuery(String flow, String agency, String version)  throws SdmxException{
		return(buildDSDQuery(flow, agency, version, false));
	}


	@Override
	protected String buildDSDQuery(String dsd, String agency, String version, boolean full){
		if( endpoint!=null  && dsd!=null && !dsd.isEmpty()){
	
			String query = endpoint + "/GetDataStructure/" + dsd;
			return query;
		}
		else{
			throw new RuntimeException("Invalid query parameters: dsd=" + dsd + " endpoint=" + endpoint);
		}
	}

	@Override
	protected String buildDataQuery(Dataflow dataflow, String resource, 
			String startTime, String endTime, 
			boolean serieskeysonly, String updatedAfter, boolean includeHistory) throws SdmxException{
		if( endpoint!=null && 
				dataflow!=null &&
				resource!=null && !resource.isEmpty()){
			
			// for OECD use the simple DF id
			String query = endpoint + "/GetData/" + dataflow.getId() + "/";
			query += resource ;
			
			//query=query+"?";
			//query += "&format=compact_v2";
			query += RestQueryBuilder.addParams(startTime, endTime, 
					serieskeysonly, updatedAfter, includeHistory, format);
			return query;
		}
		else{
			throw new RuntimeException("Invalid query parameters: dataflow=" + dataflow + " resource=" + resource + " endpoint=" + endpoint);
		}
	}
}
