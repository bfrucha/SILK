package controller;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import javax.swing.JTextField;

import fr.lri.swingstates.canvas.CPolyLine;
import fr.lri.swingstates.canvas.CRectangle;
import fr.lri.swingstates.canvas.CShape;
import fr.lri.swingstates.canvas.CStateMachine;
import fr.lri.swingstates.canvas.CText;
import fr.lri.swingstates.canvas.Canvas;
import fr.lri.swingstates.canvas.transitions.EnterOnShape;
import fr.lri.swingstates.canvas.transitions.LeaveOnShape;
import fr.lri.swingstates.canvas.transitions.MoveOnShape;
import fr.lri.swingstates.canvas.transitions.PressOnShape;
import fr.lri.swingstates.canvas.transitions.ReleaseOnShape;
import fr.lri.swingstates.gestures.Gesture;
import fr.lri.swingstates.gestures.dollar1.Dollar1Classifier;
import fr.lri.swingstates.sm.State;
import fr.lri.swingstates.sm.Transition;
import fr.lri.swingstates.sm.transitions.Click;
import fr.lri.swingstates.sm.transitions.Drag;
import fr.lri.swingstates.sm.transitions.Press;
import fr.lri.swingstates.sm.transitions.Release;
import view.ProjectView;
import view.SketchView;
import view.WidgetView;
import model.SketchModel;
import model.WidgetModel;

public class SketchController {

	private ProjectController project;
	
	private SketchModel model;
	private SketchView view;
	
	private Dimension minimalDimension = new Dimension(100, 100);
	
	private ArrayList<CStateMachine> machines;
	private CStateMachine drawMachine;
	private CStateMachine changeWidgetMachine;
	private CStateMachine popUpMachine;
	private CStateMachine deleteMachine;
	private CStateMachine moveMachine;
	private CStateMachine nameMachine;
	private CStateMachine duplicateMachine;
	private CStateMachine resizeMachine;

	// recognize rectangle widgets
	private static Dollar1Classifier widgetClassifier = Dollar1Classifier.newClassifier("classifier/square.cl");
	
	// ease the suppression of widgets on a sketch
	private HashMap<CShape, WidgetController> shapeToWidget;
	
	
	public SketchController(ProjectController parent, SketchModel model, SketchView view) {
		this.project = parent;

		this.model = model;
		this.view = view;
		
		// set border to red => not valide dimension
		if(!hasValideDimension()) {
			view.setBorder(SketchView.INVALIDE_BORDER);
		}
		
		machines = new ArrayList<CStateMachine>();
		
		// enable drawing on the Sketch
		machines.add(drawMachine = attachDrawSM());

		// enable widget's type changing
		machines.add(changeWidgetMachine = attachWidgetMachine());
		
		// enable display widget's type
		machines.add(popUpMachine = attachPopUpSM());
		
		// enable erasing shapes
		machines.add(deleteMachine = attachDeleteSM());
		
		// enable Sketch's movement
		machines.add(moveMachine = attachMoveSM(view.getTitleBar()));
		
		// enable direct editing of the Sketch's name
		machines.add(nameMachine = attachNameSM(view.getTitle()));
		
		// enable Sketches duplication
		machines.add(duplicateMachine = attachDuplicateSM(view.getCopyButton()));
		
		// enablt Sketches resizing
		machines.add(resizeMachine = attachResizeSM(view.getResizeButton()));
		
		
		// TODO pb when copying a sketch
		shapeToWidget = new HashMap<CShape, WidgetController>();
	}
	
	public String getName() {
		return model.getName();
	}
	
	// access to a sketch's view from its controller
	public SketchView getView() {
		return view;
	}
	
	// makes this sketch the main one or not
	public void setHome(boolean isHome) {
		view.setHome(isHome);
	}
	
	public LinkedList<WidgetController> getWidgets() {
		return model.getWidgets();
	}
	
	// change the relative point of the sketch
	public void setLocation(Point tlc) {
		model.move(tlc);
		putOnTop();
		view.update();
	}
	
	// get sketch's location
	public Point2D getLocation() {
		return model.getLocation();
	}
	
