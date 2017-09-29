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

public class Message {
	private String code = null;
	private String severity = null;
	private String url = null; //eventual url redirection for Eurostat
	private List<String> text = null;

	/**
	 * 
	 */
	public Message() {
		super();
		this.text = new ArrayList<String>();
	}

	/**
	 * @return
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
	 * @return
	 */
	public List<String> getText() {
		return text;
	}

	/**
	 * @param text
	 */
	public void setText(List<String> text) {
		this.text = text;
	}

	/**
	 * @param textItem
	 */
	public void addText(String textItem){
		this.text.add(textItem);
	}
	
	/**
	 * @param severity
	 */
	public void setSeverity(String severity) {
		this.severity = severity;
	}

	/**
	 * @return
	 */
	public String getSeverity() {
		return severity;
	}

	/**
	 * @param url
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return
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
