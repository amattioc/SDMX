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
package it.bankitalia.reri.sia.sdmx.api;

import it.bankitalia.reri.sia.util.SdmxException;

import java.util.List;
import java.util.Map;

/**
 * @author Attilio Mattiocco
 *
 */
public interface GenericSDMXClient {
	
	/**
	 * <p>Gets all the dataflows from this provider
	 * @return a HashTable of mappings (flow id and name)
	 * @throws SdmxException 
	 */
	public Map<String, String> getDataflows() throws SdmxException;

	/**
	 * <p>Gets the dsd name for the given dataflow
	 * @param dataFlow name of the dataflow
	 * @param agency TODO
	 * @param version TODO
	 * @return the dsd identifier (id, agency, version)
	 * @throws SdmxException 
	 */
	public DSDIdentifier getDSDIdentifier(String dataFlow, String agency, String version) throws SdmxException;

	/**
	 * <p>Gets the dimension names for the given dataflow
	 * @param dataflow name of the dataflow
	 * @return the list of dimensions
	 * @throws SdmxException 
	 */
	//public List<Dimension> getDimensions(String dataflow) throws SdmxException;
	
	/**
	 * <p>Gets the basic dsd structure for the given dataflow
	 * @param dsd the dsd identification
	 * @return the dimensions and code lists
	 * @throws SdmxException 
	 */
	public DataFlowStructure getDataFlowStructure(DSDIdentifier dsd) throws SdmxException;

	/**
	 * <p>Gets the dimension names for the given dataflow
	 * @param dataflow name of the dataflow
	 * @return the list of dimensions
	 * @throws SdmxException 
	 */
	//public List<Dimension> getDimensions(String dataflow) throws SdmxException;
	
	/**
	 * <p>Gets all the codes from this provider for the specified codelist
	 * 
	 * @param provider
	 * @param codeList
	 * @return
	 * @throws SdmxException 
	 */
	public Map<String,String> getCodes(String provider, String codeList) throws SdmxException;

	/**
     * <p>Gets a time series with the specified classification keys. The id is in a dot separated
     * form, where the first token is the name of  the dataflow. Note that single keys can be 
     * wildcarded.
     * 
     * <p>e.g.
     *
     *<p>		'EXR.M.USD.EUR.SP00.A'		or
     *<p>		'EXR.*.*.EUR.SP00.A'		for ECB
     *<p>		'REFSERIES.AUS.PPPGDP.A' 	or
     *<p>		'REFSERIES.*.PPPGDP.A' 	for OECD
     *
     *
     * 
     * 
     * @param id the id of the time series
     * @param startTime start time of the observations to be gathered
     * @param endTime end time of the observations to be gathered
     * @return the list of {@link PortableTimeSeries }
	 * @throws SdmxException 
     */
	public List<PortableTimeSeries> getTimeSeries(String dataflow, DataFlowStructure dsd, String resource, String startTime, String endTime) throws SdmxException;
	
	/**
     * <p>Gets the dimension names for the given dataflow
     * @param dataflow name of the dataflow
     * @return the list of dimensions
	 * @throws SdmxException 
     */
	//public List<Dimension> getDimensions(String dataflow) throws SdmxException;
	
	/**
     * <p>Checks id this is a secure provider, needing credentials. To be used 
     * with setCredentials()
     * @return true if credentials have to be set
     */
	public boolean needsCredentials();
	
	/**
     * <p>Sets the security credentials for the provider to which this client will
     * be attached. 
     * @param user the user name
     * @param pw the password
     */
	public void setCredentials(String user, String pw);

}