	public Dimension getSize() {
		return model.getSize();
	}
	
	// change the size of the sketch
	public void setSize(int width, int height) {
		setSize(new Dimension(width, height));
	}
	
	// change size of the sketch and its border according to minimal dimension accepted
	public void setSize(Dimension dimension) {
		model.setSize(dimension);	
		view.update();
		if(hasValideDimension()) {
			view.setBorder(SketchView.VALIDE_BORDER);
		} else {
			view.setBorder(SketchView.INVALIDE_BORDER);
		}
	}
	
	// verifies the sketch's dimension
	public boolean hasValideDimension() {
		Dimension current = model.getSize();
		return minimalDimension.width <= current.width && minimalDimension.height <= current.height;
	}
	
	// ask project controller to put this sketch on top of the others
	public void putOnTop() {
		project.putOnTop(this);
	}
	

	// tell whether this sketch is selected by an user's action
	// check if the point is on the title or the title bar
	public boolean isSelected(Point2D point) {
		Point location = view.getLocation();
		
		point.setLocation(point.getX() - location.getX(), point.getY() - location.getY());
		
		return view.getTitleBar().contains(point) != null;
	}
	
	// pause all machines this sketch is attached to
	public void suspendMachines() {
		for(CStateMachine machine: machines) {
			machine.suspend();
		}
	}
	
	
	// pause all machines this sketch is attached to
	public void resumeMachines() {
		for(CStateMachine machine: machines) {
			machine.resume();		
		}
	}
	
	/* UNDO/REDO methods */
	
	public void addShape(CPolyLine line) {
		model.addShape(line);
		view.addShape(line);
		view.update();
	}
	
	public void removeShape(CPolyLine line) {
		model.removeShape(line);
		view.removeShape(line);
		view.update();
	}
	
	// add a widget. Called by the project controller (undo/redo)
	public void addWidget(WidgetController widget) {
		model.addWidget(widget);
		model.addShape(widget.getGhost());
		
		view.addShape(widget.getGhost());
		view.addShape(widget.getView());
		
		shapeToWidget.put(widget.getGhost(), widget);
	}
	
	// remove a widget. Called by project controller (undo/redo)
	public void removeWidget(WidgetController widget) {
		model.removeWidget(widget);
		model.removeShape(widget.getGhost());
		
		view.removeShape(widget.getGhost());
		view.removeShape(widget.getView());
		
		shapeToWidget.remove(widget.getGhost());
	}
	
	/* END of UNDO/REDO methods */
	
	// creates a widget from a rectangle
	public WidgetController createWidget(CPolyLine ghost, int type) {
		CRectangle bounds = ghost.getBoundingBox();
		
		WidgetModel model = new WidgetModel(type, bounds.getMinX(), bounds.getMinY(),
											bounds.getWidth(), bounds.getHeight());
		WidgetView view = new WidgetView(model);
		
		WidgetController controller = new WidgetController(this, model, view, ghost);
		
		project.addAction(this, controller, ActionList.CREATE);
		
		return controller;
	}
	
	// link copied widgets to the new sketch and update shapeToWidget
	public void linkWidgets() {
		shapeToWidget = new HashMap<CShape, WidgetController>();
		
		for(WidgetController widget: model.getWidgets()) {
			widget.setSketch(this);
			
			view.addShape(widget.getView());
			
			shapeToWidget.put(widget.getGhost(), widget);
		}
	}
	
	public WidgetController getWidgetAt(Point2D p) {
		WidgetController widget = null;
		
		Point2D location = model.getLocation();
		Point2D relativePoint = new Point2D.Double(p.getX() - location.getX(), p.getY() - location.getY());
		
		LinkedList<WidgetController> widgets = model.getWidgets();
		
		// from end to start to respect widgets order
		int index = widgets.size() - 1;
		while(widget == null && index >= 0) {
			WidgetController tmp = widgets.get(index--);
			
			if(tmp.contains(relativePoint)) {
				widget = tmp;
			}
		}
		
		return widget;
	}
	
