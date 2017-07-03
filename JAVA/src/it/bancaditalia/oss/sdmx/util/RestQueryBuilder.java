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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * URL builder for rest queries.
 *
 * @author Philippe Charles
 */
public final class RestQueryBuilder {

	/**
	 * Creates a new builder from a base URL.
	 *
	 * @param entryPoint a non-null URL
	 * @return a non-null builder
	 * @throws NullPointerException if entryPoint is null
	 */
	public static RestQueryBuilder of(URL entryPoint) {
		if (entryPoint == null) {
			throw new NullPointerException("entryPoint");
		}
		return new RestQueryBuilder(entryPoint);
	}

	private final URL entryPoint;
	private final List<String> paths;
	private final Map<String, String> params;

	private RestQueryBuilder(URL entryPoint) {
		this.entryPoint = entryPoint;
		this.paths = new ArrayList<String>();
		this.params = new LinkedHashMap<String, String>();
	}

	public RestQueryBuilder clear() {
		this.paths.clear();
		this.params.clear();
		return this;
	}

	/**
	 * Appends the specified path the current URL.
	 *
	 * @param path a non-null path
	 * @return this builder
	 * @throws NullPointerException if entryPoint is null
	 */
	public RestQueryBuilder path(String path) {
		if (path == null) {
			throw new NullPointerException("path");
		}
		this.paths.add(path);
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
	public RestQueryBuilder param(String key, String value) {
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
	 * Creates a new URL using the specified path and parameters.
	 *
	 * @param needsURLEncoding encodes the content if necessary
	 * @return a non-null URL
	 */
	public URL build(boolean needsURLEncoding) {
		StringBuilder result = new StringBuilder();
		result.append(entryPoint);
		for (String o : paths) {
			result.append('/').append(needsURLEncoding ? encode(o) : o);
		}
		Iterator<Entry<String, String>> iter = params.entrySet().iterator();
		if (iter.hasNext()) {
			result.append('?');
			append(result, iter.next(), needsURLEncoding);
			while (iter.hasNext()) {
				result.append('&');
				append(result, iter.next(), needsURLEncoding);
			}
		}
		try {
			return new URL(result.toString());
		} catch (MalformedURLException ex) {
			throw new RuntimeException("Failed to build URL", ex);
		}
	}

	private static String encode(String o) {
		return o.replace("|", "%2B").replace("+", "%2B");
	}

	private static void append(StringBuilder result, Entry<String, String> param, boolean needsURLEncoding) {
		if (needsURLEncoding) {
			result.append(encode(param.getKey())).append('=').append(encode(param.getValue()));
		} else {
			result.append(param.getKey()).append('=').append(param.getValue());
		}
	}
}
