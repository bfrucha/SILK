package controller;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Point2D;

import javax.swing.JTextField;

import fr.lri.swingstates.canvas.CPolyLine;
import fr.lri.swingstates.canvas.CShape;
import fr.lri.swingstates.canvas.CStateMachine;
import fr.lri.swingstates.canvas.CText;
import fr.lri.swingstates.canvas.Canvas;
import fr.lri.swingstates.canvas.transitions.PressOnShape;
import fr.lri.swingstates.canvas.transitions.ReleaseOnShape;
import fr.lri.swingstates.sm.State;
import fr.lri.swingstates.sm.Transition;
import fr.lri.swingstates.sm.transitions.Click;
import fr.lri.swingstates.sm.transitions.Drag;
import fr.lri.swingstates.sm.transitions.Press;
import fr.lri.swingstates.sm.transitions.Release;
import view.SketchView;
import model.SketchModel;

public class SketchController {

	private ProjectController project;
	
	private SketchModel model;
	private SketchView view;
	
	private Dimension minimalDimension = new Dimension(100, 100);
	
	private CStateMachine drawMachine;
	private CStateMachine moveMachine;
	private CStateMachine nameMachine;
	private CStateMachine duplicateMachine;
	private CStateMachine resizeMachine;
	
	
	public SketchController(ProjectController parent, SketchModel model, SketchView view) {
		this.project = parent;

		this.model = model;
		this.view = view;
		// set border to red => not valide dimension
		if(!hasValideDimension()) {
			view.setBorder(SketchView.INVALIDE_BORDER);
		}
		
		// enable drawing on the Sketch
		drawMachine = attachDrawSM(view);
		
		// enable Sketch's movement
		moveMachine = attachMoveSM(view.getTitleBar());
		
		// enable direct editing of the Sketch's name
		nameMachine = attachNameSM(view.getTitle());
		
		// enable Sketches duplication
		duplicateMachine = attachDuplicateSM(view.getCopyButton());
		
		// enablt Sketches resizing
		resizeMachine = attachResizeSM(view.getResizeButton());
	}
	
	// access to a sketch's view from its controller
	public SketchView getView() {
		return view;
	}
	
	// change the relative point of the sketch
	public void setLocation(Point tlc) {
		model.move(tlc);
		putOnTop();
		view.update();
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
	public boolean isSelected(Point2D point) {
		Point location = view.getLocation();
		
		point.setLocation(point.getX() - location.getX(), point.getY() - location.getY());
		
		return view.onTitle(point);
	}
	
	// pause all machines this sketch is attached to
	public void suspendMachines() {
		drawMachine.suspend();
		moveMachine.suspend();
		nameMachine.suspend();
		duplicateMachine.suspend();
		resizeMachine.suspend();
	}
	
	
	// pause all machines this sketch is attached to
	public void resumeMachines() {
		drawMachine.resume();
		moveMachine.resume();
		nameMachine.resume();
		duplicateMachine.resume();
		resizeMachine.resume();
	}
	
	// enable draw on the sketch
	private CStateMachine attachDrawSM(Canvas canvas) {
		return new CStateMachine(canvas) {
			
			CPolyLine line = null;
			
			State beforeDrawing = new State() {
				Transition press = new Press(BUTTON1, ">> drawing") {
					public void action() {
						project.putOnTop(SketchController.this);
						line = new CPolyLine(getPoint());
						line.setFilled(false);
						model.addShape(line);
						view.update();
					}
				};
			};
			
			State drawing = new State() {
				Transition move = new Drag() {
					public void action() {
						line.lineTo(getPoint());
					}
				};
				
				Transition release = new Release(BUTTON1, ">> beforeDrawing") {
					public void action() {}
				};
				
			};
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
						
						Point relPoint = model.getRefPoint();
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
				model.changeName(ghost.getText());
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