	// return the first widget contained by the rectangle
	public WidgetController getWidgetContainedIn(CRectangle box) {
		WidgetController widget = null;
		
		LinkedList<WidgetController> widgets = model.getWidgets();
		
		// from end to start to respect widgets order
		int index = widgets.size() - 1;
		while(widget == null && index >= 0) {
			WidgetController tmp = widgets.get(index--);
			
			if(tmp.containedBy(box)) {
				widget = tmp;
			}
		}
		
		return widget;
	}
	
	
	// return the first widget containing the rectangle
	public WidgetController getWidgetContaining(CRectangle box) {
		WidgetController widget = null;
		
		LinkedList<WidgetController> widgets = model.getWidgets();
		
		// from end to start to respect widgets order
		int index = widgets.size() - 1;
		while(widget == null && index >= 0) {
			WidgetController tmp = widgets.get(index--);
			
			// second getBounds() to cast to Rectangle
			if(tmp.contains(box)) {
				widget = tmp;
			}
		}
		
		return widget;
	}
	
	// show widgets' bounds
	public void showWidgetsBounds() {
		for(WidgetController widget: model.getWidgets()) {
			widget.getView().setDrawable(true);
		}
	}
	
	// hides widgets' bounds
	public void hideWidgetsBounds() {
		for(WidgetController widget: model.getWidgets()) {
			widget.getView().setDrawable(false);
		}
	}
	
	// propose a type for the new widget
	public int typeChoice(Dimension sketchSize, CRectangle bounds) {
		int type = WidgetModel.BUTTON;
		
		// half of the sketch => PANEL
		if(bounds.getWidth() >= sketchSize.width/2 
		&& bounds.getHeight() >= sketchSize.height/2) {
			type = WidgetModel.PANEL;
		}
		// bounds' width > 4 * bounds' height => TEXT_FIELD
		else if(bounds.getWidth() > 4*bounds.getHeight()) {
			type = WidgetModel.TEXT_FIELD;
		}
		return type;
	}
	
	/** STATEMACHINES **/
	
	// enable draw on the sketch
	private CStateMachine attachDrawSM() {
		return new CStateMachine(view) {
			
			Gesture gesture = null;
			
			CPolyLine line = null;
			
			State beforeDrawing = new State() {
				Transition press = new Press(BUTTON1, ">> drawing") {
					public void action() {
						project.putOnTop(SketchController.this);
						
						Point2D mouse = getPoint();
						
						line = new CPolyLine(mouse);
						line.setFilled(false);
						model.addShape(line);
						view.update();
						
						gesture = new Gesture();
						gesture.addPoint(mouse.getX(), mouse.getY());
					}
				};
			};
			
			State drawing = new State() {
				Transition move = new Drag() {
					public void action() {
						Point2D mouse = getPoint();
						
						line.lineTo(mouse);
						gesture.addPoint(mouse.getX(), mouse.getY());
					}
				};
				
				Transition release = new Release(BUTTON1, ">> beforeDrawing") {
					public void action() {
						if(widgetClassifier.classify(gesture) != null) {
							// best guess for widget's type
							int type = typeChoice(getSize(), line.getBoundingBox());
							
							WidgetController widget = createWidget(line, type);
							view.addShape(widget.getView());
							
							model.addWidget(widget);
							
							for(WidgetController tmp: model.getWidgets()) {
								tmp.getView().aboveAll();
							}
							
							shapeToWidget.put(line, widget);
						} else {
							// record line creation
							project.addAction(SketchController.this, line, ActionList.CREATE);
						}
					}
				};
				
			};
		};
	}
	
