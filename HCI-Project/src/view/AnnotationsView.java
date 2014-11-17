package view;

import java.awt.Color;

import view.ProjectView;
import fr.lri.swingstates.canvas.CRectangle;
import fr.lri.swingstates.canvas.Canvas;

public class AnnotationsView extends Canvas {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	ProjectView parent;
	
	CRectangle background;
	
	public AnnotationsView(ProjectView parent) {
		this.parent = parent;
		
		setOpaque(false);
		
		background = newRectangle(0, 0, 1, 1);
		background.setOutlined(false);
		background.setFillPaint(new Color(100, 100, 100, 100));
	}
	
	public void display() {
		int width = parent.getWidth();
		int height = parent.getHeight();
		
		setSize(width, height);
		
		background.scaleTo(1);
		background.scaleBy(2*width, 2*height);
		
		parent.add(this);
		parent.setComponentZOrder(this, 0);
		parent.repaint();
		parent.validate();
	}
}
