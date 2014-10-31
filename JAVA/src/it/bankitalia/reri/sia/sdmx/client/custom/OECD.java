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
package it.bankitalia.reri.sia.sdmx.client.custom;

import it.bankitalia.reri.sia.sdmx.api.DSDIdentifier;
import it.bankitalia.reri.sia.sdmx.api.DataFlowStructure;
import it.bankitalia.reri.sia.sdmx.api.Dataflow;
import it.bankitalia.reri.sia.sdmx.client.SdmxClientHandler;
import it.bankitalia.reri.sia.sdmx.parser.v20.DataStructureParser;
import it.bankitalia.reri.sia.util.Configuration;
import it.bankitalia.reri.sia.util.SdmxException;

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
public class OECD extends RestSdmx20Client{
		
	protected static Logger logger = Configuration.getSdmxLogger();
	
	public OECD() throws MalformedURLException{
		super("OECD", new URL("http://stats.oecd.org/restsdmx/sdmx.ashx/"), false, true);
	}


	@Override
	public Dataflow getDataflow(String dataflow, String agency, String version) throws SdmxException {
		// OECD does not handle flows. We simulate it
		String query=null;
		String xml = null;
		Dataflow result = null;
		query = buildFlowQuery(wsEndpoint, dataflow, SdmxClientHandler.ALL_AGENCIES, SdmxClientHandler.LATEST_VERSION );
		xml = runQuery(query, null);
		if(xml!=null && !xml.isEmpty()){
			logger.finest(xml);
			try {
				List<DataFlowStructure> dsds = DataStructureParser.parse(xml);
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
			}
		}
		else{
			throw new SdmxException("The query returned an empty result");
		}
		return result;

	}

	@Override
	public Map<String, Dataflow> getDataflows() throws SdmxException {
		String query=null;
		String xml = null;
		Map<String, Dataflow> result = new HashMap<String, Dataflow>();
		query = buildFlowQuery(wsEndpoint, "ALL", SdmxClientHandler.ALL_AGENCIES, SdmxClientHandler.LATEST_VERSION );
		xml = runQuery(query, null);
		if(xml!=null && !xml.isEmpty()){
			logger.finest(xml);
			try {
				List<DataFlowStructure> dsds = DataStructureParser.parse(xml);
				if(dsds.size() > 0){
					result = new HashMap<String, Dataflow>();
					for (Iterator<DataFlowStructure> iterator = dsds.iterator(); iterator.hasNext();) {
						DataFlowStructure dsd = (DataFlowStructure) iterator.next();
						// OECD does not handle flows. We simulate it
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
			}
		}
		else{
			throw new SdmxException("The query returned an empty result");
		}
		return result;
	}
	
	@Override
	protected String buildDataQuery(URL endpoint, Dataflow dataflow, String resource, String startTime, String endTime){
		if( endpoint!=null && 
				dataflow!=null &&
				resource!=null && !resource.isEmpty()){
			
			// for OECD use the simple DF id
			String query = endpoint + "/GetData/" + dataflow.getId() + "/";
			query += resource ;
			
			if(startTime != null && startTime.isEmpty()) startTime = null;
			if(endTime != null && endTime.isEmpty()) endTime = null;		
			if(startTime != null || endTime != null){
				query=query+"?";
				if(startTime != null){
					query=query+"startPeriod="+startTime;
				}
				if(startTime != null && endTime != null){
					query=query+"&";
				}
				if(endTime != null){
					query=query+"endPeriod="+endTime;
				}
			}
			return query;
		}
		else{
			throw new RuntimeException("Invalid query parameters: dataflow=" + dataflow + " resource=" + resource + " endpoint=" + endpoint);
		}
	}
	
	@Override
	protected String buildDSDQuery(URL endpoint, String dsd, String agency, String version){
		if( endpoint!=null  &&
				dsd!=null && !dsd.isEmpty()){

			String query = endpoint + "/GetDataStructure/" + dsd;
			return query;
		}
		else{
			throw new RuntimeException("Invalid query parameters: dsd=" + dsd + " endpoint=" + endpoint);
		}
	}
	
	@Override
	protected String buildFlowQuery(URL endpoint, String flow, String agency, String version)  throws SdmxException{
		return(buildDSDQuery(endpoint, flow, agency, version));
	}
}