	// enable changing type of a widget
	private CStateMachine attachWidgetMachine() {
		return new GestureSM(view, GestureSM.CIRCLE_TRIGO, GestureSM.VALIDATION_COLOR) {
			
			public void gestureRecognized() {
				CRectangle box = gesture.asPolyLine().getBoundingBox();
				
				WidgetController widget = getWidgetContainedIn(box);
				if(widget == null) {
					widget = getWidgetContaining(box);
				}
				
				if(widget != null) {
					if(classifier.classify(gesture).equals("circle_trigo")) {
						// decrease type number
						widget.setType(widget.getType() + 1);
					} else {
						// increase type number
						widget.setType(widget.getType() - 1);
					}
					
					popUpMachine.reset();
					
					// update widget order
					model.removeWidget(widget);
					model.addWidget(widget);
					
					view.repaint();
				}
			}
			
		};
	}
	
	
	// attach a pop up machine that display the widget type on mouse over
	public CStateMachine attachPopUpSM() {
		return new CStateMachine(view) {
		
			CText text;
			CRectangle textBackground;
			
			WidgetView widgetView;
			
			public void doReset() {
				super.doReset();
				
				if(text != null) { text.remove(); }
				if(textBackground != null) { textBackground.remove(); }
			}
			
			State out = new State() {
				Transition enter = new EnterOnShape(">> over") {
					// just check if the shape is a WidgetView
					public boolean guard() { return getShape() instanceof WidgetView; }
					
					public void action() {
						widgetView = (WidgetView) getShape();
						
						LinkedList<WidgetController> widgets = model.getWidgets();
						
						// we want the controller of the widget
						int widgetIndex = 0;
						WidgetController widgetController = widgets.get(widgetIndex);
						
						while(widgetIndex < widgets.size() && !widgetController.getView().equals(widgetView)) {
							widgetController = widgets.get(++widgetIndex);
						}
						
						String type; int space;
						switch(widgetController.getType()) {
						case WidgetModel.BUTTON: type = "Button"; space = 45; break;
						case WidgetModel.TEXT_FIELD: type = "Text field"; space = 60; break;
						default: type = "Panel"; space = 40;
						}
						
						textBackground = view.newRectangle(getPoint(), space, 15);
						textBackground.setFillPaint(new Color(0xb8, 0xcf, 0xe5));
						textBackground.setOutlinePaint(new Color(0x63, 0x82, 0xbf));
						textBackground.setReferencePoint(0, 0);
						textBackground.setPickable(false);
						
						text = view.newText(getPoint(), type);
						text.setOutlinePaint(java.awt.Color.BLACK);
						text.setReferencePoint(0, 0);
						text.setPickable(false);
					}
				};
			};
			
			State over = new State() {
				Transition drag = new MoveOnShape() {
					public boolean guard() { return getShape().equals(widgetView); }
					
					public void action() {
						text.translateTo(getPoint().getX() + 10, getPoint().getY() + 10);
						textBackground.translateTo(getPoint().getX() + 8, getPoint().getY() + 8);
					}
				};

				Transition leave = new LeaveOnShape(">> out") {
					public boolean guard() { return getShape().equals(widgetView); }
					
					public void action() { 
						view.removeShape(text);
						view.removeShape(textBackground);
					}
				};
			};
		};
	}
	
	
	// enable erasing shapes on view
	private CStateMachine attachDeleteSM() {
		return new GestureSM(view, GestureSM.PIGTAIL, GestureSM.SUPPRESSION_COLOR) {
			public void gestureRecognized() {
				model.removeShape((CPolyLine) caught);
				view.removeShape(caught);
				
				// delete widget associated to this shape if it exists
				WidgetController widget = shapeToWidget.get(caught); 
				if(widget != null) {
					popUpMachine.suspend();
					view.removeShape(widget.getView());
					model.removeWidget(widget);
					
					// warn the project that a widget has been removed
					project.removeWidget(widget);
					
					shapeToWidget.remove(caught);
					popUpMachine.resume();
					popUpMachine.reset();
					
					project.addAction(SketchController.this, widget, ActionList.DELETE);
				} else {
					project.addAction(SketchController.this, caught, ActionList.DELETE);
				}
			}
		};
	}
	
