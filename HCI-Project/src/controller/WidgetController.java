package controller;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;

import fr.lri.swingstates.canvas.CPolyLine;
import fr.lri.swingstates.canvas.CRectangle;
import fr.lri.swingstates.canvas.CStateMachine;
import fr.lri.swingstates.canvas.CText;
import fr.lri.swingstates.canvas.transitions.EnterOnShape;
import fr.lri.swingstates.canvas.transitions.LeaveOnShape;
import fr.lri.swingstates.canvas.transitions.MoveOnShape;
import fr.lri.swingstates.sm.State;
import fr.lri.swingstates.sm.Transition;
import fr.lri.swingstates.sm.transitions.Drag;
import model.WidgetModel;
import view.SketchView;
import view.WidgetView;

public class WidgetController {

	// sketch the widget belongs to
	SketchController sketch;
	
	WidgetModel model;
	WidgetView view;
	
	CPolyLine ghost;
	
	CStateMachine popUpMachine;
	
	public WidgetController(SketchController sketch, WidgetModel model, WidgetView view, CPolyLine ghost) {
		this.sketch = sketch;
		
		this.model = model;
		this.view = view;
		
		this.ghost = ghost;
		
		update();
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
	
	public CPolyLine getGhost() {
		return ghost;
	}
	
	public void setGhost(CPolyLine ghost) {
		System.out.println(ghost);
		this.ghost = ghost;
	}
	
	public int getType() {
		return model.getType();
	}
	
	public void setType(int type) {
		model.setType(type);
		update();
	}
	
	public double getWidth() {
		return model.width;
	}
	
	public double getHeight() {
		return model.height;
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
	
	// check whether the widget contains a given point
	public boolean contains(Point2D p) {
		return model.contains(p);
	}
	
	// check whether the widget contains the given rectangle
	public boolean contains(CRectangle rectangle) {
		return model.contains(rectangle.getMinX(), rectangle.getMinY(), rectangle.getWidth(), rectangle.getHeight());
	}
	
	// check whether the widget is inside the given rectangle
	public boolean containedBy(CRectangle rectangle) {
		return rectangle.contains(model.getMinX(), model.getMinY(), model.getWidth(), model.getHeight()) != null;
	}
	
	public void update() {
		// update the color of the line representing the widget
		ghost.setStroke(new BasicStroke(2));
		
		switch(model.getType()) {
		case WidgetModel.BUTTON: ghost.setOutlinePaint(WidgetView.BUTTON_PAINT); break;
		case WidgetModel.TEXT_FIELD: ghost.setOutlinePaint(WidgetView.TF_PAINT); break;
		default: ghost.setOutlinePaint(WidgetView.PANEL_PAINT);
		}
		
		view.update();
	}
}
