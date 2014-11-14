package model;

import java.awt.geom.Rectangle2D;

// model of a widget that could be extended to create button, panels etc...
public class WidgetModel extends Rectangle2D.Double {
	
	// here should be added the interaction models
	public WidgetModel(double x, double y, double width, double height) {
		super(x, y, width, height);
	}
}
