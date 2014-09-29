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


import it.bankitalia.reri.sia.sdmx.api.GenericSDMXClient;
import it.bankitalia.reri.sia.util.Configuration;
import it.bankitalia.reri.sia.util.SdmxException;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>Java Factory class for creating the Sdmx Clients.
 * 
 * @author Attilio Mattiocco
 *
 */
public class SDMXClientFactory {
	
	private static final String ECB_PROVIDER = "http://sdw-wsrest.ecb.europa.eu/service";
	private static final String BIS_PROVIDER = "https://dbsonline.bis.org/sdmx21/rest";
	private static final String EUROSTAT_PROVIDER = "http://ec.europa.eu/eurostat/SDMX/diss-web/rest";

	//read the configuration file
	static {
		Configuration.init();
		initBuiltInProviders();
	}
	
	private static final String sourceClass = SDMXClientFactory.class.getSimpleName();
	protected static Logger logger = Configuration.getSdmxLogger();
	private static Map<String, Provider> providers;


	/**
     * Initialize the sdmx providers
     * 
     */
	private static void initBuiltInProviders(){
		providers = new HashMap<String, Provider>();
    	try {
	    	addProvider("ECB", "ECB", new URL(ECB_PROVIDER), false);
	    	addProvider("BIS", "BIS", new URL(BIS_PROVIDER), true);
			addProvider("EUROSTAT", "ESTAT", new URL(EUROSTAT_PROVIDER), false);
	    } catch (MalformedURLException e) {
			logger.severe("Exception. Class: " + e.getClass().getName() + " .Message: " + e.getMessage());
			logger.log(Level.FINER, "", e);
		}

	    //add internal 2.0 providers
	    addProvider("OECD", "OECD", null, false);
	    addProvider("ILO", "ILO", null, false);
	    addProvider("IMF", "IMF", null, false);
	    
    	//Legacy 2.0
    	ServiceLoader<GenericSDMXClient> ldr = ServiceLoader.load(GenericSDMXClient.class);
        for (GenericSDMXClient provider : ldr) {
            addProvider(provider.getClass().getSimpleName(), provider.getAgency(), null, provider.needsCredentials());
        }
	}
	
	/**
     * General method for creating an SdmxClient. 
     * 
	 * @param name
	 * @param agency
	 * @param endpoint
	 * @param needsCredentials
	 */
	public static void addProvider(String name, String agency, URL endpoint, boolean needsCredentials){
		Provider p = new Provider(name, agency, endpoint, needsCredentials);
    	providers.put(name, p);
	}
	
	/**
     * General method for creating an SdmxClient. 
     * 
	 * @param provider
	 * @param user
	 * @param pw
	 * @return
	 */
	public static GenericSDMXClient createClient(String provider) throws SdmxException{
		final String sourceMethod = "createClient";

		logger.entering(sourceClass, sourceMethod);
		logger.fine("Create an SDMX client for '" + provider + "'");
		GenericSDMXClient client = null;	
		Provider p = providers.get(provider);

		String errorMsg = "The provider '" + provider + "' is not available in this configuration.";
		if(p != null && p.getEndpoint() != null){
			if(p.getEndpoint().getProtocol().equals("http")){
				client = new RestSdmxClient(p.getName(), p.getEndpoint(), p.getAgency(), p.isNeedsCredentials(), false);
			}
			else if(p.getEndpoint().getProtocol().equals("https")){
				try {
					client = new HttpsSdmxClient(p.getName(), p.getEndpoint(), p.getAgency(), p.isNeedsCredentials(), false);
				} catch (KeyManagementException e) {
					logger.severe("Exception. Class: " + e.getClass().getName() + " .Message: " + e.getMessage());
					logger.log(Level.FINER, "", e);
					throw new SdmxException(errorMsg);
				} catch (NoSuchAlgorithmException e) {
					logger.severe("Exception. Class: " + e.getClass().getName() + " .Message: " + e.getMessage());
					logger.log(Level.FINER, "", e);
					throw new SdmxException(errorMsg);
				}
			}
			else {
				logger.severe("The protocol '" + p.getEndpoint().getProtocol() + "' is not supported.");
				throw new SdmxException(errorMsg);
			}
		}		
		else {
			///legacy 2.0
			try{
				Class<?> clazz = Class.forName("it.bankitalia.reri.sia.sdmx.client.custom." + provider);
				client = (GenericSDMXClient)clazz.newInstance();
			}
			catch (ClassNotFoundException e) {
				logger.severe("The provider '" + provider + "' is not available in this configuration.");
				throw new SdmxException(errorMsg);
			}
			catch (Exception e) {
				logger.severe("Exception caught. Class: " + e.getClass().getName() + " .Message: " + e.getMessage());
				logger.log(Level.FINER, "Exception caught. ", e);
				throw new SdmxException(errorMsg);
			}
		}
		logger.exiting(sourceClass, sourceMethod);
		return client;
	}
	
	/**
	 * Get the list of all available SDMX Providers
	 * @return
	 */
	public static Map<String, Provider> getProviders() {
//		List<String> res = new ArrayList<String>();
//		res.addAll(providers.keySet());
//		
//		//Legacy 2.0
//		ServiceLoader<GenericSDMXClient> ldr = ServiceLoader.load(GenericSDMXClient.class);
//        for (GenericSDMXClient provider : ldr) {
//            res.add(provider.getClass().getSimpleName());
//        }
        ////
        return providers;
    }

//	/**
//	 * 	 Get the list of all available SDMX Providers that do not require authentication
//	 * @return
//	 */
//	public static List<String> getClearProviders() {
//		List<String> result = new ArrayList<String>();
//		for (Iterator<Provider> iterator = providers.values().iterator(); iterator.hasNext();) {
//			Provider p = (Provider) iterator.next();
//			if(!p.isNeedsCredentials()){
//				result.add(p.getName());
//			}	
//		}
//		
//		//Legacy 2.0
////		ServiceLoader<GenericSDMXClient> ldr = ServiceLoader.load(GenericSDMXClient.class);
////        for (GenericSDMXClient provider : ldr) {
////            if(!provider.needsCredentials()){
////            	result.add(provider.getClass().getSimpleName());
////            }
////        }
//        return result;
//    }
}
