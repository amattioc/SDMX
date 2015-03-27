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

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

/**
 * @author Attilio Mattiocco
 *
 */
public class ProgressViewer extends JDialog {

	private static final long serialVersionUID = -7937931709790747236L;
	private JProgressBar progressBar;

    public ProgressViewer(Component parent) {
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        this.add(BorderLayout.CENTER, progressBar);
        this.add(BorderLayout.NORTH, new JLabel("Executing query..."));
        this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        this.setSize(300, 75);
        this.setLocationRelativeTo(parent);
        this.setModal(true);
    }
}
