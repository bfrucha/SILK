package view;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;

import javax.swing.JPanel;

import controller.SketchController;
import model.ProjectModel;
import fr.lri.swingstates.canvas.Canvas;

public class ProjectView extends Canvas {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final Color BG_COLOR = Color.gray;
	
	public static final Color ACTION_COLOR = Color.blue;
	public static final Color SUPPRESSION_ACTION_COLOR = Color.red;
	
	private ProjectModel model;
	
	private Canvas calque;
	
	public ProjectView(ProjectModel model, int width, int height) {
		super(width, height);
		
		this.model = model;
		
		setLayout(null);
		
		calque = new Canvas();
		calque.setSize(width, height);
		calque.setBackground(new Color(0, 0, 0, 0));
		calque.setOpaque(false);
		
		setBackground(BG_COLOR);
	}
	
	
	// get the canvas where will be drawn all user's commands
	public Canvas getCalque() {
		return calque;
	}
	
	
	// update the view on relation with model
	public void update() {
		ArrayList<SketchController> sketches = model.getSketches();
		
		// add sketches that are not already added
		for(SketchController sketch: sketches) {
			SketchView view = sketch.getView();
			if(this != view.getParent()) {
				add(view);
			} 
		}
		
		repaint();
	}
	
}
