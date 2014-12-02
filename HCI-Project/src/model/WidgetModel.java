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
	public final static int TEXT_FIELD = 1;
	public final static int PANEL = 2;
	
	private int type;
	
	public WidgetModel(int type, double x, double y, double width, double height) {
		super(x, y, width, height);
		
		this.type = type;
	}
	
	public WidgetModel(WidgetModel clone) {
		super(clone.getMinX(), clone.getMinY(), clone.getWidth(), clone.getHeight());
		
		this.type = clone.getType();
	}
	
	public int getType() {
		return type;
	}
	
	// need to use BUTTON and PANEL as bounds to enable reuse without rewrite
	public void setType(int type) {
		if(type < BUTTON) { this.type = PANEL; }
		else if(type > PANEL) { this.type = BUTTON; }
		else { this.type = type; }
	}
}