	// enable sketches to be move in the main canvas
	private CStateMachine attachMoveSM(Canvas canvas) {
		return new CStateMachine(canvas) {
			
			// distance to the top-left corner of the canvas
			Point2D delta;
			
			State movable = new State() {
				
				Transition press = new Press(BUTTON1, ">> position") {
					public void action() {
						delta = getPoint();
						// problem when writing on the sketch on top of another => wrong priority
						
						putOnTop();
					}
				};	
				
			};
			
			State position = new State() {
				
				Transition drag = new Drag(">> position") {
					public void action() {
						Point2D mouse = getPoint();
						Point2D movement = new Point2D.Double(mouse.getX() - delta.getX() , mouse.getY() - delta.getY());
						
						Point relPoint = model.getLocation();
						model.moveTo((int) (relPoint.getX() + movement.getX()), (int) (relPoint.getY() + movement.getY()));
						view.update();
					}
					
				};
				
				Transition release = new Release(BUTTON1, ">> movable") {
					// goes back to the first state
					public void action() { }
				};
			};
		};
	}
	
	
	// enable sketch's name changing
	private CStateMachine attachNameSM(CText title) {
		return new CStateMachine(title) {
			
			// variables used to do the change
			Canvas titleBar = view.getTitleBar();
			CText title = view.getTitle();
			JTextField ghost;
			
			State standing = new State() {
				Transition click = new Click(BUTTON1, ">> editing") {
					// fire transition only on a double click
					public boolean guard() {
						return getMouseEvent().getClickCount() >= 2;
					}
				};
			};
			
			State editing = new State() {
				public void enter() {
					title.remove();
					
					ghost = new JTextField(title.getText(), view.getWidth()/15);
					
					ghost.addKeyListener(new KeyListener() {
						@Override
						public void keyReleased(KeyEvent event) {
							switch(event.getKeyCode()) {
							case 10: 
								validateTitle(); break;
							case 27:
								cancelTitle(); break;
							} 
						}
						
						@Override
						public void keyTyped(KeyEvent event) {}

						@Override
						public void keyPressed(KeyEvent arg0) {}
						
					});
					titleBar.add(ghost);
					titleBar.validate();
				}
				
				Transition validate = new Click(BUTTON1, ">> standing") {
					public void action() {
						cancelTitle();
					}
				};
			};
			
			private void validateTitle() {
				String newName = ghost.getText();
				
				while(!project.isValidName(newName) && !newName.equals(model.getName())) {
					newName += "Copy";
				}
				model.changeName(newName);
				titleBar.remove(ghost);
				view.update();
				titleBar.addShape(title);
			}
			
			private void cancelTitle() {
				titleBar.remove(ghost);
				titleBar.addShape(title);
			}
		};
	}
	
	// enable duplication of sketches
	private CStateMachine attachDuplicateSM(CShape shape) {
		return new CStateMachine(shape) {
			
			State beforeClick = new State() {
				Transition press = new PressOnShape(BUTTON1, ">> clickDone") {
					public void action() {
						project.duplicateSketch(model).putOnTop();
					}
				};
			};
			
			State clickDone = new State() {
				Transition release = new ReleaseOnShape(BUTTON1, ">> beforeClick") {
					public void action() {};
				};
			};
		};
	}
	
	
	// enable resizing of sketches
	private CStateMachine attachResizeSM(CShape shape) {
		return new CStateMachine(shape) {
			
			Point2D lastPoint;
			
			State beforeDrag = new State() {
				Transition press = new PressOnShape(BUTTON1, ">> dragging") {
					public void action() {
						// disable draw machine during the resize => avoid waste lines
						drawMachine.suspend();
						lastPoint = getPoint();
					}
				};
			};
			
			
			State dragging = new State() {
				Transition drag = new Drag(">> dragging") {
					public void action() {
						// drawMachine.attachTo(view);
						int width = (int) (getPoint().getX() - lastPoint.getX());
						int height = (int) (getPoint().getY() - lastPoint.getY());

						Dimension size = model.getSize();
						setSize(size.width + width, size.height + height);
						
						view.update();
						
						lastPoint = getPoint();
					};
				};
				
				Transition release = new Release(BUTTON1, ">> beforeDrag") {
					public void action() {
						if(!hasValideDimension()) {
							setSize(minimalDimension);
						}
						
						drawMachine.reset();
						drawMachine.resume();
					}
				};
			};
		};
	}
}
