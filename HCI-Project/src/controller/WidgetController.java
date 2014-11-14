package controller;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import model.WidgetModel;
import view.WidgetView;

public class WidgetController {

	WidgetModel model;
	WidgetView view;
	
	public WidgetController(WidgetModel model, WidgetView view) {
		this.model = model;
		this.view = view;
	}

	public WidgetModel getModel() {
		return model;
	}
	
	public WidgetView getView() {
		return view;
	}
	
	public Rectangle2D getBounds() {
		return model;
	}
	
	public Point2D getLocation() {
		return new Point2D.Double(model.getX(), model.getY());
	}
}
