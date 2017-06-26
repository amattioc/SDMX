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

public final class SdmxExceptionFactory 
{
	private static final Map<Integer, String> sdmxMessages = new HashMap<Integer, String>();
	
	// TODO: These should be read from configuration and not statically defined
	static {
		sdmxMessages.put(100, "No results matching the query.");
		sdmxMessages.put(110, "Credentials needed.");
		sdmxMessages.put(130, "Results too large.");
		sdmxMessages.put(140, "There is a problem with the syntax of the query.");
		sdmxMessages.put(150, "The syntax of the query is OK but it has no meaning.");
		// sdmxMessages.put(304, "No change since the timestamp supplied in the If-Modified-Since header.");
		// sdmxMessages.put(406, "Not a supported format.");
		sdmxMessages.put(500, "Error on the provider side.");
		sdmxMessages.put(501, "Feature not supported.");
		sdmxMessages.put(503, "Service temporarily unavailable. Please try again later.");
		sdmxMessages.put(510, "Response too large.");
	}
	
	private SdmxExceptionFactory() {}

	/**
	 * Builds a SdmxResponseException from a given http code.
	 * 
	 * @param httpCode an http response code that have a SDMX meaning as defined in the specification
	 * @param inner an optional IOException that caused the exception being built
	 * @param provider an optional provider eventually containing custom sdmx message codes
	 * @return
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
			case 400: return 140;
			case 401: return 110;
			case 403: return 150;
			case 404: return 100;
			case 413: return 510; // also: 130
			case 500: case 501: case 503: return httpCode;
			default: return httpCode;
		}
	}

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
