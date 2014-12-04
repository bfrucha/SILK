package view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import model.InteractionsModel;
import controller.SketchController;
import controller.WidgetController;
import fr.lri.swingstates.canvas.CRectangle;
import fr.lri.swingstates.canvas.CSegment;
import fr.lri.swingstates.canvas.Canvas;

public class InteractionsView extends Canvas {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static public final Color OPEN_PAINT = Color.orange;
	static public final Color CLOSE_PAINT = Color.magenta;
	static public final Color LINK_PAINT = new Color(0xcc, 0x10, 0x10);
	
	ProjectView parent;
	
	private InteractionsModel model;
	
	// attributes of interactions segments
	protected static final float[] dash_phase = {5, 3};
	protected static int dash_offset = 5; 
	
	public InteractionsView(ProjectView parent, InteractionsModel model) {
		this.parent = parent;
		this.model = model;
		
		setOpaque(false);
	}
	
	public void update() {
		int width = parent.getWidth();
		int height = parent.getHeight();
		
		setSize(width, height);
		
		removeAllShapes();
		
		CRectangle background = newRectangle(0, 0, width, height);
		background.setOutlined(false);
		background.setFillPaint(new Color(255, 255, 255, 100));
		background.setPickable(false);
		
		for(Entry<WidgetController, Object> interaction: model.getInteractions().entrySet()) {
			WidgetController widget = interaction.getKey();
			Point2D widgetCenter = widget.getAbsoluteCenter();
			
			Object controller = interaction.getValue();
			
			CSegment segment = newSegment(widgetCenter, widgetCenter);
			segment.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, dash_phase, dash_offset));
			
			// widget => sketch
			if(controller instanceof SketchController) {
				segment.setPoints(widgetCenter, ((SketchController) controller).getLocation());
			
				if(widget.getSketch() != controller) {
					segment.setOutlinePaint(OPEN_PAINT);
				}
				else {
					segment.setOutlinePaint(CLOSE_PAINT);
				}
			} 
			// widget => widget
			else {
				segment.setPoints(widgetCenter, ((WidgetController) controller).getAbsoluteCenter());
				
				segment.setOutlinePaint(LINK_PAINT);
			}
			
			model.addSegment(segment, widget);
		}
	}
 	
	// show the view on top of the project's view
	public void display() {
		update();
		
		parent.add(this);
		parent.setComponentZOrder(this, 0);
		parent.repaint();
		parent.validate();
	}
}
