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
package it.bancaditalia.oss.sdmx.parser.v30;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxExceptionFactory;
import it.bancaditalia.oss.sdmx.exceptions.SdmxInvalidParameterException;
import it.bancaditalia.oss.sdmx.util.RestQueryBuilder;

/**
 * @author Attilio Mattiocco
 *
 */
public class Sdmx30Queries extends RestQueryBuilder {

	public Sdmx30Queries(URI entryPoint) {
		super(entryPoint);
	}

	public static Sdmx30Queries createDataQuery(URI endpoint, String dataflow, String tskey, String filter, String start, String end, boolean serieskeysonly,
			String updatedAfter, boolean includeHistory) throws SdmxInvalidParameterException {

		if (endpoint != null && dataflow != null && !dataflow.isEmpty()){
			Sdmx30Queries query = (Sdmx30Queries) new Sdmx30Queries(endpoint).addParams(filter, start, end, serieskeysonly, updatedAfter, includeHistory, null).addPath("data")
					.addPath("dataflow").addPath(dataflow.replace(",", "/"));
			if(tskey != null && !tskey.isEmpty()) query.addPath(tskey);
			return query;
		}
		else
			throw new SdmxInvalidParameterException("Invalid query parameters: dataflow=" + dataflow + " resource=" + tskey + " endpoint=" + endpoint);
	}

	public static Sdmx30Queries createAvailabilityQuery(URI endpoint, String dataflow, String filter,
			String mode)  throws SdmxInvalidParameterException {
		if (endpoint != null && dataflow != null && !dataflow.isEmpty())
			return (Sdmx30Queries) new Sdmx30Queries(endpoint).addParams(filter, null, null, false, null, false, mode).addPath("availability")
					.addPath("dataflow").addPath(dataflow.replace(",", "/"));
		else
			throw new SdmxInvalidParameterException("Invalid query parameters: dataflow=" + dataflow + " filter=" + filter + " endpoint=" + endpoint);
	}
	
	public static Sdmx30Queries createAvailabilityQueryByKey(URI endpoint, String dataflow, String key,
			String mode)  throws SdmxInvalidParameterException {
		if (endpoint != null && dataflow != null && !dataflow.isEmpty())
			return (Sdmx30Queries) new Sdmx30Queries(endpoint).addParams(null, null, null, false, null, false, mode).addPath("availability")
					.addPath("dataflow").addPath(dataflow.replace(",", "/")).addPath(key);
		else
			throw new SdmxInvalidParameterException("Invalid query parameters: dataflow=" + dataflow + " filter=" + key + " endpoint=" + endpoint);
	}

	public static Sdmx30Queries createStructureQuery(URI endpoint, String dsd, String agency, String version) throws SdmxException {
		return createStructureQuery(endpoint, dsd, agency, version, false);
	}

	public static Sdmx30Queries createStructureQuery(URI endpoint, String dsd, String agency, String version, boolean full)
			throws SdmxInvalidParameterException {
		if (endpoint != null && agency != null && !agency.isEmpty() && dsd != null && !dsd.isEmpty()) {
			Sdmx30Queries query = (Sdmx30Queries) new Sdmx30Queries(endpoint).addPath("structure").addPath("datastructure").addPath(agency).addPath(dsd).addParam("format", "sdmx-2.1");
			if (version != null && !version.isEmpty()) {
				query.addPath(version);
			}
			//query.addPath("");
			if (full) {
				query.addParam("references", "children");
			}
			return query;
		} else
			throw new SdmxInvalidParameterException("Invalid query parameters: agency=" + agency + " dsd=" + dsd + " endpoint=" + endpoint);
	}

	public static Sdmx30Queries createDataflowQuery(URI endpoint, String dataflow, String agency, String version) throws SdmxInvalidParameterException {
		if (endpoint != null || dataflow != null)
			return ((Sdmx30Queries) new Sdmx30Queries(endpoint).addPath("structure").addPath("dataflow").addParam("format", "sdmx-2.1")).addResourceId(agency, dataflow, version);		
		else
			throw new SdmxInvalidParameterException("Invalid query parameters: dataflow: " + dataflow + ", endpoint=" + endpoint);
	}

	public static Sdmx30Queries createCodelistQuery(URI endpoint, String codeList, String agency, String version) throws SdmxInvalidParameterException {
		if (endpoint != null && codeList != null && !codeList.isEmpty())
			return ((Sdmx30Queries) new Sdmx30Queries(endpoint).addPath("structure").addPath("codelist")).addResourceId(agency, codeList, version);
		else
			throw new SdmxInvalidParameterException("Invalid query parameters: codeList=" + codeList + " endpoint=" + endpoint);
	}

	public Sdmx30Queries addResourceId(String agencyId, String resourceId, String version) {
		if (agencyId != null)
			addPath(agencyId);

		addPath(resourceId);
		
		if (version != null)
			addPath(version);
		
		//addPath("");

		return this;
	}

	public Sdmx30Queries addParams(String filter, String start, String end, boolean serieskeysonly, 
			String updatedAfter, boolean includeHistory, String mode) {
		if (filter != null && !filter.isEmpty())
			addFilter(filter);
		if (start != null && !start.isEmpty())
			addParam("startPeriod", start);
		if (end != null && !end.isEmpty())
			addParam("endPeriod", end);
		if (serieskeysonly)
			addParam("detail", "serieskeysonly");
		if (includeHistory)
			addParam("includeHistory", "true");
		if (updatedAfter != null && !updatedAfter.isEmpty())
			addParam("updatedAfter", updatedAfter);
		if (mode != null && !mode.isEmpty())
			addParam("mode", mode);
		
		return this;
	}
	
	/**
	 * Just a convenience wrapper around {@link RestQueryBuilder#build()}
	 * 
	 * @return the query URL
	 * @throws SdmxException if {@link RestQueryBuilder#build()} throws a {@link MalformedURLException}
	 */
	public URL buildQuery() throws SdmxException {
		try {
			return build();
		} catch (MalformedURLException e) {
			throw SdmxExceptionFactory.wrap(e);
		}
	}
}
