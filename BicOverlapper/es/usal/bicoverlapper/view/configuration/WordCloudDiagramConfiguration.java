package es.usal.bicoverlapper.view.configuration;

import java.awt.Dimension;

public class WordCloudDiagramConfiguration extends DiagramConfiguration {
	
	public int textIndex=0;
	public int sizeIndex=0;
	public int splitIndex=0;
	public int ontologyIndex=0;
	
	public WordCloudDiagramConfiguration(int id, String name, int posX,
			int posY, Dimension dim) {
		super(id, name, posX, posY, dim);
		// TODO Auto-generated constructor stub
	}
	public WordCloudDiagramConfiguration(DiagramConfiguration dc)
		{
		super(dc.getId(), dc.getTitle(), dc.getPosX(), dc.getPosY(), dc.getDim());
		}
}
