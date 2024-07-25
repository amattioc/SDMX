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

import it.bancaditalia.oss.sdmx.util.LocalizedText;

/**
 * This is a Java container for dataflow information.
 * 
 * @author Attilio Mattiocco
 * 
 */
public class Dataflow extends SDMXReference
{
	private final LocalizedText name; // the description
	private SDMXReference dsdIdentifier;

	public Dataflow(SDMXReference other, String name)
	{
		super(other);
		this.name = new LocalizedText(name);
	}
	
	public Dataflow(String id, String agency, String version, LocalizedText name)
	{
		super(id, agency, version);
		this.name = name;
	}

	/**
	 * @return The dataflow description.
	 */
	public String getDescription()
	{
		return name.getText();
	}

	/**
	 * @return The dataflow extended name in the form "agency/id/version ;
	 *         description".
	 */
	public String getName()
	{
		return getFullIdentifier() + " ; " + name;
	}

	/**
	 * @return The DSDIdentifier for this dataflow
	 */
	public SDMXReference getDsdIdentifier()
	{
		return dsdIdentifier;
	}

	/**
	 * @param dsdIdentifier
	 */
	public void setDsdIdentifier(SDMXReference dsdIdentifier)
	{
		this.dsdIdentifier = dsdIdentifier;
	}
	
	// TODO: use super
	@Override
	public String getFullIdentifier()
	{
		return getFullIdWithSep(',');
	}

	@Override
	public String toString()
	{
		return String.format("Dataflow [id=%s,\n name=%s,\n dsd=%s]", getFullIdentifier(), name, dsdIdentifier);
	}
}
