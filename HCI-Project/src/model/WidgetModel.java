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
	
	public final static int BUTTON = 0;
	public final static int PANEL = 1;
	
	private int type;
	
	public WidgetModel(int type, double x, double y, double width, double height) {
		super(x, y, width, height);
		
		this.type = type;
	}
	
	public WidgetModel(Rectangle2D bounds) {
		super(bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight());
	}
	
	public int getType() {
		return type;
	}
	
	public void setType(int type) {
		this.type = type;
	}
}
