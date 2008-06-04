package es.usal.bicoverlapper.utils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Random;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.Timer;

import es.usal.bicoverlapper.kernel.managers.FileMenuManager;

public class ProgressPanel extends JPanel implements PropertyChangeListener, Runnable{
	 JLabel text;
	 JProgressBar progressBar;
	 private Task task;
	 FileMenuManager reader;

	 
	 class Task extends SwingWorker<Void, Void> {
	        /*
	         * Main task. Executed in background thread.
	         */
	        @Override
	        public Void doInBackground() {
	        	int progress=0;
	        	setProgress(progress);
	        	//reader.leerMicroarray();
	            /*Random random = new Random();
	            int progress = 0;
	            //Initialize progress property.
	            setProgress(0);
	            //Sleep for at least one second to simulate "startup".
	            try {
	                Thread.sleep(1000 + random.nextInt(2000));
	            } catch (InterruptedException ignore) {}
	            while (progress < 100) {
	                //Sleep for up to one second.
	                try {
	                    Thread.sleep(random.nextInt(1000));
	                } catch (InterruptedException ignore) {}
	                //Make random progress.
	                progress += random.nextInt(10);
	                setProgress(Math.min(progress, 100));
	            }*/
	            return null;
	        }
	 }
	        
	 public ProgressPanel(String title, FileMenuManager fmm)
	 	{
		super();
		reader=fmm;
		text=new JLabel("Reading Microarray data Matrix");
		progressBar=new JProgressBar();
		this.add(text);
		this.add(progressBar);
		
		//Create and set up the window.
	    JFrame frame = new JFrame(title);
	    frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

	    //Create and set up the content pane.
	    JComponent newContentPane = this;
	     newContentPane.setOpaque(true); //content panes must be opaque
	    frame.setContentPane(newContentPane);

	    //Display the window.
	    //frame.add(license);
	    frame.pack();
	    frame.setSize(320,100);
	    frame.setLocation(100,100);
	    frame.setVisible(true);
	    
	    //Equivalente a pulsar start
	    progressBar.setIndeterminate(true);
        //startButton.setEnabled(false);
        //Instances of javax.swing.SwingWorker are not reusuable, so
        //we create new instances as needed.
        task = new Task();
        task.addPropertyChangeListener(this);
        task.execute();
		}
	 
	  /**
	     * Invoked when task's progress property changes.
	     */
	    public void propertyChange(PropertyChangeEvent evt) {
	        if ("progress" == evt.getPropertyName()) {
	            int progress = (Integer) evt.getNewValue();
	            progressBar.setIndeterminate(false);
	            progressBar.setValue(progress);
	            //text.append(String.format(
	              //          "Completed %d%% of task.\n", progress));
	        }
	    }
	    
	    public void run()
		{
		}
}
