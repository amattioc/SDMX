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
package it.bankitalia.reri.sia.sdmx.client;

import it.bankitalia.reri.sia.sdmx.api.DSDIdentifier;
import it.bankitalia.reri.sia.sdmx.api.DataFlowStructure;
import it.bankitalia.reri.sia.sdmx.api.Dimension;
import it.bankitalia.reri.sia.sdmx.api.GenericSDMXClient;
import it.bankitalia.reri.sia.sdmx.api.PortableTimeSeries;
import it.bankitalia.reri.sia.util.Configuration;
import it.bankitalia.reri.sia.util.LoginDialog;
import it.bankitalia.reri.sia.util.SdmxException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
/**
 * <p>Java class for optimizing interactions with the SdmxClients in non Java 
 * environment. It provides a sort fo 'session', storing the clients that are created 
 * and reusing them. It also provides caching of all key families retrieved.
 * 
 * @author Attilio Mattiocco
 *
 */
public class SdmxClientHandler {
	
	protected static Logger logger = Configuration.getSdmxLogger();
	private static final String sourceClass = SdmxClientHandler.class.getSimpleName();

	// we only manage 'latest' and 'all' as starting points, for now
	public static final String LATEST_VERSION = "latest";
	public static final String ALL_AGENCIES = "all";

	// key: provider name --> client
	private static Map<String, GenericSDMXClient> clients = new Hashtable<String, GenericSDMXClient>();

	public static boolean needsCredentials(String provider) throws SdmxException {
		return getClient(provider).needsCredentials();
	}
	
	public static void setCredentials(String provider, String user, String pw) throws SdmxException {
		getClient(provider, user, pw);
	}

	public static void addProvider(String name, String agency, String endpoint, boolean needsCredentials){
		URL ep;
		try {
			ep = new URL(endpoint);
			SDMXClientFactory.addProvider(name, ep, needsCredentials);
		} catch (MalformedURLException e) {
			logger.severe("Exception. Class: " + e.getClass().getName() + " .Message: " + e.getMessage());
			logger.log(Level.FINER, "", e);
		}
	}

	/**
	 * Get the list of all available SDMX Providers
	 * @return
	 */
	public static List<String> getProviders() {
		List<String> providers = new ArrayList<String>();
		Set<String> result = SDMXClientFactory.getProviders().keySet();
		if(result != null){
			providers = new ArrayList<String>(result);
		}
		return providers;
    }
	
	public static DataFlowStructure getDataFlowStructure(String provider, String dataflow) throws SdmxException {
		DataFlowStructure tmp = null;
		DSDIdentifier keyF = getDSDIdentifier(provider, dataflow);
		if(keyF != null){
			String fullkeyFamilyKey = keyF.getFullName();
			Provider p = getProvider(provider);
			tmp = p.getDSD(fullkeyFamilyKey);
			if(tmp == null){
				logger.info("DSD for " + keyF.getFullName() + " not cached. Call Provider.");
				try {
					tmp = getClient(provider).getDataFlowStructure(keyF);
					p.setDSD(fullkeyFamilyKey, tmp);
					delay();
				} catch (SdmxException e) {
					logger.severe("Exception. Class: " + e.getClass().getName() + " .Message: " + e.getMessage());
					logger.log(Level.FINER, "", e);
				}
			}
		}
		else{
			throw new SdmxException("The dataflow: '" + dataflow + "' does not exist in provider: '" + provider + "'");
		}
		return tmp;
	}
	
	public static DSDIdentifier getDSDIdentifier(String provider, String dataflow) throws SdmxException {
		Provider p = getProvider(provider);
		DSDIdentifier result = null;
		result = p.getDSDIdentifier(dataflow);
		if(result == null){
			logger.info("DSD identifier for dataflow " + dataflow + " not cached. Call Provider.");
			try {
				result = getClient(provider).getDSDIdentifier(dataflow, ALL_AGENCIES, LATEST_VERSION);
				p.setDSDIdentifier(dataflow, result);
				delay();
			} catch (SdmxException e) {
				logger.severe("Exception. Class: " + e.getClass().getName() + " .Message: " + e.getMessage());
				logger.log(Level.FINER, "", e);
			}
		}
		return result;
	}

