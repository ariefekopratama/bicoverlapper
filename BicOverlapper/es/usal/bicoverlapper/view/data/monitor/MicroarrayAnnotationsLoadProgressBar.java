package es.usal.bicoverlapper.view.data.monitor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;
import javax.swing.JProgressBar;

import es.usal.bicoverlapper.view.main.BicOverlapperWindow;

public class MicroarrayAnnotationsLoadProgressBar implements PropertyChangeListener {

private JProgressBar progressBar;

	public MicroarrayAnnotationsLoadProgressBar(BicOverlapperWindow framePrincipal){
		progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setPreferredSize(new Dimension(70,10));
		progressBar.setVisible(false);
		progressBar.setToolTipText("Loading annotations...");
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(progressBar, BorderLayout.EAST);
		//framePrincipal.add(panel, BorderLayout.PAGE_END);
		
		framePrincipal.getJMenuBar().add(panel);
	}
	
	/**
	 * Método que notifica que la clase ya es oyente de alguien
	 */
	public void isListener(){
		if(!progressBar.isVisible()){
			progressBar.setVisible(true);
		}		
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		//si la propiedad progress cambia de valor, se actualiza la barra de progreso
        if ("progress".equals(evt.getPropertyName())) {
    		progressBar.setValue((Integer)evt.getNewValue());
        }
	}
}