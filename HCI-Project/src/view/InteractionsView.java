package view;

import java.awt.Color;
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
	
	ProjectView parent;
	
	private InteractionsModel model;
	
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
		
		for(Entry<WidgetController, SketchController> interaction: model.getInteractions().entrySet()) {
			WidgetController widget = interaction.getKey();
			SketchController sketch = interaction.getValue();
			
			CSegment segment = newSegment(widget.getAbsoluteCenter(), sketch.getLocation());
			
			if(widget.getSketch() != sketch) {
				segment.setOutlinePaint(OPEN_PAINT);
			}
			else {
				segment.setOutlinePaint(CLOSE_PAINT);
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
