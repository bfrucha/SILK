package view;

import java.awt.Color;

import model.WidgetModel;
import fr.lri.swingstates.canvas.CRectangle;

// represents a widget (rectangular shape for now)
public class WidgetView extends CRectangle {
	
	WidgetModel model;
	
	private Color fillPaint = new Color(200, 200, 200, 100);
	private Color outlinePaint = new Color(50, 50, 50, 100);
	
	public WidgetView(WidgetModel model) {
		super(model.getX(), model.getY(), model.getWidth(), model.getHeight());
		
		this.model = model;
		
		setFillPaint(fillPaint);
		setOutlinePaint(outlinePaint);
	}
	
}
