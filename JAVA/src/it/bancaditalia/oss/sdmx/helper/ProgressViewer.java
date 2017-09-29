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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

/**
 * @author Attilio Mattiocco
 *
 */
public class ProgressViewer extends JDialog {

	private static final long serialVersionUID = -7937931709790747236L;
	
	private static final ExecutorService executorService = Executors.newCachedThreadPool(); 
	
	private final JProgressBar progressBar;
	private final SwingWorker<Void, Void> worker;
	private final AtomicBoolean interrupted = new AtomicBoolean(false);
	private Future<?> task = null;

    public ProgressViewer(Component parent, final Runnable executor) {
    	this.worker = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception 
			{
				try {
					while (!isVisible() && !interrupted.get())
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							// ignore
						}

					if (!interrupted.get())
					{
						task = executorService.submit(executor);
						task.get();
					}
				} finally {
					setVisible(false);
					dispose();
				}
				
				return null;
			}
    	};
        
        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        getContentPane().add(panel, BorderLayout.CENTER);
        panel.setLayout(new BorderLayout(0, 0));
        
        progressBar = new JProgressBar();
        panel.add(progressBar);
        progressBar.setIndeterminate(true);
        
        Box horizontalBox = Box.createHorizontalBox();
        horizontalBox.setBorder(new EmptyBorder(10, 0, 0, 0));
        panel.add(horizontalBox, BorderLayout.SOUTH);
        
        Component horizontalGlue = Box.createHorizontalGlue();
        horizontalBox.add(horizontalGlue);
        
        ActionListener cancelListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				while (task == null && !interrupted.get())
					try {
						Thread.sleep(100);
					} catch (InterruptedException e1) {
						// ignore
					}
				
				if (task != null && !interrupted.get())
				{
					task.cancel(true);
					interrupted.set(true);
				}
			}
		}; 
        
        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(cancelListener);
        horizontalBox.add(btnCancel);
        
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.setSize(300, 103);
        this.setLocationRelativeTo(parent);
        this.setModal(true);
    	this.setResizable(false);
    	this.setTitle("Executing query...");
    	
    	JRootPane root = getRootPane();
    	root.setDefaultButton(btnCancel);
        root.registerKeyboardAction(cancelListener, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

		worker.execute();
    }
}
