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
package it.bancaditalia.oss.sdmx.helper;

import it.bancaditalia.oss.sdmx.util.Configuration;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BuildCommandActionListener implements ActionListener{
	Component parent = null;
	protected static Logger logger = Configuration.getSdmxLogger();

	public BuildCommandActionListener(Component parent) {
		super();
		this.parent = parent;
	}
	
	public void actionPerformed(ActionEvent ae) {
		try {
			BuildCommandContentFrame wnd;
			wnd = new BuildCommandContentFrame();
			wnd.setTitle("Commands in statistical tools");
		    wnd.setVisible( true );
		}
		catch (Exception ex) {
				logger.severe("Exception. Class: " + ex.getClass().getName() + " .Message: " + ex.getMessage());
				logger.log(Level.FINER, "", ex);
			}
    }
}
