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
package it.bancaditalia.oss.sdmx.api;

import java.net.URL;
import java.util.List;
import java.util.Map;

import it.bancaditalia.oss.sdmx.exceptions.SdmxException;

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
	public Map<String, Dataflow> getDataflows() throws SdmxException;

	/**
	 * <p>Gets the dataflow information for the given dataflow id
	 * @param dataFlow name of the dataflow
	 * @param agency name of the agency
	 * @param version dataflow version
	 * @return the dsd identifier (id, agency, version)
	 * @throws SdmxException 
	 */
	public Dataflow getDataflow(String dataFlow, String agency, String version) throws SdmxException;
	
	/**
	 * <p>Gets the basic dsd structure for the given dataflow
	 * @param dsd a non-null dsd identifier
	 * @param full if true, for 2.1 providers it retrieves the full dsd, with all the codelists.
	 * @return the dimensions and (if configured) code lists
	 * @throws SdmxException 
	 */
	public DataFlowStructure getDataFlowStructure(DSDIdentifier dsd, boolean full) throws SdmxException;

	
	/**
	 * <p>Gets all the codes from this provider for the specified codelist
	 * 
	 * @param codeList name of the codelist to get
	 * @param agency agency of the codelist to get
	 * @param version version of the codelist to get
	 * @return
	 * @throws SdmxException
	 */
	public Map<String,String> getCodes(String codeList, String agency, String version) throws SdmxException;

	/**
     * <p>Gets a time series list with the specified classification keys. The id is in a dot separated
     * form, where the first token is the name of  the dataflow. Note that single keys can be 
     * wildcarded.
     * 
     * <p>e.g.
     *
     *<p>		'EXR.M.USD.EUR.SP00.A' or 'EXR/M.USD.EUR.SP00.A'
     *<p>		'EXR.*.*.EUR.SP00.A' or 'EXR...EUR.SP00.A' or 'EXR/..EUR.SP00.A'
     *<p>		'EXR.A|M.USD.EUR.SP00.A' or 'EXR.A+M.USD.EUR.SP00.A' or 'EXR/A+M.USD.EUR.SP00.A'
     * 
     * @param resource the id of the time series
     * @param startTime start time of the observations to be gathered
     * @param endTime end time of the observations to be gathered
     * @param dataflow the dataflow of the time series to be gathered
     * @param dsd the structure of the dataflow of the time series to be gathered
     * @param seriesKeyOnly boolean flag for disabling data and attributes processing (usually for getting the only dataflow contents)
     * @param updatedAfter if set, only data updated after the given date will be retrieved (e.g. '2014-01-01')
     * @param includeHistory boolean flag for enabling getting the history of revisions
     * @return the list of {@link PortableTimeSeries }
	 * @throws SdmxException 
     */
	public List<PortableTimeSeries> getTimeSeries(Dataflow dataflow, DataFlowStructure dsd, String resource, 
			String startTime, String endTime, 
			boolean seriesKeyOnly, String updatedAfter, boolean includeHistory) throws SdmxException;
	
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

	/**
     * <p>Gets the URL of the web service for this provider client
     * @return the endpoint URL
	 * @throws SdmxException 
     */
	public URL getEndpoint() throws SdmxException;

	/**
     * <p>Sets the URL of the web service for this provider client
     */
	public void setEndpoint(URL endpoint);

	/**
     * <p>Gets the exact URL corresponding to the data query in input for this client 
     * @param resource the id of the time series
     * @param startTime start time of the observations to be gathered
     * @param endTime end time of the observations to be gathered
     * @param dataflow the dataflow of the time series to be gathered
     * @param seriesKeyOnly boolean flag for disabling data and attributes processing (usually for getting the only dataflow contents)
     * @return the query URL for the endpoint
	 * @throws SdmxException 
     */
	public String buildDataURL(Dataflow dataflow, String resource, 
			String startTime, String endTime, 
			boolean seriesKeyOnly, String updatedAfter, boolean includeHistory) throws SdmxException;

}
