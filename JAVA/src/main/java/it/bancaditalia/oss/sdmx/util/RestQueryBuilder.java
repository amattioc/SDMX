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

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import it.bancaditalia.oss.sdmx.parser.v21.Sdmx21Queries;

/**
 * URL builder for rest queries.
 *
 * @author Philippe Charles
 */
public class RestQueryBuilder {

	protected final URI entryPoint;
	protected final List<String> paths = new ArrayList<>();
	protected final Map<String, String> params = new LinkedHashMap<>();
	protected String filter = null;

	public RestQueryBuilder(URI entryPoint) {
		this.entryPoint = entryPoint;
	}

	/**
	 * Appends the specified path the current URL.
	 *
	 * @param path a non-null path
	 * @return this builder
	 * @throws NullPointerException if entryPoint is null
	 */
	public RestQueryBuilder addPath(String path) {
		if (path == null) {
			throw new NullPointerException("path");
		}
		paths.add(path);
		return this;
	}

	/**
	 * Appends the specified parameter to the current URL.
	 *
	 * @param key a non-null key
	 * @param value a non-null value
	 * @return this builder
	 * @throws NullPointerException if key or value is null
	 */
	public RestQueryBuilder addParam(String key, String value) {
		if (key == null) {
			throw new NullPointerException("key");
		}
		if (value == null) {
			throw new NullPointerException("value");
		}
		params.put(key, value);
		return this;
	}

	/**
	 * Appends the specified parameter to the current URL.
	 *
	 * @param filter 
	 * @throws NullPointerException if key or value is null
	 */
	public RestQueryBuilder addFilter(String filter) {
		if (filter == null) {
			throw new NullPointerException("filter");
		}
		this.filter  = filter;
		return this;
	}

	/**
	 * Creates a new URL using the specified path and parameters.
	 *
	 * @return a URL
	 * @throws MalformedURLException 
	 */
	public URL build() throws MalformedURLException 
	{
		try {
			StringBuilder result = new StringBuilder();
			result.append(entryPoint);
			
			if(this instanceof Sdmx21Queries){
				for (String path : paths)
					result.append('/').append(URLEncoder.encode(path, "UTF-8"));
			}
			else{
				for (String path : paths)
					result.append('/').append(path);
			}
			
			boolean first = true;
			if(filter != null){
				result.append(first ? '?' : '&');
				//result.append(URLEncoder.encode(filter, "UTF-8"));
				result.append(filter);
				first = false;
			}
			for (Entry<String, String> entry: params.entrySet())
			{
				result.append(first ? '?' : '&');
				result.append(URLEncoder.encode(entry.getKey(), "UTF-8")).append('=').append(entry.getValue());
				first = false;
			}
			
			return new URL(result.toString());
		} catch (UnsupportedEncodingException e) {
			// safe to ignore.
			throw new RuntimeException("UnsupportedEncodingException", e);
		}
	}
}
