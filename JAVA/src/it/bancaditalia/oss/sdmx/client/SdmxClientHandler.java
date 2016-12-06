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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.swing.JFrame;

import it.bancaditalia.oss.sdmx.api.Codelist;
import it.bancaditalia.oss.sdmx.api.DSDIdentifier;
import it.bancaditalia.oss.sdmx.api.DataFlowStructure;
import it.bancaditalia.oss.sdmx.api.Dataflow;
import it.bancaditalia.oss.sdmx.api.Dimension;
import it.bancaditalia.oss.sdmx.api.GenericSDMXClient;
import it.bancaditalia.oss.sdmx.api.PortableDataSet;
import it.bancaditalia.oss.sdmx.api.PortableTimeSeries;
import it.bancaditalia.oss.sdmx.exceptions.DataStructureException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxInvalidParameterException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxUnknownProviderException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxXmlContentException;
import it.bancaditalia.oss.sdmx.util.Configuration;
import it.bancaditalia.oss.sdmx.util.LoginDialog;
//import it.bancaditalia.oss.sdmx.util.SdmxException;
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

	/**
	 * Adds a provider to current configuration 
	 * 
	 * @param provider a non-null, non-empty provider identification short name.
	 * @param endpoint a non-null provider-defined endpoint url for queries
	 * @param needsCredentials true if the provider needs authentication
	 * @param needsURLEncoding true if the url must be encoded
	 * @param supportsCompression true if the provider supports message compression
	 * @param description an optional natural language description of the provider
	 * 
	 * @throws SdmxException
	 */
	public static void addProvider(String provider, String endpoint, boolean needsCredentials, 
			boolean needsURLEncoding, boolean supportsCompression, String description) throws SdmxException {
		
		if(provider == null || provider.trim().isEmpty()){
			logger.severe("The name of the provider cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
		}
		if(endpoint == null){
			logger.severe("The enpoint of the provider cannot be null");
			throw new SdmxInvalidParameterException("The endpoint of the provider cannot be null");
		}
		try {
			SDMXClientFactory.addProvider(provider, new URL(endpoint), needsCredentials, needsURLEncoding, 
					supportsCompression, description, false);
		} catch (MalformedURLException e) {
			throw new SdmxInvalidParameterException(e.getMessage());
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
			logger.severe("The name of the provider cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
		}
		if(dataflow == null || dataflow.trim().isEmpty()){
			logger.severe("The name of the dataflow cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
		}
		DataFlowStructure result = null;
		DSDIdentifier keyF = getDSDIdentifier(provider, dataflow);
		String fullkeyFamilyKey = keyF.getFullIdentifier();
		Provider p = getProvider(provider);
		result = p.getDSD(fullkeyFamilyKey);
		if(result == null)
		{
			logger.finer("DSD for " + keyF.getFullIdentifier() + " not cached. Calling Provider.");
			result = getClient(provider).getDataFlowStructure(keyF, false);
			if(result != null)
				p.setDSD(fullkeyFamilyKey, result);
			else
				throw new SdmxXmlContentException("Could not find dataflow structure for '" + dataflow + "' in provider: '" + provider + "'");
		}

		return result;
	}
	
	public static DSDIdentifier getDSDIdentifier(String providerName, String dataflow) throws SdmxException {
		if(providerName == null || providerName.trim().isEmpty()){
			logger.severe("The name of the provider cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
		}
		if(dataflow == null || dataflow.trim().isEmpty()){
			logger.severe("The name of the dataflow cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
		}
		Provider provider = getProvider(providerName);
		DSDIdentifier result = null;
		result = provider.getDSDIdentifier(dataflow);
		if(result == null)
		{
			logger.finer("DSD identifier for dataflow " + dataflow + " not cached. Calling Provider.");
			Dataflow df = getClient(providerName).getDataflow(dataflow, ALL_AGENCIES, LATEST_VERSION);
			if(df != null){
				provider.setFlow(df);
				result = df.getDsdIdentifier();
				if (result == null)
					throw new SdmxXmlContentException("Could not get DSD identifier for dataflow '" + dataflow + "' in provider: '" + provider + "'");
			}
			else
				throw new SdmxXmlContentException("Could not get dataflow '" + dataflow + "' in provider: '" + providerName + "'");
		}
		return result;
	}
	
	

	public static List<Dimension> getDimensions(String provider, String dataflow) throws SdmxException {
		if(provider == null || provider.trim().isEmpty()){
			logger.severe("The name of the provider cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
		}
		if(dataflow == null || dataflow.trim().isEmpty()){
			logger.severe("The name of the dataflow cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
		}
		return getDataFlowStructure(provider, dataflow).getDimensions();
	}
	
	public static Map<String,String> getCodes(String provider, String dataflow, String dimension) throws SdmxException {
		if(provider == null || provider.trim().isEmpty()){
			logger.severe("The name of the provider cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
		}
		if(dataflow == null || dataflow.trim().isEmpty()){
			logger.severe("The name of the dataflow cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
		}
		if(dimension == null || dimension.trim().isEmpty()){
			logger.severe("The name of the dimension cannot be null");
			throw new SdmxInvalidParameterException("The name of the dimension cannot be null");
		}
		Map<String,String> codes = null;
		DataFlowStructure dsd = getDataFlowStructure(provider, dataflow);
		Dimension dim = dsd.getDimension(dimension);
		if(dim != null)
		{
			codes = dim.getCodeList().getCodes();
			if(codes == null)
			{ 
				// this is a 2.1 provider
				logger.finer("Codelist for " + provider + ", " + dataflow + ", " + dimension + " not cached.");
				Codelist codelist = dsd.getDimension(dimension).getCodeList();
				codes = getClient(provider).getCodes(codelist.getId(), codelist.getAgency(), codelist.getVersion());
				if(codes != null)
					codelist.setCodes(codes);
				else
					throw new SdmxXmlContentException("Could not get codes for '" + dataflow + "' in provider: '" + provider + "'");
			}
		} 
		else
			throw new SdmxXmlContentException("The dimension: '" + dimension + "' does not exist in dataflow: '" + dataflow + "'");
		
		return codes;
	}

	public static Dataflow getFlow(String provider, String dataflow) throws SdmxException {
		if(provider == null || provider.trim().isEmpty()){
			logger.severe("The name of the provider cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
		}
		if(dataflow == null || dataflow.trim().isEmpty()){
			logger.severe("The name of the dataflow cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
		}
		Provider p = getProvider(provider);
		Dataflow flow = p.getFlows().get(dataflow);
		if(flow == null)
		{
			logger.fine("Dataflow " + dataflow + " not cached. Calling Provider.");
			flow = getClient(provider).getDataflow(dataflow, ALL_AGENCIES, LATEST_VERSION);
			if(flow != null)
				p.setFlow(flow);
			else
				throw new SdmxXmlContentException("Could not get dataflow '" + dataflow + "' in provider: '" + provider + "'");
		}
		return flow;
	}
	
	public static Map<String,String> getFlows(String provider, String pattern) throws SdmxException {
		if(provider == null || provider.trim().isEmpty()){
			logger.severe("The name of the provider cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
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
			else
				throw new SdmxXmlContentException("Could not get dataflows from provider: '" + provider + "'");
			
		}
		return filterFlows(flows, pattern);
	}

	public static PortableDataSet getTimeSeriesTable(String provider, String tsKey,
			String startTime, String endTime) throws SdmxException, DataStructureException {
		return new PortableDataSet(getTimeSeries(provider, tsKey, startTime, endTime, false, null, false));
	}
	
	public static List<PortableTimeSeries> getTimeSeries(String provider, String tsKey,
			String startTime, String endTime) throws SdmxException {
		return getTimeSeries(provider, tsKey, startTime, endTime, false, null, false);
	}

	public static List<PortableTimeSeries> getTimeSeriesRevisions(String provider, String tsKey,
			String startTime, String endTime, 
			String updatedAfter, boolean includeHistory) throws SdmxException {
		return getTimeSeries(provider, tsKey, startTime, endTime, false, 
				updatedAfter, includeHistory);
	}

	public static List<PortableTimeSeries> getTimeSeriesNames(String provider, String tsKey) throws SdmxException {
		return getTimeSeries(provider, tsKey, null, null, true, null, false);
	}

	private static List<PortableTimeSeries> getTimeSeries(String provider, String tsKey, String startTime, String endTime, 
			boolean serieskeysonly, String updatedAfter, boolean includeHistory) throws SdmxException {
		if(provider == null || provider.trim().isEmpty()){
			logger.severe("The name of the provider cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
		}
		if(tsKey == null || tsKey.trim().isEmpty()){
			logger.severe("The tsKey cannot be null");
			throw new SdmxInvalidParameterException("The tsKey cannot be null");
		}
		List<PortableTimeSeries> ts = new ArrayList<PortableTimeSeries> ();
		String[] ids = tsKey.trim().split("\\s*;\\s*");
		for (int i = 0; i < ids.length; i++) {
			List<PortableTimeSeries> tmp = getSingleTimeSeries(provider, ids[i], startTime, endTime, 
					serieskeysonly, updatedAfter, includeHistory);
			if(tmp != null){
				ts.addAll(tmp);
			}
		}
		return(ts);
	}

	public static String getDataURL(String provider, String tsKey, String start, String end, 
			boolean seriesKeysOnly, String updatedAfter, boolean includeHistory) throws SdmxException {
		if(provider == null || provider.trim().isEmpty()){
			logger.severe("The name of the provider cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
		}
		if(tsKey == null || tsKey.trim().isEmpty()){
			logger.severe("The tsKey cannot be null");
			throw new SdmxInvalidParameterException("The tsKey cannot be null");
		}
		
		String[] tokens = extractFlowAndResource(tsKey);
		String dataflow = tokens[0];
		String resource = tokens[1];
		Dataflow df = getFlow(provider, dataflow);
		
		String result = getClient(provider).buildDataURL(df, resource, start, end, seriesKeysOnly, updatedAfter, includeHistory);
		return(result);
	}

	private static List<PortableTimeSeries> getSingleTimeSeries(String provider, String tsKey, String startTime, 
			String endTime, boolean serieskeysonly, String updatedAfter, boolean includeHistory) throws SdmxException {
		if(provider == null || provider.trim().isEmpty()){
			logger.severe("The name of the provider cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
		}
		if(tsKey == null || tsKey.trim().isEmpty()){
			logger.severe("The tsKey cannot be null");
			throw new SdmxInvalidParameterException("The tsKey cannot be null");
		}
		
		List<PortableTimeSeries> result = null;
		
		String[] tokens = extractFlowAndResource(tsKey);
		String dataflow = tokens[0];
		String resource = tokens[1];

		Dataflow df = getFlow(provider, dataflow);
		DataFlowStructure dsd = getDataFlowStructure(provider, dataflow);
		result = getClient(provider).getTimeSeries(df, dsd, resource, startTime, endTime, 
				serieskeysonly, updatedAfter, includeHistory);
		if(result == null || result.size() == 0)
			throw new SdmxXmlContentException("The query: " + tsKey + " did not match any time series on the provider.");
		return result;
	}

	public static String dumpTimeSeriesList(List<PortableTimeSeries> ts) {
		StringBuffer result = new StringBuffer("");
		int maxSize = 0;
		for (Iterator<PortableTimeSeries> iterator = ts.iterator(); iterator.hasNext();) {
			PortableTimeSeries series = (PortableTimeSeries) iterator.next();
			result.append(";").append(series.getName());
			int size = series.getObservations().size();
			if(size > maxSize) maxSize = size;
			if(iterator.hasNext()){
				result.append(";");
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
					result.append(ts.get(j).getTimeSlots().get(i)).append(";");
					result.append(ts.get(j).getObservations().get(i));
				}
				else{
					result.append(";");
				}
				if(j+1<ts.size()){
					result.append(";");
				}
			}
			result.append("\n");
		}
		return result.toString();
	}

	public static String dumpTimeSeries(String provider, String id,
			String startTime, String endTime) throws SdmxException, DataStructureException {
		if(provider == null || provider.trim().isEmpty()){
			logger.severe("The name of the provider cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
		}
		if(id == null || id.trim().isEmpty()){
			logger.severe("The id cannot be null");
			throw new SdmxInvalidParameterException("The id cannot be null");
		}
		if(!SDMXClientFactory.getProviders().containsKey(provider)){
			throw new SdmxInvalidParameterException("The provider : " + id + " does not exist.");
		}
		String result = "";
		if(!Configuration.isTable()){
			// Do it as a list of time series
			List<PortableTimeSeries> ts = getTimeSeries(provider, id, startTime, endTime);
			result = dumpTimeSeriesList(ts);
		}
		else{
			// do it as a table
			result = getTimeSeriesTable(provider, id, startTime, endTime).toString();
		}
		return result;
	}
	
	private static Provider getProvider(String providerName) throws SdmxException{
		if(providerName == null || providerName.trim().isEmpty()){
			logger.severe("The name of the provider cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
		}
		Provider provider = SDMXClientFactory.getProviders().get(providerName);
		
		// TODO: move this check to SDMXClientFactory
		if(provider == null)
			throw new SdmxUnknownProviderException(providerName);

		return provider;
	}

	private static GenericSDMXClient getClient(String provider, String user, String password) throws SdmxException{
		final String sourceMethod = "getClient";
		logger.entering(sourceClass, sourceMethod);
		if(provider == null || provider.trim().isEmpty()){
			logger.severe("The name of the provider cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
		}
		GenericSDMXClient client = clients.get(provider);
		if(client == null){
			logger.finer("Client for " + provider + " does not exist. I will create it.");
			client = (GenericSDMXClient)SDMXClientFactory.createClient(provider);
			if(client.needsCredentials())
				handlePassword(client, user, password);

			clients.put(provider, client);
		}
		logger.exiting(sourceClass, sourceMethod);
		return client;
		
	}

	private static GenericSDMXClient getClient(String provider) throws SdmxException {
		if(provider == null || provider.trim().isEmpty()){
			logger.severe("The name of the provider cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
		}
		
		return getClient(provider, null, null);
		
	}
	
	private static void handlePassword(GenericSDMXClient client, String user, String pw) throws SdmxException{
		if(client == null){
			logger.severe("The client cannot be null");
			throw new SdmxInvalidParameterException("The client cannot be null");
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
			if(tokens.length != 2)
				throw new SdmxXmlContentException("Malformed time series key: " + tsKey);
		}
		return tokens;
	}
	
}
