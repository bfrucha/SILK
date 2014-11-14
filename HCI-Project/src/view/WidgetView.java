package view;

import model.WidgetModel;
import fr.lri.swingstates.canvas.CRectangle;

// represents a widget (rectangular shape for now)
public class WidgetView extends CRectangle {
	
	WidgetModel model;
	
	public WidgetView(WidgetModel model) {
		super(model.getX(), model.getY(), model.getWidth(), model.getHeight());
		
		this.model = model;
	}
	
}
