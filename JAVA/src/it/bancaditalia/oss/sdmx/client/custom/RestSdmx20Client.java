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
import it.bancaditalia.oss.sdmx.api.PortableTimeSeries;
import it.bancaditalia.oss.sdmx.client.RestSdmxClient;
import it.bancaditalia.oss.sdmx.parser.v20.CodelistParser;
import it.bancaditalia.oss.sdmx.parser.v20.DataStructureParser;
import it.bancaditalia.oss.sdmx.parser.v20.DataflowParser;
import it.bancaditalia.oss.sdmx.parser.v20.GenericDataParser;
import it.bancaditalia.oss.sdmx.util.SdmxException;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public abstract class RestSdmx20Client extends RestSdmxClient{

	public RestSdmx20Client(String name, URL endpoint, boolean needsCredentials) {
		super(name, endpoint, needsCredentials, false, false);
	}
	
	@Override
	public Map<String, Dataflow> getDataflows() throws SdmxException {
		String query=null;
		InputStreamReader xmlStream = null;
		Map<String, Dataflow> result = new HashMap<String, Dataflow>();
		query = buildFlowQuery(wsEndpoint, "ALL", null, null);
		xmlStream = runQuery(query, null);
		if(xmlStream!=null){
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
					throw new SdmxException("The query returned zero dataflows");
				}
			} catch (Exception e) {
				logger.severe("Exception caught parsing results from call to provider " + name);
				logger.log(Level.FINER, "Exception: ", e);
				throw new SdmxException("Exception. Class: " + e.getClass().getName() + " .Message: " + e.getMessage());
			}
		}
		else{
			throw new SdmxException("The query returned anull stream");
		}
		return result;
	}

	@Override
	public Dataflow getDataflow(String dataflow, String agency, String version) throws SdmxException {
		String query=null;
		InputStreamReader xmlStream = null;
		Dataflow df = null;
		query = buildFlowQuery(wsEndpoint, dataflow, agency, version);
		xmlStream = runQuery(query, null);
		if(xmlStream!=null){
			try {
				List<Dataflow> flows = DataflowParser.parse(xmlStream);
				if(flows.size() >= 1){
					df = flows.get(0);
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
			throw new SdmxException("The query returned a null stream");
		}
		return df;
	}

	@Override
	public DataFlowStructure getDataFlowStructure(DSDIdentifier dsd) throws SdmxException {
		String query=null;
		InputStreamReader xmlStream = null;
		DataFlowStructure str = new DataFlowStructure();
		if(dsd!=null){
			query = buildDSDQuery(wsEndpoint, dsd.getId(), dsd.getAgency(), dsd.getVersion());
			xmlStream = runQuery(query, null);
			if(xmlStream!=null){
				try {
					str = DataStructureParser.parse(xmlStream).get(0);
				} catch (Exception e) {
					logger.severe("Exception caught parsing results from call to provider " + name);
					logger.log(Level.FINER, "Exception: ", e);
					throw new SdmxException("Exception. Class: " + e.getClass().getName() + " .Message: " + e.getMessage());
				}
			}
			else{
				throw new SdmxException("The query returned a null stream");
			}
		}
		else{
			throw new SdmxException("Null dsd in input");
		}
		return str;
	
	}

	@Override
	public Map<String,String> getCodes(String provider, String codeList, String agency, String version) throws SdmxException {
		String query=null;
		InputStreamReader xmlStream = null;
		Map<String, String> result = null;
		query = buildCodelistQuery(wsEndpoint, codeList, agency, version);
		xmlStream = runQuery(query, null);
		if(xmlStream!=null){
			try {
				result = CodelistParser.parse(xmlStream);
			} catch (Exception e) {
				logger.severe("Exception caught parsing results from call to provider " + name);
				logger.log(Level.FINER, "Exception: ", e);
				throw new SdmxException("Exception. Class: " + e.getClass().getName() + " .Message: " + e.getMessage());
			}
			
		}
		else{
			throw new SdmxException("The query returned a null stream");
		}
		return result;
	}

	@Override
	public List<PortableTimeSeries> getTimeSeries(Dataflow dataflow, DataFlowStructure dsd, String resource, String startTime, String endTime) throws SdmxException {
		String query=null;
		InputStreamReader xmlStream = null;
		List<PortableTimeSeries> ts = new ArrayList<PortableTimeSeries>();
		query = buildDataQuery(wsEndpoint, dataflow, resource, startTime, endTime);
		xmlStream = runQuery(query, null);
		if(xmlStream!=null){
			try {
				//ts = CompactDataParser.parse(xmlStream, dsd, dataflow.getId());
				ts = GenericDataParser.parse(xmlStream, dsd, dataflow.getId());
			} catch (Exception e) {
				logger.severe("Exception caught parsing results from call to provider " + name);
				logger.log(Level.FINER, "Exception: ", e);
				throw new SdmxException("Exception. Class: " + e.getClass().getName() + " .Message: " + e.getMessage());
			}
		}
		else{
			throw new SdmxException("The query returned a null stream");
		}
		return ts;
	}
		
}