	public static List<Dimension> getDimensions(String provider, String dataflow) throws SdmxException {
		List<Dimension> result = null;
		DataFlowStructure dsd = getDataFlowStructure(provider, dataflow);
		if(dsd != null){
			result = dsd.getDimensions();
		}
		else{
			throw new SdmxException("The dataflow: '" + dataflow + "' does not exist in provider: '" + provider + "'");
		}
		return result;
	}
	
	public static Map<String,String> getCodes(String provider, String dataflow, String dimension) throws SdmxException {
		Map<String,String> codes = null;
		DataFlowStructure dsd = getDataFlowStructure(provider, dataflow);
		Dimension dim = dsd.getDimension(dimension);
		if(dim != null){
			codes = dim.getCodes();
			if(codes == null){ // this is a 2.1 provider
				try {
					logger.info("Codelist for " + provider + ", " + dataflow + ", " + dimension +" not cached.");
					String codelist = dsd.getDimension(dimension).getCodeList();
					codes = getClient(provider).getCodes(provider, codelist);
					dsd.setCodes(dimension, codes);			
				} catch (SdmxException e) {
					logger.severe("Exception. Class: " + e.getClass().getName() + " .Message: " + e.getMessage());
					logger.log(Level.FINER, "", e);
				}
			}
		}
		else{
			throw new SdmxException("The dimension: '" + dimension + "' does not exist in dataflow: '" + dataflow + "'");
		}
		return codes;
	}

	public static Map<String,String> getFlows(String provider, String pattern) throws SdmxException {
		Map<String,String> result = null;
		Provider p = getProvider(provider);
		result = p.getFlows();
		if(result == null || result.size() == 0){
			logger.info("Flows for " + provider + " not cached. Call Provider.");
			try {
				result = getClient(provider).getDataflows();
				p.setFlows(result);
			} catch (SdmxException e) {
				logger.severe("Exception. Class: " + e.getClass().getName() + " .Message: " + e.getMessage());
				logger.log(Level.FINER, "", e);
			}
		}
		return filterFlows(result, pattern);
	}

	public static List<PortableTimeSeries> getTimeSeries(String provider, String tsKey,
			String startTime, String endTime) throws SdmxException {
		List<PortableTimeSeries> ts = new ArrayList<PortableTimeSeries> ();
		String[] ids = tsKey.trim().split("\\s*,\\s*");
		for (int i = 0; i < ids.length; i++) {
			List<PortableTimeSeries> tmp = getSingleTimeSeries(provider, ids[i], startTime, endTime);
			if(tmp != null){
				ts.addAll(tmp);
			}
		}
		return(ts);
	}

	public static List<PortableTimeSeries> getSingleTimeSeries(String provider, String tsKey,
			String startTime, String endTime) throws SdmxException {
		List<PortableTimeSeries> result = null;
		String dataflow = null;
		String resource = null;
		tsKey = tsKey.trim();
		String delims = "[ /]";
		String[] tokens = tsKey.split(delims, 2);
		if(tokens.length == 2){
			dataflow = tokens[0];
			resource = tokens[1];
		}
		else {
			// legacy mode: flow.tskey
			tokens = translateLegacyTSQuery(tsKey);
			dataflow = tokens[0];
			resource = tokens[1];
		}
		

		DataFlowStructure dsd = getDataFlowStructure(provider, dataflow);
		try {
			result = getClient(provider).getTimeSeries(dataflow, dsd, resource, startTime, endTime);
		} catch (SdmxException e) {
			logger.severe("Exception. Class: " + e.getClass().getName() + " .Message: " + e.getMessage());
			logger.log(Level.FINER, "", e);
		}

		return result;
	}
	
