/*
 * Copyright 2016 National Bank of Belgium
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
package it.bancaditalia.oss.sdmx.util;

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
	 * Factory that creates an exception from code and message.
	 *
	 * @param responseCode
	 * @param responseMessage
	 * @return a non-null exception
	 */
	public static SdmxResponseException of(int responseCode, String responseMessage) {
		return new SdmxResponseException(responseCode, responseMessage);
	}

	private final int responseCode;
	private final String responseMessage;

	private SdmxResponseException(int responseCode, String responseMessage) {
		super(getGenericMessage(responseCode, responseMessage));
		this.responseCode = responseCode;
		this.responseMessage = responseMessage;
	}

	public int getResponseCode() {
		return responseCode;
	}

	public String getResponseMessage() {
		return responseMessage;
	}

	private static String getGenericMessage(int responseCode, String responseMessage) {
		String msg = "Connection failed. HTTP error code : " + responseCode + ", message: " + responseMessage + "\n";
		String meaning = getSdmxMeaning(responseCode);
		return meaning != null ? (msg + "SDMX meaning: " + meaning) : msg;
	}

	private static String getSdmxMeaning(int responseCode) {
		switch (responseCode) {
			case 304:
				return "No change since the timestamp supplied in the If-Modified-Since header.";
			case 400:
				return "There is a problem with the syntax of the query.";
			case 401:
				return "Credentials needed.";
			case 403:
				return "The syntax of the query is OK but it has no meaning.";
			case 404:
				return "No results matching the query.";
			case 406:
				return "Not a supported format.";
			case 413:
				return "Results too large.";
			case 500:
				return "Error on the provider side.";
			case 501:
				return "Feature not supported.";
			case 503:
				return "Service temporarily unavailable. Please try again later.";
			default:
				return null;
		}
	}
}
