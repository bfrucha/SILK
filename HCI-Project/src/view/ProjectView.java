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
	public static final Color VALIDATE_ACTION_COLOR = new Color(25, 125, 25);
	
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
		
		annotations = new AnnotationsView(this);
		
		setBackground(BG_COLOR);
	}

	@Override
	public void setSize(int width, int height) {
		super.setSize(width, height);
		calque.setSize(width, height);
	}
	
	// get the canvas where will be drawn all user's commands
	public Canvas getCalque() {
		return calque;
	}
	
	
	// get canvas where project's annotations are written
	public AnnotationsView getAnnotationsView() {
		return annotations;
	}
	
	// set the interactions view (called by controller)
	public void setInteractionsView(InteractionsView intView) {
		interactions = intView;
	}
	
	// get canvas where project's interactions are written
	public InteractionsView getInteractionsView() {
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
}

