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
 * @see
 * https://github.com/amattioc/sdmx-rest/blob/master/v2_1/ws/rest/docs/rest_cheat_sheet.pdf
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
}
