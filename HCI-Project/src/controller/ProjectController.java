package controller;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import controller.ActionList.Action;
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
import view.MainScreen;
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
	private CStateMachine gestureMachine;
	
	private AnnotationsController annotationsController;
	
	private InteractionsController interactionsController;
	
	private ActionList actionList;
	
	private SketchController homeSketch;
	
	public ProjectController(ProjectModel model, ProjectView view) {
		this.model = model;
		this.view = view;
		
		// enable creation of sketches
		creationMachine = attachCreationSM();
		
		// enable suppression and main sketches
		gestureMachine = attachGestureSM();
		
		
		// enable annotations writing on top of the project
		annotationsController = new AnnotationsController(view.getAnnotationsView());
		
		// enable interactions creation between sketches
		InteractionsModel intModel = new InteractionsModel();
		InteractionsView intView = new InteractionsView(view, intModel);
		view.setInteractionsView(intView);
		interactionsController = new InteractionsController(this, intModel, intView);
		
		actionList = new ActionList();
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
	
	// add a new action to the list
	public void addAction(Object first, Object second, int mode) {
		actionList.addAction(first, second, mode);
	}
	
	// undo the last action depending on the mode
	public void undo() {
		switch(currentMode) {
		case INTERACTIONS_MODE: interactionsController.undo(); break;
		case ANNOTATIONS_MODE: annotationsController.undo(); break;
		default: 
			Action action = actionList.undo();
			if(action != null) { undo(action); }
		}
	}
	
	private void undo(Action action) {
		SketchController sketch = (SketchController) action.getFirst();
		
		// sketch to create/remove
		if(action.getSecond() == null) {
			if(action.getMode() == ActionList.CREATE) {
				removeSketch(sketch, false);
			} else {
				view.add(sketch.getView());
				
				model.addSketch(sketch);
				
				putOnTop(sketch);
				if(model.getSketches().size() == 1) { (homeSketch = model.getSketches().get(0)).setHome(true); }
			}
			view.update();
		} else {
			Object second = action.getSecond();
			if(second instanceof CPolyLine) {
				if(action.getMode() == ActionList.CREATE) { sketch.removeShape((CPolyLine) second); }
				else { sketch.addShape((CPolyLine) second); }
			}
			else {
				WidgetController widget = (WidgetController) second;
				
				if(action.getMode() == ActionList.CREATE) { 
					sketch.removeWidget(widget);
					interactionsController.removeInteraction(widget);
				}
				else { System.out.println("add widget");sketch.addWidget(widget); }
			}
		}
	}
	
	public void redo() {
		switch(currentMode) {
		case INTERACTIONS_MODE: interactionsController.redo(); break;
		case ANNOTATIONS_MODE: annotationsController.redo(); break;
		default:
			Action action = actionList.redo();
			if(action != null) { redo(action); }
		}
	}
	
	private void redo(Action action) {
		SketchController sketch = (SketchController) action.getFirst();
		
		if(action.getSecond() == null) {
			if(action.getMode() == ActionList.CREATE) {
				view.add(sketch.getView());
				
				model.addSketch(sketch);
				
				putOnTop(sketch);
				if(model.getSketches().size() == 1) { (homeSketch = model.getSketches().get(0)).setHome(true); }
			} else {
				view.remove(sketch.getView());
				
				model.removeSketch(sketch);
			}
		} else {
			Object second = action.getSecond();
			
			if(second instanceof CPolyLine) {
				if(action.getMode() == ActionList.CREATE) { sketch.addShape((CPolyLine) second); }
				else { sketch.removeShape((CPolyLine) second); }
			}
			else {
				WidgetController widget = (WidgetController) second;
				
				if(action.getMode() == ActionList.CREATE) { sketch.addWidget(widget); }
				else { 
					sketch.removeWidget(widget);
					interactionsController.removeInteraction(widget);
				}
			}
		}
		
		view.update();
	}
	
	// change current mode for this project
	public void changeMode(int mode) {
		currentMode = mode;
		
		if(currentMode == INTERACTIONS_MODE) {
			showWidgetsBounds();
		} else {
			hideWidgetsBounds();
		}
		
		if(currentMode == INTERACTIONS_MODE || currentMode == ANNOTATIONS_MODE) {
			suspendMachines();
		} else {
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
		String name = "Sketch" + ++sketchesNb;
		while(!isValidName(name)) { name = "-"+name; }
		
		SketchModel sketchModel = new SketchModel(name, tlc, new Dimension(1, 1));
		SketchView sketchView = new SketchView(sketchModel);
		SketchController sketchController = new SketchController(this, sketchModel, sketchView); 
		
		model.addSketch(sketchController);
		view.update();
		
		actionList.addAction(sketchController, null, ActionList.CREATE);
		
		if(model.getSketches().size() == 1) { homeSketch = sketchController; }
		else { sketchController.setHome(false); }
		
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
		
		actionList.addAction(cloneController, null, ActionList.CREATE);
		
		return cloneController;
	}
	
	public void removeSketch(SketchController sketch, boolean addAction) {
		if(addAction) { actionList.addAction(sketch, null, ActionList.DELETE); }
		
		model.removeSketch(sketch);
		view.remove(sketch.getView());
		
		for(WidgetController widget: sketch.getWidgets()) {
			interactionsController.removeInteraction(widget);
		}
		interactionsController.removeInteraction(sketch);
		
		if(sketch.equals(homeSketch)) { 
			homeSketch.setHome(false);
			
			if(model.getSketches().size() > 0) {
				homeSketch = model.getSketches().get(0);
				homeSketch.setHome(true); 
			}
			else { homeSketch = null; }
		}
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
		
		// we need to get the sketch above all
		// +1 because of interactions view
		int minimalOrder = sketches.size() + 1;
		
		for(int index = 0; index < sketches.size(); index++) {
			SketchController tmp = sketches.get(index); 
			SketchView tmpView = tmp.getView();
			
			int sketchZOrder = view.getComponentZOrder(tmpView);
			
			// need to change coordinates (intrinsec sketch coordinates)
			Point2D location = tmpView.getLocation();
			if(tmpView.contains((int) (p.getX() - location.getX()), (int) (p.getY() - location.getY()))
			&& minimalOrder > sketchZOrder) {
				sketch = tmp;
				minimalOrder = sketchZOrder;
			}
		}
		return sketch;
	}
	
	// returns the main sketch of the project
	public SketchController getHomeSketch() {
		return homeSketch;
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
	
	
	public void removeWidget(WidgetController widget) {
		interactionsController.removeInteraction(widget);
	}
	 
	public void showWidgetsBounds() {
		for(SketchController sketch: model.getSketches()) {
			sketch.showWidgetsBounds();
		}
	}
	
	public void hideWidgetsBounds() {
		for(SketchController sketch: model.getSketches()) {
			sketch.hideWidgetsBounds();
		}
	}
	
	// suspend all state machines except annotations and interactions ones
	public void suspendMachines() {
		creationMachine.suspend();
		gestureMachine.suspend();
		
		for(SketchController sketch: model.getSketches()) {
			sketch.suspendMachines();
		}
	}
	
	// resume all state machines 
	public void resumeMachines() {
		creationMachine.resume();
		gestureMachine.resume();
		
		for(SketchController sketch: model.getSketches()) {
			sketch.resumeMachines();
		}
	}
	
	/* STATMACHINES */
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
	public CStateMachine attachGestureSM() {
		return new CStateMachine(view) {
			
			Dollar1Classifier delete = Dollar1Classifier.newClassifier("classifier/delete.cl");
			Dollar1Classifier circle = Dollar1Classifier.newClassifier("classifier/circle.cl");
			Gesture gesture;
			
			Canvas calque;
			CPolyLine actionGhost;
			SketchController caught;
			
			State noAction = new State() {
				Transition begin = new Press(BUTTON3, ">> selection") {
					public void action() {
						Point2D mouse = getPoint();
						
						calque = view.getCalque();
						
						actionGhost = calque.newPolyLine(mouse);
						actionGhost.setFilled(false);
						actionGhost.setOutlinePaint(ProjectView.ACTION_COLOR);
						actionGhost.setStroke(new BasicStroke(2));
						
						gesture = new Gesture();
						gesture.addPoint(mouse.getX(), mouse.getY());
						
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
						
						gesture.addPoint(mouse.getX(), mouse.getY());
						
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
							if(delete.classify(gesture) != null) {
								actionGhost.setOutlinePaint(ProjectView.SUPPRESSION_ACTION_COLOR);
							} else if(circle.classify(gesture) != null) {
								actionGhost.setOutlinePaint(ProjectView.VALIDATE_ACTION_COLOR);
							}
						} 
					}
				};
				
				Transition validate = new Release(BUTTON3, ">> noAction") {
					public void action() {
						// delete the selected sketch
						if(caught != null) {
							if(delete.classify(gesture) != null) {
								removeSketch(caught, true);
							}
							else if(circle.classify(gesture) != null) {
								if(homeSketch != null) { homeSketch.setHome(false); }
								homeSketch = caught;
								homeSketch.setHome(true);
							}
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
