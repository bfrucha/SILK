package controller;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;

import model.WidgetModel;
import view.WidgetView;

public class WidgetController {

	// sketch the widget belongs to
	SketchController sketch;
	
	WidgetModel model;
	WidgetView view;
	
	public WidgetController(SketchController sketch, WidgetModel model, WidgetView view) {
		this.sketch = sketch;
		
		this.model = model;
		this.view = view;
	}

	public void setSketch(SketchController sketch) {
		this.sketch = sketch;
	}
	
	public SketchController getSketch() {
		return sketch;
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
	
	public Point2D getAbsoluteLocation() {
		Point2D parentLocation = sketch.getLocation();
		
		return new Point2D.Double(model.getX() + parentLocation.getX(), model.getY() + parentLocation.getY());
	}
	
	public Point2D getAbsoluteCenter() {
		Point2D parentLocation = sketch.getLocation();
		
		return new Point2D.Double(model.getCenterX() + parentLocation.getX(), model.getCenterY() + parentLocation.getY());
	}
	
	public boolean contains(Point2D p) {
		return model.contains(p);
	}
}
