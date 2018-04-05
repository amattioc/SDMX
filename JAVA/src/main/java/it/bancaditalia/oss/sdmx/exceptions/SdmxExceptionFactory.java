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
package it.bancaditalia.oss.sdmx.exceptions;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import it.bancaditalia.oss.sdmx.client.Provider;
import java.net.HttpURLConnection;

/**
 * @author Valentino Pinna
 *
 */
public final class SdmxExceptionFactory 
{
	private static final Map<Integer, String> sdmxMessages = new HashMap<>();
	
	// TODO: These should be read from configuration and not statically defined
	static {
		sdmxMessages.put(SdmxResponseException.SDMX_NO_RESULTS_FOUND, "No results matching the query.");
		sdmxMessages.put(SdmxResponseException.SDMX_UNAUTHORIZED, "Credentials needed.");
		sdmxMessages.put(SdmxResponseException.SDMX_RESPONSE_SIZE_CLIENT, "Results too large.");
		sdmxMessages.put(SdmxResponseException.SDMX_SYNTAX_ERROR, "There is a problem with the syntax of the query.");
		sdmxMessages.put(SdmxResponseException.SDMX_SEMANTIC_ERROR, "The syntax of the query is OK but it has no meaning.");
		// sdmxMessages.put(304, "No change since the timestamp supplied in the If-Modified-Since header.");
		// sdmxMessages.put(406, "Not a supported format.");
		sdmxMessages.put(SdmxResponseException.SDMX_INTERNAL_SERVER_ERROR, "Error on the provider side.");
		sdmxMessages.put(SdmxResponseException.SDMX_NOT_IMPLEMENTED, "Feature not supported.");
		sdmxMessages.put(SdmxResponseException.SDMX_SERVICE_UNAVAILABLE, "Service temporarily unavailable. Please try again later.");
		sdmxMessages.put(SdmxResponseException.SDMX_RESPONSE_SIZE_SERVER, "Response too large.");
	}
	
	private SdmxExceptionFactory() {}

	/**
	 * Builds a SdmxResponseException from a given http code.
	 * 
	 * @param httpCode an http response code that have a SDMX meaning as defined in the specification
	 * @param cause an optional IOException that caused the exception being built
	 * @param provider an optional provider eventually containing custom sdmx message codes
	 * 
	 * @return The {@link SdmxException} that maps the specified HTTP return code.
	 */
	public static SdmxException createRestException(int httpCode, IOException cause, Provider provider)
	{
		int sdmxCode = translateCode(httpCode);
		String message = provider != null ? null /* TODO: provider.getSdmxCustomMessage(code) */ : null; 
		
		if (message == null)
			message = sdmxMessages.get(sdmxCode);
		
		if (message == null)
			return new SdmxInvalidParameterException("HTTP error code " + sdmxCode + " doesn't have a defined SDMX meaning.");
		
		SdmxResponseException result = new SdmxResponseException(sdmxCode, cause, "(" + sdmxCode + "): " + sdmxMessages.get(sdmxCode));
		
		return /*sdmxCode < 500 ? */result/* : new SdmxIOException(result)*/;
	}

	/**
	 * map HTTP codes to SDMX codes for RESTful web services as defined in SDMX specification
	 */
	private static int translateCode(int httpCode) 
	{
		switch (httpCode)
		{
			case HttpURLConnection.HTTP_BAD_REQUEST: 
				return SdmxResponseException.SDMX_SYNTAX_ERROR;
			case HttpURLConnection.HTTP_UNAUTHORIZED: 
				return SdmxResponseException.SDMX_UNAUTHORIZED;
			case HttpURLConnection.HTTP_FORBIDDEN: 
				return SdmxResponseException.SDMX_SEMANTIC_ERROR;
			case HttpURLConnection.HTTP_NOT_FOUND: 
				return SdmxResponseException.SDMX_NO_RESULTS_FOUND;
			case HttpURLConnection.HTTP_ENTITY_TOO_LARGE: 
				return SdmxResponseException.SDMX_RESPONSE_SIZE_SERVER; // also: SDMX_RESPONSE_SIZE_CLIENT
			case HttpURLConnection.HTTP_INTERNAL_ERROR: 
			case HttpURLConnection.HTTP_NOT_IMPLEMENTED: 
			case HttpURLConnection.HTTP_UNAVAILABLE: 
				return httpCode;
			default: 
				return httpCode;
		}
	}

	/**
	 * Wraps a generic {@link Exception} into a {@link SdmxException}.
	 * @param cause The exception to wrap. It must be of type {@link XMLStreamException} or {@link IOException}.
	 * 
	 * @return The wrapped exception.
	 * 
	 * NOTE: {@link RuntimeException} instances will not be wrapped by this method.
	 * @throws UnsupportedOperationException if the exception to wrap is an instance of uncompatible class.
	 */
	public static SdmxException wrap(Exception cause) 
	{
		if (cause instanceof RuntimeException)
			throw (RuntimeException)cause;
		else if (cause instanceof XMLStreamException)
			if (cause.getCause() instanceof IOException)
				return new SdmxIOException((XMLStreamException) cause);
			else
				return new SdmxXmlParsingException((XMLStreamException) cause);
		else if (cause instanceof IOException)
			return new SdmxIOException((IOException) cause);
		else
			throw new IllegalStateException("Exception " + cause.getClass().getSimpleName() + " was not recognized.", cause);
	}

}
