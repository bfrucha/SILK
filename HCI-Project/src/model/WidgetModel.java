package model;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import controller.SketchController;

// model of a widget that could be extended to create buttons, panels etc...
public class WidgetModel extends Rectangle2D.Double {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private SketchController open;
	private SketchController close;
	
	public WidgetModel(double x, double y, double width, double height) {
		super(x, y, width, height);
	}
	
	public WidgetModel(Rectangle2D bounds) {
		super(bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight());
	}
	
	public void setSketchToOpen(SketchController toOpen) {
		open = toOpen;
	}
	
	public SketchController getSketchToOpen() {
		return open;
	}
	
	public void setSketchToClose(SketchController toClose) {
		close = toClose;
	}
	
	public SketchController getSketchToClose() {
		return close;
	}
}
