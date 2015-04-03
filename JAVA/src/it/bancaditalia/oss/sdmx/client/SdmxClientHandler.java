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
package it.bancaditalia.oss.sdmx.client;

import it.bancaditalia.oss.sdmx.api.Codelist;
import it.bancaditalia.oss.sdmx.api.DSDIdentifier;
import it.bancaditalia.oss.sdmx.api.DataFlowStructure;
import it.bancaditalia.oss.sdmx.api.Dataflow;
import it.bancaditalia.oss.sdmx.api.Dimension;
import it.bancaditalia.oss.sdmx.api.GenericSDMXClient;
import it.bancaditalia.oss.sdmx.api.PortableTimeSeries;
import it.bancaditalia.oss.sdmx.util.Configuration;
import it.bancaditalia.oss.sdmx.util.LoginDialog;
import it.bancaditalia.oss.sdmx.util.SdmxException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
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

	public static void setPreferredLanguage(String lang) throws SdmxException {
		Configuration.setLang(lang);
	}

	public static void addProvider(String name, String endpoint, boolean needsCredentials, boolean needsURLEncoding, boolean supportsCompression, String description) throws SdmxException{
		if(name == null || name.trim().isEmpty()){
			logger.severe("The name of the provider cannot be null: " + name);
			throw new SdmxException("The name of the provider cannot be null: '" + name + "'");
		}
		if(endpoint == null || endpoint.trim().isEmpty()){
			logger.severe("The enpoint of the provider cannot be null: " + endpoint);
			throw new SdmxException("The enpoint of the provider cannot be null: '" + endpoint + "'");
		}
		URL ep;
		try {
			ep = new URL(endpoint);
			SDMXClientFactory.addProvider(name, ep, needsCredentials, needsURLEncoding, supportsCompression, description);
		} catch (MalformedURLException e) {
			logger.severe("Exception. Class: " + e.getClass().getName() + " .Message: " + e.getMessage());
			logger.log(Level.FINER, "", e);
			throw new SdmxException("The URL provided is not valid: '" + endpoint + "'");
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
		if(provider == null || provider.trim().isEmpty()){
			throw new SdmxException("The name of the provider cannot be null: " + provider);
		}
		if(dataflow == null || dataflow.trim().isEmpty()){
			throw new SdmxException("The name of the dataflow cannot be null: " + dataflow);
		}
		DataFlowStructure tmp = null;
		DSDIdentifier keyF = getDSDIdentifier(provider, dataflow);
		if(keyF != null){
			String fullkeyFamilyKey = keyF.getFullIdentifier();
			Provider p = getProvider(provider);
			tmp = p.getDSD(fullkeyFamilyKey);
			if(tmp == null){
				logger.finer("DSD for " + keyF.getFullIdentifier() + " not cached. Calling Provider.");
				tmp = getClient(provider).getDataFlowStructure(keyF, false);
				if(tmp != null){
					p.setDSD(fullkeyFamilyKey, tmp);
				}
				else{
					throw new SdmxException("Could not get structure for '" + dataflow + "' in provider: '" + provider + "'");
				}
			}
		}
		else{
			throw new SdmxException("Could not get dataflow '" + dataflow + "' in provider: '" + provider + "'");
		}
		return tmp;
	}
	
	public static DSDIdentifier getDSDIdentifier(String provider, String dataflow) throws SdmxException {
		if(provider == null || provider.trim().isEmpty()){
			throw new SdmxException("The name of the provider cannot be null: " + provider);
		}
		if(dataflow == null || dataflow.trim().isEmpty()){
			throw new SdmxException("The name of the dataflow cannot be null: " + dataflow);
		}
		Provider p = getProvider(provider);
		DSDIdentifier result = null;
		result = p.getDSDIdentifier(dataflow);
		if(result == null){
			logger.finer("DSD identifier for dataflow " + dataflow + " not cached. Calling Provider.");
			Dataflow df = getClient(provider).getDataflow(dataflow, ALL_AGENCIES, LATEST_VERSION);
			if(df != null){
				p.setFlow(df);
				result = df.getDsdIdentifier();
			}
			else{
				throw new SdmxException("Could not get dataflow '" + dataflow + "' in provider: '" + provider + "'");
			}
		}
		return result;
	}
	
	

	public static List<Dimension> getDimensions(String provider, String dataflow) throws SdmxException {
		if(provider == null || provider.trim().isEmpty()){
			throw new SdmxException("The name of the provider cannot be null: " + provider);
		}
		if(dataflow == null || dataflow.trim().isEmpty()){
			throw new SdmxException("The name of the dataflow cannot be null: " + dataflow);
		}
		List<Dimension> result = null;
		DataFlowStructure dsd = getDataFlowStructure(provider, dataflow);
		if(dsd != null){
			result = dsd.getDimensions();
		}
		else{
			throw new SdmxException("Could not get structure for '" + dataflow + "' in provider: '" + provider + "'");
		}
		return result;
	}
	
	public static Map<String,String> getCodes(String provider, String dataflow, String dimension) throws SdmxException {
		if(provider == null || provider.trim().isEmpty()){
			throw new SdmxException("The name of the provider cannot be null: " + provider);
		}
		if(dataflow == null || dataflow.trim().isEmpty()){
			throw new SdmxException("The name of the dataflow cannot be null: " + dataflow);
		}
		if(dimension == null || dimension.trim().isEmpty()){
			throw new SdmxException("The name of the dimension cannot be null: " + dimension);
		}
		Map<String,String> codes = null;
		DataFlowStructure dsd = getDataFlowStructure(provider, dataflow);
		Dimension dim = dsd.getDimension(dimension);
		if(dim != null){
			codes = dim.getCodeList().getCodes();
			if(codes == null){ // this is a 2.1 provider
				logger.finer("Codelist for " + provider + ", " + dataflow + ", " + dimension +" not cached.");
				Codelist codelist = dsd.getDimension(dimension).getCodeList();
				codes = getClient(provider).getCodes(codelist.getId(), codelist.getAgency(), codelist.getVersion());
				if(codes != null){
					codelist.setCodes(codes);
				}
				else{
					throw new SdmxException("Could not get codes for '" + dataflow + "' in provider: '" + provider + "'");
				}
			}
		}
		else{
			throw new SdmxException("The dimension: '" + dimension + "' does not exist in dataflow: '" + dataflow + "'");
		}
		return codes;
	}

	public static Dataflow getFlow(String provider, String dataflow) throws SdmxException {
		if(provider == null || provider.trim().isEmpty()){
			throw new SdmxException("The name of the provider cannot be null: " + provider);
		}
		if(dataflow == null || dataflow.trim().isEmpty()){
			throw new SdmxException("The name of the dataflow cannot be null: " + dataflow);
		}
		Provider p = getProvider(provider);
		Dataflow flow = p.getFlows().get(dataflow);
		if(flow == null){
			logger.fine("Dataflow " + dataflow + " not cached. Calling Provider.");
			flow = getClient(provider).getDataflow(dataflow, ALL_AGENCIES, LATEST_VERSION);
			if(flow != null){
				p.setFlow(flow);
			}
			else{
				throw new SdmxException("Could not get dataflow '" + dataflow + "' in provider: '" + provider + "'");
			}
		}
		return flow;
	}
	
	public static Map<String,String> getFlows(String provider, String pattern) throws SdmxException {
		if(provider == null || provider.trim().isEmpty()){
			throw new SdmxException("The name of the provider cannot be null: " + provider);
		}
		Map<String,Dataflow> flows = null;
		Provider p = getProvider(provider);
		flows = p.getFlows();
		if(flows == null || flows.size() == 0 || !p.isFull()){
			logger.fine("Flows for " + provider + " not cached. Calling Provider.");
			flows = getClient(provider).getDataflows();
			if(flows != null && flows.size() != 0){
				p.setFlows(flows);
				p.setFull(true);
			}
			else{
				throw new SdmxException("Could not get dataflows from provider: '" + provider + "'");
			}
		}
		return filterFlows(flows, pattern);
	}

	public static List<PortableTimeSeries> getTimeSeries(String provider, String tsKey,
			String startTime, String endTime) throws SdmxException {
		return getTimeSeries(provider, tsKey, startTime, endTime, false);
	}
	
	public static List<PortableTimeSeries> getTimeSeries(String provider, String tsKey,
			String startTime, String endTime, boolean serieskeysonly) throws SdmxException {
		if(provider == null || provider.trim().isEmpty()){
			throw new SdmxException("The name of the provider cannot be null: " + provider);
		}
		if(tsKey == null || tsKey.trim().isEmpty()){
			throw new SdmxException("The tsKey cannot be null: " + tsKey);
		}
		List<PortableTimeSeries> ts = new ArrayList<PortableTimeSeries> ();
		String[] ids = tsKey.trim().split("\\s*;\\s*");
		for (int i = 0; i < ids.length; i++) {
			List<PortableTimeSeries> tmp = getSingleTimeSeries(provider, ids[i], startTime, endTime, serieskeysonly);
			if(tmp != null){
				ts.addAll(tmp);
			}
		}
		return(ts);
	}

	public static String getDataURL(String provider, String tsKey, String start, String end, boolean seriesKeysOnly) throws SdmxException {
		if(provider == null || provider.trim().isEmpty()){
			throw new SdmxException("The name of the provider cannot be null: " + provider);
		}
		if(tsKey == null || tsKey.trim().isEmpty()){
			throw new SdmxException("The tsKey cannot be null: " + tsKey);
		}
		
		String[] tokens = extractFlowAndResource(tsKey);
		String dataflow = tokens[0];
		String resource = tokens[1];
		Dataflow df = getFlow(provider, dataflow);
		
		String result = getClient(provider).buildDataURL(df, resource, start, end, seriesKeysOnly);
		return(result);
	}

	private static List<PortableTimeSeries> getSingleTimeSeries(String provider, String tsKey,
			String startTime, String endTime, boolean serieskeysonly) throws SdmxException {
		if(provider == null || provider.trim().isEmpty()){
			throw new SdmxException("The name of the provider cannot be null: " + provider);
		}
		if(tsKey == null || tsKey.trim().isEmpty()){
			throw new SdmxException("The tsKey cannot be null: " + tsKey);
		}		List<PortableTimeSeries> result = null;
		
		String[] tokens = extractFlowAndResource(tsKey);
		String dataflow = tokens[0];
		String resource = tokens[1];

		Dataflow df = getFlow(provider, dataflow);
		DataFlowStructure dsd = getDataFlowStructure(provider, dataflow);
		result = getClient(provider).getTimeSeries(df, dsd, resource, startTime, endTime, serieskeysonly);
		if(result == null || result.size() == 0){
			throw new SdmxException("The query: " + tsKey + " did not match any time series on the provider.");
		}
		return result;
	}
	
	public static String dumpTimeSeries(String provider, String id,
			String startTime, String endTime) throws SdmxException {
		if(provider == null || provider.trim().isEmpty()){
			throw new SdmxException("The name of the provider cannot be null: " + provider);
		}
		if(id == null || id.trim().isEmpty()){
			throw new SdmxException("The id cannot be null: " + id);
		}
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
		if(provider == null || provider.trim().isEmpty()){
			throw new SdmxException("The name of the provider cannot be null: " + provider);
		}
		Provider p = SDMXClientFactory.getProviders().get(provider);
		if(p == null){
			throw new SdmxException("The provider " + provider + " does not exist.");
		}
		return p;
	}

	private static GenericSDMXClient getClient(String provider, String user, String password) throws SdmxException{
		final String sourceMethod = "getClient";
		logger.entering(sourceClass, sourceMethod);
		if(provider == null || provider.trim().isEmpty()){
			throw new SdmxException("The name of the provider cannot be null: " + provider);
		}
		GenericSDMXClient client = clients.get(provider);
		if(client == null){
			logger.finer("Client for " + provider + " does not exist. I will create it.");
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
		if(provider == null || provider.trim().isEmpty()){
			throw new SdmxException("The name of the provider cannot be null: " + provider);
		}
		return getClient(provider, null, null);
		
	}
	
	private static void handlePassword(GenericSDMXClient client, String user, String pw) throws SdmxException{
		if(client == null){
			throw new SdmxException("The client cannot be null: " + client);
		}
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
	
	private static Map<String,String> filterFlows(Map<String,Dataflow> flows, String pattern){
		final String sourceMethod = "filterFlows";
		logger.entering(sourceClass, sourceMethod);
		Map<String,String> result = new HashMap<String,String>();
		if(flows != null && flows.size() > 0){
			if(pattern != null && !pattern.trim().isEmpty()){
				pattern = pattern.replaceAll ("\\*", ".*").replaceAll ("\\?", ".");
			}
			for (Iterator<String> iterator = flows.keySet().iterator(); iterator.hasNext();) {
				String key = (String) iterator.next();
				String value = flows.get(key).getDescription();
				if(pattern != null && !pattern.trim().isEmpty()){
					if(key.matches(pattern) || value.matches(pattern)){
						result.put(key, value);
					}
				}
				else{
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
	
	private static String[] extractFlowAndResource(String tsKey) throws SdmxException{
		tsKey = tsKey.trim();
		String delims = "[ /]";
		String[] tokens = tsKey.split(delims, 2);
		if(tokens.length != 2){
			// legacy mode: flow.tskey
			tokens = translateLegacyTSQuery(tsKey);
			if(tokens.length != 2){
				throw new SdmxException("Malformed time series key: " + tsKey);
			}
		}
		return tokens;
	}
	
}
