package view;

import java.awt.Color;
import java.util.ArrayList;

import controller.ProjectController;
import controller.SketchController;
import model.ProjectModel;
import fr.lri.swingstates.canvas.CRectangle;
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

	private InteractionsView interactions;
	private AnnotationsView annotations;
	
	public ProjectView(ProjectModel model, int width, int height) {
		super(width, height);
		
		this.model = model;
		
		setLayout(null);
		
		calque = new Canvas();
		calque.setSize(width, height);
		calque.setOpaque(false);
		
		interactions = new InteractionsView(this);
		annotations = new AnnotationsView(this);
		
		setBackground(BG_COLOR);
	}

	
	// get the canvas where will be drawn all user's commands
	public Canvas getCalque() {
		return calque;
	}
	
	
	// get canvas where project's annotations are written
	public Canvas getAnnotationsView() {
		return annotations;
	}
	
	// get canvas where project's interactions are written
	public Canvas getInteractionsView() {
		return interactions;
	}
	

	public void changeMode(int mode) {
		remove(annotations);
		remove(interactions);
		
		if(mode == ProjectController.ANNOTATIONS_MODE) {
			annotations.display();
		} else if(mode == ProjectController.INTERACTIONS_MODE) {
			interactions.display();
		} else {
			// something to add ?
		}
		
		repaint();
		validate();
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


	/* PRIVATE CLASSES */
	
	// add transparent background
	private class AnnotationsView extends Canvas {
		
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
			background.setFillPaint(new Color(BG_COLOR.getRed(), BG_COLOR.getGreen(), BG_COLOR.getBlue(), 100));
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

	
	// same class as above for now
	private class InteractionsView extends Canvas {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		ProjectView parent;
		
		CRectangle background;
		
		public InteractionsView(ProjectView parent) {
			this.parent = parent;
			
			setOpaque(false);
			
			background = newRectangle(0, 0, 1, 1);
			background.setOutlined(false);
			background.setFillPaint(new Color(255, 255, 255, 100));
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
	
}

