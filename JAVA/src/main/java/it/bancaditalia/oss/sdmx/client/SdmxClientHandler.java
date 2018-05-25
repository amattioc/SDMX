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

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
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
import it.bancaditalia.oss.sdmx.api.SdmxAttribute;
import it.bancaditalia.oss.sdmx.client.custom.RestSdmx20Client;
import it.bancaditalia.oss.sdmx.exceptions.DataStructureException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxInvalidParameterException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxUnknownProviderException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxXmlContentException;
import it.bancaditalia.oss.sdmx.util.Configuration;
import it.bancaditalia.oss.sdmx.util.LoginDialog;

//import it.bancaditalia.oss.sdmx.util.SdmxException;
/**
 * <p>
 * Java class for optimizing interactions with the SdmxClients in non Java environment. It provides a sort fo 'session',
 * storing the clients that are created and reusing them. It also provides caching of all key families retrieved.
 * 
 * @author Attilio Mattiocco
 *
 */
public class SdmxClientHandler
{

	protected static Logger							logger			= Configuration.getSdmxLogger();
	private static final String						sourceClass		= SdmxClientHandler.class.getSimpleName();

	// we only manage 'latest' and 'all' as starting points, for now
	public static final String						LATEST_VERSION	= "latest";
	public static final String						ALL_AGENCIES	= "all";

	// key: provider name --> client
	private static Map<String, GenericSDMXClient>	clients			= new HashMap<>();

	public static boolean needsCredentials(String provider) throws SdmxException
	{
		return getClient(provider).needsCredentials();
	}

	public static void setCredentials(String provider, String user, String pw) throws SdmxException
	{
		getClient(provider, user, pw);
	}

	public static void setPreferredLanguage(String lang) throws SdmxException
	{
		Configuration.setLang(lang);
	}

