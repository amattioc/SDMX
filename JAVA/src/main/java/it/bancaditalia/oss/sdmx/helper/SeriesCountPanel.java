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

import javax.swing.*;
import java.util.ResourceBundle;

class SeriesCountPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final JLabel seriesCountLabel;
    private final JTextField seriesCount;
    private final JLabel obsCountLabel;
    private final JTextField obsCount;

    public SeriesCountPanel(int seriesCount, int obsCount)
    {
        this.seriesCountLabel = new JLabel();
        this.seriesCount = new JTextField(Integer.toString(seriesCount));
        this.seriesCount.setEditable(false);
        this.seriesCount.setMinimumSize(new java.awt.Dimension(100, 33));
        this.obsCountLabel = new JLabel();
        this.obsCount = new JTextField(Integer.toString(obsCount));
        this.obsCount.setEditable(false);
        this.obsCount.setMinimumSize(new java.awt.Dimension(100, 33));
        add(seriesCountLabel);
        add(this.seriesCount);
        add(obsCountLabel);
        add(this.obsCount);
    }

    public void updateCounts(int seriesCount, int obsCount)
    {
        this.seriesCount.setText(Integer.toString(seriesCount));
        this.seriesCount.setCaretPosition(0);
        this.obsCount.setText(Integer.toString(obsCount));
        this.obsCount.setCaretPosition(0);
        if (seriesCount <= 0)
            hideSeriesCount();
        else
            showSeriesCount();

        if (obsCount <= 0)
            hideObsCount();
        else
            showObsCount();
    }

    public void updateBundle(ResourceBundle b)
    {
        this.seriesCountLabel.setText(b.getString("SDMXHelper.105"));
        this.obsCountLabel.setText(b.getString("SDMXHelper.106"));
    }

    public void hidePanel()
    {
        this.seriesCount.setVisible(false);
        this.seriesCountLabel.setVisible(false);
        this.obsCount.setVisible(false);
        this.obsCountLabel.setVisible(false);
        this.setVisible(false);
    }

    public void showPanel() {
        this.seriesCount.setVisible(true);
        this.seriesCountLabel.setVisible(true);
        this.obsCount.setVisible(true);
        this.obsCountLabel.setVisible(true);
        this.setVisible(true);
    }

    public void hideSeriesCount() {
        this.seriesCountLabel.setVisible(false);
        this.seriesCount.setVisible(false);
    }

    public void showSeriesCount() {
        this.seriesCountLabel.setVisible(true);
        this.seriesCount.setVisible(true);
    }

    public void hideObsCount() {
        this.obsCountLabel.setVisible(false);
        this.obsCount.setVisible(false);
    }

    public void showObsCount() {
        this.obsCountLabel.setVisible(true);
        this.obsCount.setVisible(true);
    }

}
