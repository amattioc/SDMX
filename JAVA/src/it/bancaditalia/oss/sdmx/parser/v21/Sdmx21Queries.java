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
package it.bancaditalia.oss.sdmx.parser.v21;

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
public class Sdmx21Queries extends RestQueryBuilder {

	public Sdmx21Queries(URI entryPoint) {
		super(entryPoint);
	}

	public static Sdmx21Queries createDataQuery(URI endpoint, String dataflow, String resource, String start, String end, boolean serieskeysonly,
			String updatedAfter, boolean includeHistory, String format) throws SdmxInvalidParameterException {

		if (endpoint != null && dataflow != null && !dataflow.isEmpty() && resource != null && !resource.isEmpty())
			return (Sdmx21Queries) new Sdmx21Queries(endpoint).addParams(start, end, serieskeysonly, updatedAfter, includeHistory, format).addPath("data")
					.addPath(dataflow).addPath(resource);
		else
			throw new SdmxInvalidParameterException("Invalid query parameters: dataflow=" + dataflow + " resource=" + resource + " endpoint=" + endpoint);
	}

	public static Sdmx21Queries createStructureQuery(URI endpoint, String dsd, String agency, String version) throws SdmxException {
		return createStructureQuery(endpoint, dsd, agency, version, false);
	}

	public static Sdmx21Queries createStructureQuery(URI endpoint, String dsd, String agency, String version, boolean full)
			throws SdmxInvalidParameterException {
		if (endpoint != null && agency != null && !agency.isEmpty() && dsd != null && !dsd.isEmpty()) {
			Sdmx21Queries query = (Sdmx21Queries) new Sdmx21Queries(endpoint).addPath("datastructure").addPath(agency).addPath(dsd);
			if (version != null && !version.isEmpty()) {
				query.addPath(version);
			}
			if (full) {
				query.addParam("references", "children");
			}
			return query;
		} else
			throw new SdmxInvalidParameterException("Invalid query parameters: agency=" + agency + " dsd=" + dsd + " endpoint=" + endpoint);
	}

	public static Sdmx21Queries createDataflowQuery(URI endpoint, String dataflow, String agency, String version) throws SdmxInvalidParameterException {
		if (endpoint != null || dataflow != null)
			return ((Sdmx21Queries) new Sdmx21Queries(endpoint).addPath("dataflow")).addResourceId(agency, dataflow, version);
		else
			throw new SdmxInvalidParameterException("Invalid query parameters: dataflow: " + dataflow + ", endpoint=" + endpoint);
	}

	public static Sdmx21Queries createCodelistQuery(URI endpoint, String codeList, String agency, String version) throws SdmxInvalidParameterException {
		if (endpoint != null && codeList != null && !codeList.isEmpty())
			return ((Sdmx21Queries) new Sdmx21Queries(endpoint).addPath("codelist")).addResourceId(agency, codeList, version);
		else
			throw new SdmxInvalidParameterException("Invalid query parameters: codeList=" + codeList + " endpoint=" + endpoint);
	}

	public Sdmx21Queries addResourceId(String agencyId, String resourceId, String version) {
		if (agencyId != null)
			addPath(agencyId);

		addPath(resourceId);
		
		if (version != null)
			addPath(version);

		return this;
	}

	public Sdmx21Queries addParams(String start, String end, boolean serieskeysonly, String updatedAfter, boolean includeHistory, String format) {
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
		if (format != null && !format.isEmpty())
			addParam("format", format);
		return this;
	}
	
	/**
	 * Just a convenience wrapper around {@link RestQueryBuilder#build()}
	 * 
	 * @return the query URL
	 * @throws SdmxException if {@link RestQueryBuilder#build()} throws a {@link MalformedURLException}
	 */
	public URL buildSdmx21Query() throws SdmxException {
		try {
			return build();
		} catch (MalformedURLException e) {
			throw SdmxExceptionFactory.wrap(e);
		}
	}
}