	/**
	 * Adds a local provider that reads SDMX files in a user specified directory
	 * 
	 * @param provider a non-null, non-empty provider identification short name.
	 * @param endpoint a non-null existing directory where the SDMX files are stored
	 * @param description an optional natural language description of the provider
	 * 
	 * @throws SdmxException
	 */
	public static void addLocalProvider(String provider, String endpoint, String description) throws SdmxException
	{

		if (provider == null || provider.trim().isEmpty())
		{
			logger.severe("The name of the provider cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
		}
		if (endpoint == null)
		{
			logger.severe("The enpoint of the provider cannot be null");
			throw new SdmxInvalidParameterException("The endpoint of the provider cannot be null");
		}
		// get the URL from this file
		File epFile = new File(endpoint);
		if (!epFile.isDirectory())
		{
			logger.severe("The enpoint of the provider must be an existing local directory");
			throw new SdmxInvalidParameterException(
					"The enpoint of the provider has to be an existing local directory");
		}

		SDMXClientFactory.addProvider(provider, epFile.toURI(), null, false, false, false, description, false);
	}

	/**
	 * Adds a local provider that reads files on disk
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
	public static void addProvider(String provider, String endpoint, boolean needsCredentials, boolean needsURLEncoding,
			boolean supportsCompression, String description) throws SdmxException
	{

		if (provider == null || provider.trim().isEmpty())
		{
			logger.severe("The name of the provider cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
		}
		if (endpoint == null)
		{
			logger.severe("The enpoint of the provider cannot be null");
			throw new SdmxInvalidParameterException("The endpoint of the provider cannot be null");
		}
		try
		{
			SDMXClientFactory.addProvider(provider, new URI(endpoint), null, needsCredentials, needsURLEncoding,
					supportsCompression, description, false);
		}
		catch (URISyntaxException e)
		{
			throw new SdmxInvalidParameterException(e.getMessage());
		}
	}

	/**
	 * Get the list of all available SDMX Providers
	 * 
	 * @return A map where the keys are the provider names and the values the needsCredentials values
	 */
	public static SortedMap<String, Boolean> getProviders()
	{
		TreeMap<String, Boolean> result = new TreeMap<>();
		for (Entry<String, Provider> entry : SDMXClientFactory.getProviders().entrySet())
			result.put(entry.getKey(), entry.getValue().isNeedsCredentials());
		return result;
	}

	public static DataFlowStructure getDataFlowStructure(String provider, String dataflow) throws SdmxException
	{
		if (provider == null || provider.trim().isEmpty())
		{
			logger.severe("The name of the provider cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
		}
		if (dataflow == null || dataflow.trim().isEmpty())
		{
			logger.severe("The name of the dataflow cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
		}
		DataFlowStructure result = null;
		DSDIdentifier keyF = getDSDIdentifier(provider, dataflow);
		String fullkeyFamilyKey = keyF.getFullIdentifier();
		Provider p = getProvider(provider);
		result = p.getDSD(fullkeyFamilyKey);
		if (result == null)
		{
			logger.finer("DSD for " + keyF.getFullIdentifier() + " not cached. Calling Provider.");
			result = getClient(provider).getDataFlowStructure(keyF,
					/*
					 * TODO: Find a way to obtain dimension description without a full query. Original code:
					 * !Configuration.getCodesPolicy().equalsIgnoreCase(Configuration.SDMX_CODES_POLICY_ID)
					 */
					true);
			if (result != null)
			{
				if (!(getClient(provider) instanceof RestSdmx20Client))
				{
					// workaround: some providers do not set in the dsd response all the referenced codelists
					for (Dimension x : result.getDimensions())
					{
						if (x.getCodeList() != null
								&& (x.getCodeList() == null || x.getCodeList().isEmpty()))
						{
							Map<String, String> cl = getClient(provider).getCodes(x.getCodeList().getId(),
									x.getCodeList().getAgency(), x.getCodeList().getVersion());
							x.getCodeList().setCodes(cl);
							result.setDimension(x);
						}
					}
					
					for (SdmxAttribute x : result.getAttributes())
					{
						if (x.getCodeList() != null
								&& (x.getCodeList() == null || x.getCodeList().isEmpty()))
						{
							Map<String, String> cl = getClient(provider).getCodes(x.getCodeList().getId(),
									x.getCodeList().getAgency(), x.getCodeList().getVersion());
							x.getCodeList().setCodes(cl);
							result.setAttribute(x);
						}
					}
				}
				p.setDSD(fullkeyFamilyKey, result);
			}
			else
				throw new SdmxXmlContentException(
						"Could not find dataflow structure for '" + dataflow + "' in provider: '" + provider + "'");
		}

		return result;
	}

	public static DSDIdentifier getDSDIdentifier(String providerName, String dataflow) throws SdmxException
	{
		if (providerName == null || providerName.trim().isEmpty())
		{
			logger.severe("The name of the provider cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
		}
		if (dataflow == null || dataflow.trim().isEmpty())
		{
			logger.severe("The name of the dataflow cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
		}
		Provider provider = getProvider(providerName);
		DSDIdentifier result = null;
		result = provider.getDSDIdentifier(dataflow);
		if (result == null)
		{
			logger.finer("DSD identifier for dataflow " + dataflow + " not cached. Calling Provider.");
			Dataflow df = getClient(providerName).getDataflow(dataflow, ALL_AGENCIES, LATEST_VERSION);
			if (df != null)
			{
				provider.setFlow(df);
				result = df.getDsdIdentifier();
				if (result == null)
					throw new SdmxXmlContentException("Could not get DSD identifier for dataflow '" + dataflow
							+ "' in provider: '" + provider + "'");
			}
			else
				throw new SdmxXmlContentException(
						"Could not get dataflow '" + dataflow + "' in provider: '" + providerName + "'");
		}
		return result;
	}

	public static List<Dimension> getDimensions(String provider, String dataflow) throws SdmxException
	{
		if (provider == null || provider.trim().isEmpty())
		{
			logger.severe("The name of the provider cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
		}
		if (dataflow == null || dataflow.trim().isEmpty())
		{
			logger.severe("The name of the dataflow cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
		}
		return getDataFlowStructure(provider, dataflow).getDimensions();
	}

	public static Map<String, String> getCodes(String provider, String dataflow, String dimension) throws SdmxException
	{
		if (provider == null || provider.trim().isEmpty())
		{
			logger.severe("The name of the provider cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
		}
		if (dataflow == null || dataflow.trim().isEmpty())
		{
			logger.severe("The name of the dataflow cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
		}
		if (dimension == null || dimension.trim().isEmpty())
		{
			logger.severe("The name of the dimension cannot be null");
			throw new SdmxInvalidParameterException("The name of the dimension cannot be null");
		}
		Map<String, String> codes = null;
		DataFlowStructure dsd = getDataFlowStructure(provider, dataflow);
		Dimension dim = dsd.getDimension(dimension);
		if (dim != null)
		{
			codes = dim.getCodeList();
			if (codes == null)
			{
				// this is a 2.1 provider
				logger.finer("Codelist for " + provider + ", " + dataflow + ", " + dimension + " not cached.");
				Codelist codelist = dsd.getDimension(dimension).getCodeList();
				codes = getClient(provider).getCodes(codelist.getId(), codelist.getAgency(), codelist.getVersion());
				if (codes != null)
					codelist.setCodes(codes);
				else
					throw new SdmxXmlContentException(
							"Could not get codes for '" + dataflow + "' in provider: '" + provider + "'");
			}
		}
		else
			throw new SdmxXmlContentException(
					"The dimension: '" + dimension + "' does not exist in dataflow: '" + dataflow + "'");

		return codes;
	}

	public static Dataflow getFlow(String provider, String dataflow) throws SdmxException
	{
		if (provider == null || provider.trim().isEmpty())
		{
			logger.severe("The name of the provider cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
		}
		if (dataflow == null || dataflow.trim().isEmpty())
		{
			logger.severe("The name of the dataflow cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
		}
		Provider p = getProvider(provider);
		Dataflow flow = p.getFlows().get(dataflow);
		if (flow == null)
		{
			logger.fine("Dataflow " + dataflow + " not cached. Calling Provider.");
			flow = getClient(provider).getDataflow(dataflow, ALL_AGENCIES, LATEST_VERSION);
			if (flow != null)
				p.setFlow(flow);
			else
				throw new SdmxXmlContentException(
						"Could not get dataflow '" + dataflow + "' in provider: '" + provider + "'");
		}
		return flow;
	}

	public static Map<String, String> getFlows(String provider, String pattern) throws SdmxException
	{
		Map<String, Dataflow> flows = getFlowObjects(provider, pattern);
		Map<String, String> result = new HashMap<>();
		for (Entry<String, Dataflow> entry : flows.entrySet())
			result.put(entry.getKey(), entry.getValue().getDescription());
		return result;
	}

	public static Map<String, Dataflow> getFlowObjects(String provider, String pattern) throws SdmxException
	{
		if (provider == null || provider.trim().isEmpty())
		{
			logger.severe("The name of the provider cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
		}

		Map<String, Dataflow> flows = null;
		Provider p = getProvider(provider);
		flows = p.getFlows();
		if (flows == null || flows.size() == 0 || !p.isFull())
		{
			logger.fine("Flows for " + provider + " not cached. Calling Provider.");
			flows = getClient(provider).getDataflows();
			if (flows != null && flows.size() != 0)
			{
				p.setFlows(flows);
				p.setFull(true);
			}
			else
				throw new SdmxXmlContentException("Could not get dataflows from provider: '" + provider + "'");
		}
		return filterFlows(flows, pattern);
	}

	public static PortableDataSet<Double> getTimeSeriesTable(String provider, String tsKey, String startTime, String endTime)
			throws SdmxException, DataStructureException
	{
		return new PortableDataSet<>(getTimeSeries(provider, tsKey, startTime, endTime, false, null, false));
	}

	public static List<PortableTimeSeries<Double>> getTimeSeries(String provider, String tsKey, String startTime,
			String endTime) throws SdmxException
	{
		return getTimeSeries(provider, tsKey, startTime, endTime, false, null, false);
	}

	public static List<PortableTimeSeries<Double>> getTimeSeriesRevisions(String provider, String tsKey, String startTime,
			String endTime, String updatedAfter, boolean includeHistory) throws SdmxException
	{
		return getTimeSeries(provider, tsKey, startTime, endTime, false, updatedAfter, includeHistory);
	}

	public static List<PortableTimeSeries<Double>> getTimeSeriesNames(String provider, String tsKey) throws SdmxException
	{
		return getTimeSeries(provider, tsKey, null, null, true, null, false);
	}

	private static List<PortableTimeSeries<Double>> getTimeSeries(String provider, String tsKey, String startTime,
			String endTime, boolean serieskeysonly, String updatedAfter, boolean includeHistory) throws SdmxException
	{
		if (provider == null || provider.trim().isEmpty())
		{
			logger.severe("The name of the provider cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
		}
		if (tsKey == null || tsKey.trim().isEmpty())
		{
			logger.severe("The tsKey cannot be null");
			throw new SdmxInvalidParameterException("The tsKey cannot be null");
		}

		List<PortableTimeSeries<Double>> result = new ArrayList<>();
		for (String keyId : tsKey.trim().split("\\s*;\\s*"))
			result.addAll(getSingleTimeSeries(provider, keyId, startTime, endTime, serieskeysonly, updatedAfter,
					includeHistory));

		return (result);
	}

	public static String getDataURL(String provider, String tsKey, String start, String end, boolean seriesKeysOnly,
			String updatedAfter, boolean includeHistory) throws SdmxException
	{
		if (provider == null || provider.trim().isEmpty())
		{
			logger.severe("The name of the provider cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
		}
		if (tsKey == null || tsKey.trim().isEmpty())
		{
			logger.severe("The tsKey cannot be null");
			throw new SdmxInvalidParameterException("The tsKey cannot be null");
		}

		String[] tokens = extractFlowAndResource(tsKey);
		String dataflow = tokens[0];
		String resource = tokens[1];
		Dataflow df = getFlow(provider, dataflow);

		String result = getClient(provider).buildDataURL(df, resource, start, end, seriesKeysOnly, updatedAfter,
				includeHistory);
		return (result);
	}

	private static List<PortableTimeSeries<Double>> getSingleTimeSeries(String provider, String tsKey, String startTime,
			String endTime, boolean serieskeysonly, String updatedAfter, boolean includeHistory) throws SdmxException
	{
		if (provider == null || provider.trim().isEmpty())
		{
			logger.severe("The name of the provider cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
		}
		if (tsKey == null || tsKey.trim().isEmpty())
		{
			logger.severe("The tsKey cannot be null");
			throw new SdmxInvalidParameterException("The tsKey cannot be null");
		}

		List<PortableTimeSeries<Double>> result = null;

		String[] tokens = extractFlowAndResource(tsKey);
		String dataflow = tokens[0];
		String resource = tokens[1];

		Dataflow df = getFlow(provider, dataflow);
		DataFlowStructure dsd = getDataFlowStructure(provider, dataflow);
		result = getClient(provider).getTimeSeries(df, dsd, resource, startTime, endTime, serieskeysonly, updatedAfter,
				includeHistory);
		if (result == null || result.size() == 0)
			throw new SdmxXmlContentException(
					"The query: " + tsKey + " did not match any time series on the provider.");
		return result;
	}

	public static String dumpTimeSeriesList(List<PortableTimeSeries<Double>> ts)
	{
		StringBuffer result = new StringBuffer("");
		int maxSize = 0;
		boolean first = true;
		for (PortableTimeSeries<?> series : ts)
		{
			if (!first)
				result.append(";");
			first = false;
			result.append(";").append(series.getName());
			int size = series.size();
			if (size > maxSize)
				maxSize = size;
			if (Configuration.isReverse())
			{
				// reverse the time series for user friendliness
				series.reverse();
			}
		}
		result.append("\n");
		for (int i = 0; i < maxSize; i++)
		{
			for (int j = 0; j < ts.size(); j++)
			{
				if (i < ts.get(j).size())
				{
					result.append(ts.get(j).get(i).getTimeslot()).append(";");
					result.append(ts.get(j).get(i).getValue());
				}
				else
				{
					result.append(";");
				}
				if (j + 1 < ts.size())
				{
					result.append(";");
				}
			}
			result.append("\n");
		}
		return result.toString();
	}

	public static String dumpTimeSeries(String provider, String id, String startTime, String endTime)
			throws SdmxException, DataStructureException
	{
		if (provider == null || provider.trim().isEmpty())
		{
			logger.severe("The name of the provider cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
		}
		if (id == null || id.trim().isEmpty())
		{
			logger.severe("The id cannot be null");
			throw new SdmxInvalidParameterException("The id cannot be null");
		}
		if (!SDMXClientFactory.getProviders().containsKey(provider))
		{
			throw new SdmxInvalidParameterException("The provider : " + id + " does not exist.");
		}
		String result = "";
		if (!Configuration.isTable())
		{
			// Do it as a list of time series
			List<PortableTimeSeries<Double>> ts = getTimeSeries(provider, id, startTime, endTime);
			result = dumpTimeSeriesList(ts);
		}
		else
		{
			// do it as a table
			result = getTimeSeriesTable(provider, id, startTime, endTime).toString();
		}
		return result;
	}

	private static Provider getProvider(String providerName) throws SdmxException
	{
		if (providerName == null || providerName.trim().isEmpty())
		{
			logger.severe("The name of the provider cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
		}
		Provider provider = SDMXClientFactory.getProviders().get(providerName);

		// TODO: move this check to SDMXClientFactory
		if (provider == null)
			throw new SdmxUnknownProviderException(providerName);

		return provider;
	}

	private static GenericSDMXClient getClient(String provider, String user, String password) throws SdmxException
	{
		final String sourceMethod = "getClient";
		logger.entering(sourceClass, sourceMethod);
		if (provider == null || provider.trim().isEmpty())
		{
			logger.severe("The name of the provider cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
		}
		GenericSDMXClient client = clients.get(provider);
		if (client == null)
		{
			logger.finer("Client for " + provider + " does not exist. I will create it.");
			client = (GenericSDMXClient) SDMXClientFactory.createClient(provider);
			if (client.needsCredentials())
				handlePassword(client, user, password);

			clients.put(provider, client);
		}
		logger.exiting(sourceClass, sourceMethod);
		return client;

	}

	private static GenericSDMXClient getClient(String provider) throws SdmxException
	{
		if (provider == null || provider.trim().isEmpty())
		{
			logger.severe("The name of the provider cannot be null");
			throw new SdmxInvalidParameterException("The name of the provider cannot be null");
		}

		return getClient(provider, null, null);

	}

	private static void handlePassword(GenericSDMXClient client, String user, String pw) throws SdmxException
	{
		if (client == null)
		{
			logger.severe("The client cannot be null");
			throw new SdmxInvalidParameterException("The client cannot be null");
		}
		if (client.needsCredentials())
		{
			if (user == null || pw == null)
			{
				final JFrame frame = new JFrame("Authentication");
				frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				LoginDialog loginDlg = new LoginDialog(frame, client.getName() + " Authentication");
				loginDlg.setVisible(true);
				client.setCredentials(loginDlg.getUsername(), loginDlg.getPassword());
				frame.dispose();
			}
			else
			{
				client.setCredentials(user, pw);
			}
		}
	}

	private static Map<String, Dataflow> filterFlows(Map<String, Dataflow> flows, String pattern)
	{
		final String sourceMethod = "filterFlows";
		logger.entering(sourceClass, sourceMethod);
		Map<String, Dataflow> result = new HashMap<>();
		if (flows != null && flows.size() > 0)
		{
			if (pattern != null && !pattern.trim().isEmpty())
				pattern = pattern.replaceAll("\\*", ".*").replaceAll("\\?", ".");

			for (Entry<String, Dataflow> entry : flows.entrySet())
			{
				String description = entry.getValue().getDescription();
				if (pattern != null && !pattern.trim().isEmpty())
					if (entry.getKey().matches(pattern) || description.matches(pattern))
						result.put(entry.getKey(), entry.getValue());
					else
						;
				else
					result.put(entry.getKey(), entry.getValue());
			}
		}

		logger.exiting(sourceClass, sourceMethod);
		return result;
	}

	private static String[] translateLegacyTSQuery(String tsKey)
	{
		String[] newKey = new String[2];
		String delims = "[.]";
		String[] tokens = tsKey.split(delims, 2);
		if (tokens.length == 2)
		{
			newKey[0] = tokens[0];
			String resource = tokens[1];
			resource = resource.replace("*", "");
			resource = resource.replace('|', '+');
			newKey[1] = resource;
		}
		else
		{
			logger.severe("Error in query string format: '" + tsKey + "'. Could not get dataflow id.");
		}
		return newKey;
	}

	private static String[] extractFlowAndResource(String tsKey) throws SdmxException
	{
		tsKey = tsKey.trim();
		String delims = "[ /]";
		String[] tokens = tsKey.split(delims, 2);
		if (tokens.length != 2)
		{
			// legacy mode: flow.tskey
			tokens = translateLegacyTSQuery(tsKey);
			if (tokens.length != 2)
				throw new SdmxXmlContentException("Malformed time series key: " + tsKey);
		}
		return tokens;
	}

}
