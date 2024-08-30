/*
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package it.bancaditalia.oss.sdmx.util;

import static java.util.Objects.requireNonNull;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxInvalidParameterException;

/**
 * URL builder for rest queries.
 *
 * @author Philippe Charles
 */
public abstract class RestQueryBuilder<T extends RestQueryBuilder<T>> {

	protected final URI entryPoint;
	protected final List<String> paths = new ArrayList<>();
	protected final Map<String, String> params = new LinkedHashMap<>();
	protected String filter = null;

	protected RestQueryBuilder(URI entryPoint)
	{
		this.entryPoint = entryPoint;
	}

	/**
	 * Appends the specified path the current URL.
	 *
	 * @param path a non-null path
	 * @return this builder
	 * @throws NullPointerException if entryPoint is null
	 */
	public T addPath(String path) 
	{
		paths.add(requireNonNull(path, "Null path element"));
		return self();
	}

	/**
	 * Appends the specified parameter to the current URL.
	 *
	 * @param key a non-null key
	 * @param value a non-null value
	 * @return this builder
	 * @throws NullPointerException if key or value is null
	 */
	public T withParam(String key, String value) 
	{
		if (value != null && !value.isEmpty())
			params.put(requireNonNull(key, "Null query parameter name"), value);
		
		return self();
	}

	/**
	 * Sets the filter in this RestQueryBuilder.
	 *
	 * @param filter the filter. If null, no filter will be added.
	 */
	public T withFilter(String filter)
	{
		if (filter != null)
			this.filter  = filter;
		return self();
	}

	public T withHistory(boolean includeHistory)
	{
		if (includeHistory)
			withParam("includeHistory", "true");
		
		return self();
	}

	public T withDetail(boolean seriesKeyOnly)
	{
		if (seriesKeyOnly)
			withParam("detail", "serieskeysonly");
		
		return self();
	}

	/**
	 * Creates a new URL using the specified path and parameters.
	 *
	 * @return a URL
	 * @throws MalformedURLException 
	 */
	public URL build() throws SdmxException 
	{
		StringBuilder result = new StringBuilder();
		result.append(entryPoint);
		for (String path : paths)
			result.append('/').append(path);
		boolean first = true;
		if(filter != null){
			result.append(first ? '?' : '&');
			result.append(filter);
			first = false;
		}
		for (Entry<String, String> entry: params.entrySet())
		{
			result.append(first ? '?' : '&');
			result.append(entry.getKey()).append('=').append(entry.getValue());
			first = false;
		}
		
		try
		{
			return new URL(result.toString());
		}
		catch (MalformedURLException e)
		{
			throw new SdmxInvalidParameterException("Invalid generated url: " + result);
		}
	}

	public T withRef(String agencyId, String maintId, String version) 
	{
		if (agencyId != null)
			addPath(agencyId);

		addPath(maintId);
		
		if (version != null)
			addPath(version);

		return self();
	}

	@SuppressWarnings("unchecked")
	private T self()
	{
		return (T) this;
	}
}
