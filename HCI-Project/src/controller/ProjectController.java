package controller;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import fr.lri.swingstates.canvas.CPolyLine;
import fr.lri.swingstates.canvas.CRectangle;
import fr.lri.swingstates.canvas.CShape;
import fr.lri.swingstates.canvas.CStateMachine;
import fr.lri.swingstates.canvas.Canvas;
import fr.lri.swingstates.gestures.Gesture;
import fr.lri.swingstates.gestures.dollar1.Dollar1Classifier;
import fr.lri.swingstates.sm.State;
import fr.lri.swingstates.sm.Transition;
import fr.lri.swingstates.sm.transitions.Drag;
import fr.lri.swingstates.sm.transitions.KeyPress;
import fr.lri.swingstates.sm.transitions.Press;
import fr.lri.swingstates.sm.transitions.Release;
import view.InteractionsView;
import view.ProjectView;
import view.SketchView;
import model.InteractionsModel;
import model.ProjectModel;
import model.SketchModel;

public class ProjectController {

	public static final int WIDGETS_MODE = 0;
	public static final int INTERACTIONS_MODE = 1;
	public static final int ANNOTATIONS_MODE = 2;

	private int currentMode = WIDGETS_MODE;
	
	private int sketchesNb = 0;
	
	private ProjectModel model;
	private ProjectView view;
	
	private CStateMachine creationMachine;
	private CStateMachine suppressionMachine;
	
	private AnnotationsController annotationsController;
	
	private InteractionsController interactionsController;
	
	
	public ProjectController(ProjectModel model, ProjectView view) {
		this.model = model;
		this.view = view;

		keySM();
		
		// enable creation of sketches
		creationMachine = attachCreationSM();
		
		// enable suppression of sketches
		suppressionMachine = attachSuppressionSM();
		
		// enable annotations writing on top of the project
		annotationsController = new AnnotationsController(view.getAnnotationsView());
		
		// enable interactions creation between sketches
		InteractionsModel intModel = new InteractionsModel();
		InteractionsView intView = new InteractionsView(view, intModel);
		view.setInteractionsView(intView);
		interactionsController = new InteractionsController(this, intModel, intView);
	}
	
	public ProjectView getView() {
		return view;
	}
	
	public AnnotationsController getAnnotationsController() {
		return annotationsController;
	}
	
	public InteractionsController getInteractionsController() {
		return interactionsController;
	}
	
	public CStateMachine attachCreationSM() {
		return new CStateMachine(view) {
			
			SketchController sketch;
			Point2D initialPoint;
			
			// creation of the new sketch
			State creation = new State() {
				
				Transition press = new Press(BUTTON1, ">> dimension") {
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
				Transition release = new Release(BUTTON1, ">> creation") {
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
				Transition begin = new Press(BUTTON3, ">> selection") {
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
				
				Transition validate = new Release(BUTTON3, ">> noAction") {
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
	
	// returns project's sketches
	public ArrayList<SketchController> getSketches() {
		return model.getSketches();
	}
	
	// check if name is already used by a sketch
	public boolean isValidName(String name) {
		ArrayList<SketchController> sketches = model.getSketches();

		int index = 0;
		int size = sketches.size();
		while(index < size && !sketches.get(index).getName().equals(name)) { index++; }
		
		return index >= size;
	}
	
	// create a new Sketch with basic attributes
	public SketchController createSketch(Point tlc) {
		String name = "Sketch " + ++sketchesNb;
		while(!isValidName(name)) { name = "-"+name; }
		
		SketchModel sketchModel = new SketchModel(name, tlc, new Dimension(1, 1));
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
		
		// change the name of the new sketch
		String newName = "-"+cloneModel.getName();
		while(!isValidName(newName)) {
			newName = "-" + newName;
		}
		cloneModel.changeName(newName);
		
		// need to associate new controller to all sketch's widgets
		cloneController.linkWidgets();
		
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
	
	// returns widget that contains p point 
	public WidgetController getWidgetAt(Point2D p) {
		WidgetController widget;
		
		SketchController sketch = getSketchAt(p);
		
		if(sketch == null) { return null; }
		else {
			return sketch.getWidgetAt(p);
		}
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
	
	
	
	
	
	
	// draw lines on canvas
	private class DrawSM extends CStateMachine {
	 	Canvas view;
		CPolyLine annotation;
		
		
		public DrawSM(Canvas view) {
			super(view);
			this.view = view;
		}
		
		State wait = new State() {
			Transition press = new Press(BUTTON1, ">> drawing") {
				public void action() {
					annotation = view.newPolyLine(getPoint());
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
	}
	
	
	// delete shapes on canvas by gestures use
	private class DeleteSM extends CStateMachine {
		// classfifier and gesture which will help classify user's input
		Dollar1Classifier classifier = Dollar1Classifier.newClassifier("classifier/delete.cl");
		Gesture gesture = null;
		
		CPolyLine ghost = null;
		
		CShape caught = null;

		Canvas view;
		
		public DeleteSM(Canvas view) {
			super(view);
			this.view = view;
		}
		
		
		State init = new State() {
			Transition press = new Press(BUTTON3, ">> erasing") {
				public void action() {
					Point2D mouse = getPoint();
					
					gesture = new Gesture();
					gesture.addPoint(mouse.getX(), mouse.getY());
					
					ghost = view.newPolyLine(mouse);
					ghost.setFilled(false);
					ghost.setOutlinePaint(ProjectView.SUPPRESSION_ACTION_COLOR);
				}
			};
		};
		
		State erasing = new State() {
			Transition drawing = new Drag() {
				public void action() {
					Point2D mouse = getPoint();
					
					gesture.addPoint(mouse.getX(), mouse.getY());
					
					ghost.lineTo(mouse);
					
					// catch the shape below the gesture
					if(caught == null) {
						view.removeShape(ghost);
						CShape tmp = view.contains(mouse);
						if(!(tmp instanceof CRectangle)) {
							caught = tmp;
						}
						view.addShape(ghost);
					}
				}
			};
			
			Transition release = new Release(BUTTON3, ">> init") {
				public void action() {
					view.removeShape(ghost);
					
					if(classifier.classify(gesture) != null) {
						view.removeShape(caught);
						
						caught = null;
					}
				}
			};
		};
	}
}
