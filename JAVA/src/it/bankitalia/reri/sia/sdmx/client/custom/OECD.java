/* Copyright 2010,2014 Bank Of Italy
*
* Licensed under the EUPL, Version 1.1 or – as soon they
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
import it.bankitalia.reri.sia.sdmx.api.PortableTimeSeries;
import it.bankitalia.reri.sia.sdmx.client.RestSdmxClient;
import it.bankitalia.reri.sia.sdmx.parser.v20.DataStructureParser;
import it.bankitalia.reri.sia.sdmx.parser.v20.GenericDataParser;
import it.bankitalia.reri.sia.util.Configuration;
import it.bankitalia.reri.sia.util.SdmxException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
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
public class OECD extends RestSdmxClient{
		
	protected static Logger logger = Configuration.getSdmxLogger();
	
	public OECD() throws MalformedURLException{
		super("OECD", new URL("http://stats.oecd.org/restsdmx/sdmx.ashx/"), "OECD", false, true);
	}

	@Override
	public List<PortableTimeSeries> getTimeSeries(String dataflow, DataFlowStructure dsd, String resource, String startTime, String endTime) throws SdmxException {
		String query=null;
		String xml = null;
		List<PortableTimeSeries> ts = new ArrayList<PortableTimeSeries>();
		query = buildDataQuery(wsEndpoint, dataflow, resource, startTime, endTime);
		xml = runQuery(query, null);
		if(xml!=null && !xml.isEmpty()){
			logger.finest(xml);
			try {
				ts = GenericDataParser.parse(xml, dataflow);
			} catch (Exception e) {
				logger.severe("Exception caught parsing results from call to provider " + getAgency());
				logger.log(Level.FINER, "Exception: ", e);
				throw new SdmxException("Exception. Class: " + e.getClass().getName() + " .Message: " + e.getMessage());
			}
		}
		else{
			throw new SdmxException("The query returned an empty result");
		}
		return ts;
	}

	@Override
	public DataFlowStructure getDataFlowStructure(DSDIdentifier dsd) throws SdmxException {
		String query=null;
		String xml = null;
		DataFlowStructure str = new DataFlowStructure();
		if(dsd!=null){
			query = buildStructureQuery(wsEndpoint, dsd.getId());
			xml = runQuery(query, null);
			if(xml!=null && !xml.isEmpty()){
				logger.finest(xml);
				try {
					str = DataStructureParser.parse(xml).get(0);
				} catch (Exception e) {
					logger.severe("Exception caught parsing results from call to provider " + getAgency());
					logger.log(Level.FINER, "Exception: ", e);
					throw new SdmxException("Exception. Class: " + e.getClass().getName() + " .Message: " + e.getMessage());
				}
			}
			else{
				throw new SdmxException("The query returned an empty result");
			}
		}
		else{
			throw new SdmxException("Null dsd in input");
		}
		return str;

	}

	@Override
	public DSDIdentifier getDSDIdentifier(String dataflow, String version) throws SdmxException {
		//quick and dirty: OECD dataflow is is equal to DSD id
		DSDIdentifier dsd = new DSDIdentifier(dataflow, getAgency(), version);
		return dsd;
	}

	@Override
	public Map<String, String> getDataflows() throws SdmxException {
		String query=null;
		String xml = null;
		Map<String, String> result = new HashMap<String, String>();
		query = buildFlowQuery(wsEndpoint, "ALL");
		xml = runQuery(query, null);
		if(xml!=null && !xml.isEmpty()){
			logger.finest(xml);
			try {
				List<DataFlowStructure> dsds = DataStructureParser.parse(xml);
				if(dsds.size() > 0){
					result = new HashMap<String, String>();
					for (Iterator<DataFlowStructure> iterator = dsds.iterator(); iterator.hasNext();) {
						DataFlowStructure dsd = (DataFlowStructure) iterator.next();
						result.put(dsd.getId(), dsd.getName());
					}
				}
				else{
					throw new SdmxException("The query returned zero dataflows");
				}
			} catch (Exception e) {
				logger.severe("Exception caught parsing results from call to provider " + getAgency());
				logger.log(Level.FINER, "Exception: ", e);
				throw new SdmxException("Exception. Class: " + e.getClass().getName() + " .Message: " + e.getMessage());
			}
		}
		else{
			throw new SdmxException("The query returned an empty result");
		}
		return result;
	}
	
	protected String buildDataQuery(URL endpoint, String dataflow, String resource, String startTime, String endTime){
		if( endpoint!=null && 
				dataflow!=null && !dataflow.isEmpty() &&
				resource!=null && !resource.isEmpty()){

			String query = endpoint + "/GetData/" + dataflow + "/";
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
	
	protected String buildStructureQuery(URL endpoint, String dsd){
		if( endpoint!=null  &&
				dsd!=null && !dsd.isEmpty()){

			String query = endpoint + "/GetDataStructure/" + dsd;
			return query;
		}
		else{
			throw new RuntimeException("Invalid query parameters: dsd=" + dsd + " endpoint=" + endpoint);
		}
	}
	
	protected String buildFlowQuery(URL endpoint, String flow){
		return(buildStructureQuery(endpoint, flow));
	}
}
