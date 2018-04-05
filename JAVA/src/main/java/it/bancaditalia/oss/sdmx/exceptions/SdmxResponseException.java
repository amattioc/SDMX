/*
 * Copyright 2016 Bank Of Italy
 * 
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
package it.bancaditalia.oss.sdmx.exceptions;

import java.io.IOException;

/**
 * Specific exception that deals with response codes from SDMX RESTful web
 * services.
 *
 * @see <a href="https://github.com/amattioc/sdmx-rest/blob/master/v2_1/ws/rest/docs/rest_cheat_sheet.pdf">REST cheat sheet</a>
 * @see <a href="https://sdmx.org/wp-content/uploads/SDMX_2-1-1-SECTION_07_WebServicesGuidelines_2013-04.pdf">SDMX Web Services Guidelines</a>
 * @author Philippe Charles
 */
public final class SdmxResponseException extends SdmxException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final int responseCode;

	SdmxResponseException(int sdmxCode, IOException cause, String message) 
	{
		super(message, cause);
		this.responseCode = sdmxCode;
	}

	public int getResponseCode() {
		return responseCode;
	}
	
	public static final int SDMX_NO_RESULTS_FOUND = 100;
	public static final int SDMX_UNAUTHORIZED = 110;
	public static final int SDMX_RESPONSE_SIZE_CLIENT = 130;
	public static final int SDMX_SYNTAX_ERROR = 140;
	public static final int SDMX_SEMANTIC_ERROR = 150;
	public static final int SDMX_INTERNAL_SERVER_ERROR = 500;
	public static final int SDMX_NOT_IMPLEMENTED = 501;
	public static final int SDMX_SERVICE_UNAVAILABLE = 503;
	public static final int SDMX_RESPONSE_SIZE_SERVER = 510;
}
