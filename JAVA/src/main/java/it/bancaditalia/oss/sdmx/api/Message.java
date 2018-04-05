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
package it.bancaditalia.oss.sdmx.api;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for handling abnormal messages and statuses, for logging purposes. 
 * 
 * @author Valentino Pinna
 */
public class Message {
	private String code = null;
	private String severity = null;
	private String url = null; //eventual url redirection for Eurostat
	private List<String> text = null;

	/**
	 * Creates an empty message.
	 */
	public Message() {
		super();
		this.text = new ArrayList<>();
	}

	/**
	 * @return The return code associated with this message
	 */
	public String getCode() {
		return code;
	}
	/**
	 * @param code
	 */
	public void setCode(String code) {
		this.code = code;
	}
	/**
	 * @return The text associated with this message
	 */
	public List<String> getText() {
		return text;
	}

	/**
	 * @param text The text of this message
	 */
	public void setText(List<String> text) {
		this.text = text;
	}

	/**
	 * Adds more text to this message.
	 * 
	 * @param textItem text to be added.
	 */
	public void addText(String textItem){
		this.text.add(textItem);
	}
	
	/**
	 * @param severity The severity of this message.
	 */
	public void setSeverity(String severity) {
		this.severity = severity;
	}

	/**
	 * @return The severity of this message.
	 */
	public String getSeverity() {
		return severity;
	}

	/**
	 * @param url an URL to be associated with this message.
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return The URL associated with this message, or null.
	 */
	public String getUrl() {
		return url;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Message [code=").append(getCode())
			.append(", severity=").append(severity)
			.append(", url=").append(url)
			.append(", text=").append(text)
			.append("]");
		return builder.toString();
	}

}