	public static String dumpTimeSeries(String provider, String id,
			String startTime, String endTime) throws SdmxException {
		StringBuffer result = new StringBuffer("");
		List<PortableTimeSeries> ts = getTimeSeries(provider, id, startTime, endTime);
		
		// create header row 
		// and get maximum ts size
		
		int maxSize = 0;
		for (Iterator<PortableTimeSeries> iterator = ts.iterator(); iterator.hasNext();) {
			PortableTimeSeries series = (PortableTimeSeries) iterator.next();
			result.append(",").append(series.getName());
			int size = series.getObservations().size();
			if(size > maxSize) maxSize = size;
			if(iterator.hasNext()){
				result.append(",");
			}
			if(Configuration.isReverse()){
				//reverse the time series for user friendliness
				series.reverse();
			}
		}
		result.append("\n");
		for (int i = 0; i < maxSize; i++) {
			for (int j = 0; j < ts.size(); j++) {
				if(i < ts.get(j).getObservations().size()){
					result.append(ts.get(j).getTimeSlots().get(i)).append(",");
					result.append(ts.get(j).getObservations().get(i));
					
				}
				else{
					result.append(",");
				}
				if(j+1<ts.size()){
					result.append(",");
				}
			}
			result.append("\n");
		}
		return result.toString();
	}
	
	private static Provider getProvider(String provider) throws SdmxException{
		Provider p = SDMXClientFactory.getProviders().get(provider);
		if(p == null){
			throw new SdmxException("The provider " + provider + " does not exist.");
		}
		return p;
	}

	private static GenericSDMXClient getClient(String provider, String user, String password) throws SdmxException{
		final String sourceMethod = "getClient";
		logger.entering(sourceClass, sourceMethod);
		GenericSDMXClient client = clients.get(provider);
		if(client == null){
			logger.info("Client for " + provider + " does not exist. Create it.");
			client = (GenericSDMXClient)SDMXClientFactory.createClient(provider);
			if(client.needsCredentials()){
				handlePassword(client, user, password);
			}
			clients.put(provider, client);
		}
		logger.exiting(sourceClass, sourceMethod);
		return client;
		
	}

	private static GenericSDMXClient getClient(String provider) throws SdmxException{
		return getClient(provider, null, null);
		
	}
	
	private static void handlePassword(GenericSDMXClient client, String user, String pw){
		if(client.needsCredentials()){
			if(user == null || pw == null){
				final JFrame frame = new JFrame("Authentication");
				frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				LoginDialog loginDlg = new LoginDialog(frame, "Authentication");
                loginDlg.setVisible(true);
                client.setCredentials(loginDlg.getUsername(), loginDlg.getPassword());
                frame.dispose();
			}
			else{
				client.setCredentials(user, pw);
			}
		}
	}
	
	private static Map<String,String> filterFlows(Map<String,String> flows, String pattern){
		final String sourceMethod = "filterFlows";
		logger.entering(sourceClass, sourceMethod);
		Map<String,String> result = flows;
		if(pattern != null && !pattern.trim().isEmpty()){
			result = new Hashtable<String, String>();
			pattern = pattern.replaceAll ("\\*", ".*").replaceAll ("\\?", ".");
			for (Iterator<String> iterator = flows.keySet().iterator(); iterator.hasNext();) {
				String key = (String) iterator.next();
				String value = flows.get(key);
				if(key.matches(pattern) || value.matches(pattern)){
					result.put(key, value);
				}
			}
		}
		logger.exiting(sourceClass, sourceMethod);
		return result;
	}
	
	private static String[] translateLegacyTSQuery(String tsKey){
		String[] newKey = new String[2];
		String delims = "[.]";
		String[] tokens = tsKey.split(delims, 2);
		if(tokens.length == 2){
			newKey[0] = tokens[0];
			String resource = tokens[1];
			resource = resource.replace("*", "");
			resource = resource.replace('|', '+');
			newKey[1] = resource;
		}
		else{
			logger.severe("Error in query string format: '" + tsKey + "'. Could not get dataflow id.");
		}
		return newKey;
	}
	
	private static void delay(){
		long sleepTime = Configuration.getDelay();
		if(sleepTime>0){
			try {
				Thread.sleep(Configuration.getDelay());
			} catch (InterruptedException e) {
				logger.warning("Error with delay");
			}
		}
	}


}
