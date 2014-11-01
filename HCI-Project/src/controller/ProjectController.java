package controller;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import fr.lri.swingstates.canvas.CPolyLine;
import fr.lri.swingstates.canvas.CStateMachine;
import fr.lri.swingstates.canvas.Canvas;
import fr.lri.swingstates.sm.State;
import fr.lri.swingstates.sm.Transition;
import fr.lri.swingstates.sm.transitions.Drag;
import fr.lri.swingstates.sm.transitions.Press;
import fr.lri.swingstates.sm.transitions.Release;
import view.ProjectView;
import view.SketchView;
import model.ProjectModel;
import model.SketchModel;

public class ProjectController {

	private ProjectModel model;
	private ProjectView view;
	
	private CStateMachine creationMachine;
	private CStateMachine suppressionMachine;
	
	public ProjectController(ProjectModel model, ProjectView view) {
		this.model = model;
		this.view = view;
		
		// enable creation of sketches
		creationMachine = attachCreationSM();
		
		// enable suppression of sketches
		suppressionMachine = attachSuppressionSM();
	}
	
	public ProjectView getView() {
		return view;
	}
	
	// create a new Sketch with basic attributes
	public SketchController createSketch(Point tlc) {
		SketchModel sketchModel = new SketchModel("New Sketch", tlc, new Dimension(1, 1));
		SketchView sketchView = new SketchView(sketchModel);
		SketchController sketchController = new SketchController(this, sketchModel, sketchView); 
		
		model.addSketch(sketchController);
		view.update();
		
		return sketchController;
	}
	
	// duplicate from an existing sketch
	public SketchController duplicateSketch(SketchModel sketchModel) {
		SketchModel cloneModel = new SketchModel(sketchModel);
		SketchView cloneView = new SketchView(cloneModel);
		SketchController cloneController = new SketchController(this, cloneModel, cloneView);
		
		cloneModel.moveBy(40, 40);
		cloneView.update();
		
		model.addSketch(cloneController);
		view.update();
		
		return cloneController;
	}
	
	// put the sketch on top of the others
	public void putOnTop(SketchController sketch) {
		view.setComponentZOrder(sketch.getView(), 0);
		view.repaint();
	}
	
	
	// get sketch containing the given point
	public SketchView getSketchAt(Point p) {
		SketchView sketchView = null;
		
		ArrayList<SketchController> sketches = model.getSketches();
		int index = 0;
		while(sketchView == null && index < sketches.size()) {
			SketchView tmp = sketches.get(index++).getView();
			
			// need to change coordinates (intrinsec sketch coordinates)
			Point location = tmp.getLocation();
			if(tmp.contains((int) (p.getX() - location.getX()), (int) (p.getY() - location.getY()))) {
				sketchView = tmp;
			}
		}
		return sketchView;
	}
	
	
	public CStateMachine attachCreationSM() {
		return new CStateMachine(view) {
			
			SketchController sketch;
			Point2D initialPoint;
			
			// creation of the new sketch
			State creation = new State() {
				
				Transition press = new Press(BUTTON3, ">> dimension") {
					public void action() {
						initialPoint = getPoint();
						
						sketch = createSketch((Point) initialPoint);
						putOnTop(sketch);
					}
				};
			};
			
			// set the dimension of it
			State dimension = new State() {
				
				Transition drag = new Drag(">> dimension") {
					public void action() {
						sketch.setSize((int) (getPoint().getX() - initialPoint.getX()), (int) (getPoint().getY() - initialPoint.getY()));
					}
				};
				
				// end creation of the sketch
				Transition release = new Release(BUTTON3, ">> creation") {
					public void action() {
						// creation of the new sketch
						//newSketch = new Sketch(MainScreen.this, "New Sketch", (int) ghost.getWidth(), (int) ghost.getHeight());
						sketch.setSize((int) (getPoint().getX() - initialPoint.getX()), (int) (getPoint().getY() - initialPoint.getY()));
						
						// remove sketch if too small
						if(!sketch.hasValideDimension()) {
							model.removeSketch(sketch);
							view.remove(sketch.getView());
						}
						
						view.update();
					}
				};
				
			};
		};
	}
	
	// enable suppression of a sketch
	// draw a line onto a sketch's title to suppress it
	public CStateMachine attachSuppressionSM() {
		return new CStateMachine(view) {
			
			Canvas calque;
			CPolyLine actionGhost;
			SketchView caught;
			
			State noAction = new State() {
				Transition begin = new Press(BUTTON1, ">> selection") {
					public void action() {
						calque = view.getCalque();
						
						actionGhost = calque.newPolyLine(getPoint());
						actionGhost.setFilled(false);
						actionGhost.setOutlinePaint(ProjectView.ACTION_COLOR);
						actionGhost.setStroke(new BasicStroke(2));
						
						view.add(calque);
						view.setComponentZOrder(calque, 0);
						view.validate();
					}
				};
			};
			
			State selection = new State() {
				Transition over = new Drag() {
					public void action() {
						actionGhost.lineTo(getPoint());
						
						// if the line crosses a sketch's view, we catch it
						//caught = getSketchAt((Point) getPoint());
						//if(caught != null) {//&& caught.contains(getPoint()) == caught.getTitle()) {
							/* TODO */
						//}
						
						view.repaint();
					}
				};
				
				Transition validate = new Release(BUTTON1, ">> noAction") {
					public void action() {
						calque.removeShape(actionGhost);
						view.remove(calque);
						view.validate();
						view.repaint();
					}
				};
			};
		};
	}
}
