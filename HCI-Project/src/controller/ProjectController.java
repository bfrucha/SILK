package controller;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import exception.InvalidActionException;
import fr.lri.swingstates.canvas.CPolyLine;
import fr.lri.swingstates.canvas.CStateMachine;
import fr.lri.swingstates.canvas.Canvas;
import fr.lri.swingstates.sm.State;
import fr.lri.swingstates.sm.Transition;
import fr.lri.swingstates.sm.transitions.Drag;
import fr.lri.swingstates.sm.transitions.KeyPress;
import fr.lri.swingstates.sm.transitions.Press;
import fr.lri.swingstates.sm.transitions.Release;
import view.ProjectView;
import view.SketchView;
import model.ProjectModel;
import model.SketchModel;

public class ProjectController {

	public static final int WIDGETS_MODE = 0;
	public static final int INTERACTIONS_MODE = 1;
	public static final int ANNOTATIONS_MODE = 2;
	public static final int DRAW = 0;
	public static final int ERASE = 1;
	private int currentMode = WIDGETS_MODE;
	private int[] actions = {DRAW, DRAW, DRAW};
	
	private ProjectModel model;
	private ProjectView view;
	
	private CStateMachine creationMachine;
	private CStateMachine suppressionMachine;
	private CStateMachine annotationsMachine;
	private CStateMachine interactionsMachine;
	
	public ProjectController(ProjectModel model, ProjectView view) {
		this.model = model;
		this.view = view;

		keySM();
		
		// enable creation of sketches
		creationMachine = attachCreationSM();
		
		// enable suppression of sketches
		suppressionMachine = attachSuppressionSM();
		
		// enable annotations writing on top of the project
		annotationsMachine = initAnnotationsMode();
		
		// enable interactions craetion between sketches
		interactionsMachine = initInteractionsMode();
		
	}
	
	public ProjectView getView() {
		return view;
	}
	
	public void keySM() {
		new CStateMachine(view) {
			State listen = new State() {
				Transition key = new KeyPress() {
					public void action() {
						switch(getChar()) {
						case 'a': case 'A': changeMode(ANNOTATIONS_MODE); break;
						case 'i': case 'I': changeMode(INTERACTIONS_MODE); break;
						case 'w': case'W': changeMode(WIDGETS_MODE); break;
						}
					}
				};
			};
		};
	}
	
	// enable annotations writing on top of the project's view
	public CStateMachine initAnnotationsMode() {
		return new CStateMachine(view.getAnnotationsView()) {
			
			Canvas canvas = view.getAnnotationsView();
			CPolyLine annotation;
			
			State wait = new State() {
				Transition press = new Press(BUTTON1, ">> drawing") {
					public void action() {
						annotation = canvas.newPolyLine(getPoint());
						annotation.setFilled(false);
						annotation.setStroke(new BasicStroke(2));
					}
				};
			};
			
			State drawing = new State() {
				Transition drag = new Drag() {
					public void action() {
						annotation.lineTo(getPoint());
					}
				};
				
				Transition release = new Release(">> wait") {
					public void action() {
						annotation.lineTo(getPoint());
					}
				};
			};
		};
	}
	
	// enlable interactions creation between sketches
	public CStateMachine initInteractionsMode() {
		return new CStateMachine(view.getInteractionsView()) {
			
			Canvas canvas = view.getInteractionsView();
			CPolyLine annotation;
			
			State wait = new State() {
				Transition press = new Press(BUTTON1, ">> drawing") {
					public void action() {
						annotation = canvas.newPolyLine(getPoint());
						annotation.setFilled(false);
						annotation.setStroke(new BasicStroke(2));
					}
				};
			};
			
			State drawing = new State() {
				Transition drag = new Drag() {
					public void action() {
						annotation.lineTo(getPoint());
					}
				};
				
				Transition release = new Release(">> wait") {
					public void action() {
						annotation.lineTo(getPoint());
					}
				};
			};
		};
	}
	
	// change current mode for this project
	public void changeMode(int mode) {
		currentMode = mode;
		
		if(currentMode == INTERACTIONS_MODE || currentMode == ANNOTATIONS_MODE) {
			System.out.println("Change to Annotations/Interactions mode");
			suspendMachines();
		} else {
			System.out.println("Change to Widgets mode");
			resumeMachines();
		}
		
		view.changeMode(currentMode);
	}	
	
	
	// change the action (DRAW, ERASE) depending on the current mode
	public void changeAction(int action) throws InvalidActionException {
		if(action != DRAW && action != ERASE) { throw new InvalidActionException(); } 
		
		actions[currentMode] = action;
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
	public SketchController getSketchAt(Point2D p) {
		SketchController sketch= null;
		
		ArrayList<SketchController> sketches = model.getSketches();
		int index = 0;
		while(sketch == null && index < sketches.size()) {
			SketchController tmp = sketches.get(index++); 
			SketchView tmpView = tmp.getView();
			
			// need to change coordinates (intrinsec sketch coordinates)
			Point2D location = tmpView.getLocation();
			if(tmpView.contains((int) (p.getX() - location.getX()), (int) (p.getY() - location.getY()))) {
				sketch = tmp;
			}
		}
		return sketch;
	}
	
	// suspend all state machines except annotations and interactions ones
	public void suspendMachines() {
		creationMachine.suspend();
		suppressionMachine.suspend();
		
		for(SketchController sketch: model.getSketches()) {
			sketch.suspendMachines();
		}
	}
	
	// resume all state machines 
	public void resumeMachines() {
		creationMachine.resume();
		suppressionMachine.resume();
		
		for(SketchController sketch: model.getSketches()) {
			sketch.resumeMachines();
		}
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
			SketchController caught;
			
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
					}
				};
			};
			
			State selection = new State() {
				Transition over = new Drag() {
					public void action() {
						Point2D mouse = getPoint();
						
						actionGhost.lineTo(mouse);
						
						SketchController tmp = getSketchAt(mouse);
						
						// if the line crosses a sketch's view, we catch it
						if(caught == null) {
							caught = tmp;
							
							if(caught != null && !caught.isSelected(mouse)) {
								caught = null;
							}
						}
						
						// on a sketch => no suppression
						if(tmp != null) {
							actionGhost.setOutlinePaint(ProjectView.ACTION_COLOR);
						} else if(caught != null) {
							actionGhost.setOutlinePaint(ProjectView.SUPPRESSION_ACTION_COLOR);
						}
					}
				};
				
				Transition validate = new Release(BUTTON1, ">> noAction") {
					public void action() {
						// delete the selected sketch
						if(caught != null && actionGhost.getOutlinePaint() == ProjectView.SUPPRESSION_ACTION_COLOR) {
							model.removeSketch(caught);
							view.remove(caught.getView());
						}
						caught = null;
						calque.removeShape(actionGhost);
						view.remove(calque);
						view.repaint();
					}
				};
			};
		};
	}
}
