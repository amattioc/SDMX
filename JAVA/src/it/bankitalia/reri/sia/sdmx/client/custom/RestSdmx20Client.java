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

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import it.bankitalia.reri.sia.sdmx.api.DSDIdentifier;
import it.bankitalia.reri.sia.sdmx.api.DataFlowStructure;
import it.bankitalia.reri.sia.sdmx.api.Dataflow;
import it.bankitalia.reri.sia.sdmx.api.PortableTimeSeries;
import it.bankitalia.reri.sia.sdmx.client.RestSdmxClient;
import it.bankitalia.reri.sia.sdmx.parser.v20.CodelistParser;
import it.bankitalia.reri.sia.sdmx.parser.v20.DataStructureParser;
import it.bankitalia.reri.sia.sdmx.parser.v20.DataflowParser;
import it.bankitalia.reri.sia.sdmx.parser.v20.GenericDataParser;
import it.bankitalia.reri.sia.sdmx.parser.v21.RestQueryBuilder;
import it.bankitalia.reri.sia.util.SdmxException;

public class RestSdmx20Client extends RestSdmxClient{

	public RestSdmx20Client(String name, URL endpoint, boolean needsCredentials, boolean dotStat) {
		super(name, endpoint, needsCredentials, dotStat);
	}
	
	@Override
	public Map<String, String> getDataflows() throws SdmxException {
		String query=null;
		String xml = null;
		Map<String, String> result = new HashMap<String, String>();
		query = buildFlowQuery(wsEndpoint, "ALL", null, null);
		xml = runQuery(query, null);
		if(xml!=null && !xml.isEmpty()){
			logger.finest(xml);
			try {
				List<Dataflow> dfs = DataflowParser.parse(xml);
				if(dfs.size() > 0){
					result = new HashMap<String, String>();
					for (Iterator<Dataflow> iterator = dfs.iterator(); iterator.hasNext();) {
						Dataflow df = (Dataflow) iterator.next();
						result.put(df.getId(), df.getName());
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
	public DSDIdentifier getDSDIdentifier(String dataflow, String agency, String version) throws SdmxException {
		String query=null;
		String xml = null;
		DSDIdentifier dsd = null;
		query = buildFlowQuery(wsEndpoint, dataflow, agency, version);
		xml = runQuery(query, null);
		if(xml!=null && !xml.isEmpty()){
			logger.finest(xml);
			try {
				List<Dataflow> flows = DataflowParser.parse(xml);
				if(flows.size() >= 1){
					Dataflow result = flows.get(0);
					dsd = result.getDsdIdentifier();
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
		return dsd;
	}

	@Override
	public DataFlowStructure getDataFlowStructure(DSDIdentifier dsd) throws SdmxException {
		String query=null;
		String xml = null;
		DataFlowStructure str = new DataFlowStructure();
		if(dsd!=null){
			query = buildStructureQuery(wsEndpoint, dsd.getId(), dsd.getAgency(), dsd.getVersion());
			xml = runQuery(query, null);
			if(xml!=null && !xml.isEmpty()){
				logger.finest(xml);
				try {
					str = DataStructureParser.parse(xml).get(0);
				} catch (Exception e) {
					logger.severe("Exception caught parsing results from call to provider " + name);
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
	public Map<String,String> getCodes(String provider, String codeList) throws SdmxException {
		String query=null;
		String xml = null;
		Map<String, String> result = null;
		query = buildCodelistQuery(wsEndpoint, codeList);
		xml = runQuery(query, null);
		if(xml!=null && !xml.isEmpty()){
			logger.finest(xml);
			try {
				result = CodelistParser.parse(xml);
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
				logger.severe("Exception caught parsing results from call to provider " + name);
				logger.log(Level.FINER, "Exception: ", e);
				throw new SdmxException("Exception. Class: " + e.getClass().getName() + " .Message: " + e.getMessage());
			}
		}
		else{
			throw new SdmxException("The query returned an empty result");
		}
		return ts;
	}
	
	protected String buildDataQuery(URL endpoint, String dataflow, String resource, String startTime, String endTime) throws SdmxException{
		throw new SdmxException("NOT IMPLEMENTED");
	}
	
	protected String buildStructureQuery(URL endpoint, String agency, String dsd, String version) throws SdmxException{
		throw new SdmxException("NOT IMPLEMENTED");
	}
	
	protected String buildFlowQuery(URL endpoint, String agency, String dataflow, String version) throws SdmxException{
		throw new SdmxException("NOT IMPLEMENTED");
	}
		
}
