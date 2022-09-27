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

import static java.awt.Dialog.ModalityType.APPLICATION_MODAL;
import static java.awt.event.KeyEvent.VK_ESCAPE;
import static javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW;
import static javax.swing.KeyStroke.getKeyStroke;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

/**
 * @author Attilio Mattiocco
 *
 */
public class ProgressViewer<T> implements Serializable
{
	private static final long serialVersionUID = -7937931709790747236L;
	private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();
	
	private final SwingWorker<Void, Void> worker;
	private final JDialog dialog = new JDialog(); 
	
    public ProgressViewer(Component parent, Callable<T> task, Consumer<T> onComplete, Consumer<Throwable> onError)
    {
    	this(parent, new AtomicBoolean(false), task, onComplete, onError);
    }
    
    public ProgressViewer(Component parent, AtomicBoolean isCancelled, Callable<T> task, Consumer<T> onSuccess, Consumer<Throwable> onError)
    {
    	JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        dialog.getContentPane().add(panel, BorderLayout.CENTER);
        panel.setLayout(new BorderLayout(0, 0));
        
        JProgressBar progressBar = new JProgressBar();
        panel.add(progressBar);
        progressBar.setIndeterminate(true);
        
        Box horizontalBox = Box.createHorizontalBox();
        horizontalBox.setBorder(new EmptyBorder(10, 0, 0, 0));
        panel.add(horizontalBox, BorderLayout.SOUTH);
        
    	JButton btnCancel = new JButton("Cancel");
        horizontalBox.add(Box.createHorizontalGlue());
        horizontalBox.add(btnCancel);
        
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(300, 103);
        dialog.setLocationRelativeTo(parent);
        dialog.setModal(true);
        dialog.setResizable(false);
        dialog.setTitle("Executing query...");
    	
        Future<T> result = EXECUTOR_SERVICE.submit(() -> task.call());
        final ActionListener cancelListener = e -> {
        	result.cancel(true);
        	isCancelled.set(true);
        };

		btnCancel.addActionListener(cancelListener);
    	dialog.getRootPane().setDefaultButton(btnCancel);
        dialog.getRootPane().registerKeyboardAction(cancelListener, getKeyStroke(VK_ESCAPE, 0), WHEN_IN_FOCUSED_WINDOW);

        worker = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception
			{
				try
				{
					while (result == null || (!isCancelled.get() && !result.isDone()))
						Thread.sleep(100);

					T value = result.get();
					SwingUtilities.invokeLater(() -> dialog.dispose());
					if (!isCancelled.get())
						SwingUtilities.invokeLater(() -> onSuccess.accept(value));
					return null;
				}
				catch (ExecutionException | InterruptedException e)
				{
					Throwable t = e instanceof ExecutionException ? e.getCause() : e;
					isCancelled.set(true);
					SwingUtilities.invokeLater(() -> dialog.dispose());
					SwingUtilities.invokeLater(() -> onError.accept(t));
					return null;
				}
			}
		};
    }
    
    public void start()
    {
    	worker.execute();
    	dialog.setModalityType(APPLICATION_MODAL);
        dialog.setVisible(true);
    }
}
