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

public class Dimension extends SdmxMetaElement {
	
	private int position;
	
	/**
	 * @param id
	 * @param position
	 * @param codeList
	 */
	public Dimension(String id, int position, Codelist codeList) {
		super(id, codeList);
		this.position = position;
	}
	
	/**
	 * 
	 */
	public Dimension() {
		super();
	}
        
	/**
	 * @return
	 */
	public int getPosition() {
		return position;
	}

	/**
	 * @param position
	 */
	public void setPosition(int position) {
		this.position = position;
	}

	/* (non-Javadoc)
	 * @see it.bancaditalia.oss.sdmx.api.SdmxMetaElement#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Dimension [id=").append(getId())
				.append(", position=").append(position)
				.append(", codelist=").append(getCodeList()).append("]\n");
		return builder.toString();
	}

}
