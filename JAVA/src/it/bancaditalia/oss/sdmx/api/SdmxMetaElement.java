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



public class SdmxMetaElement {
	
	private String id = null;
	private Codelist  codeList = null;
	private String name;
	
	/**
	 * 
	 */
	public SdmxMetaElement() {
		super();
	}

	/**
	 * @param id
	 * @param codeList
	 */
	public SdmxMetaElement(String id, Codelist codeList) {
		super();
		this.id = id;
		this.codeList = codeList;
	}

	/**
	 * @return
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * @return
	 */
	public Codelist getCodeList() {
		return codeList;
	}
	/**
	 * @param codeList
	 */
	public void setCodeList(Codelist  codeList) {
		this.codeList = codeList;
	}

    /**
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }
        
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Attribute [id=").append(id)
				.append(", name=").append(name)
				.append(", codelist=").append(codeList).append("]\n");
		return builder.toString();
	}

}
