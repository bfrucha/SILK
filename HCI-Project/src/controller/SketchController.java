package controller;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;

import fr.lri.swingstates.canvas.CPolyLine;
import fr.lri.swingstates.canvas.CShape;
import fr.lri.swingstates.canvas.CStateMachine;
import fr.lri.swingstates.canvas.Canvas;
import fr.lri.swingstates.canvas.transitions.LeaveOnShape;
import fr.lri.swingstates.canvas.transitions.PressOnShape;
import fr.lri.swingstates.canvas.transitions.ReleaseOnShape;
import fr.lri.swingstates.sm.State;
import fr.lri.swingstates.sm.Transition;
import fr.lri.swingstates.sm.transitions.Drag;
import fr.lri.swingstates.sm.transitions.Press;
import fr.lri.swingstates.sm.transitions.Release;
import view.SketchView;
import model.SketchModel;

public class SketchController {

	private SketchModel model;
	private SketchView view;
	
	private CStateMachine drawMachine;
	private CStateMachine moveMachine;
	private CStateMachine duplicateMachine;
	private CStateMachine resizeMachine;
	
	
	public SketchController(SketchModel model, SketchView view) {
		this.model = model;
		this.view = view;
		
		// enable drawing on the Sketch
		drawMachine = attachDrawSM(view);
		
		// enable Sketch's movement
		moveMachine = attachMoveSM(view.getTitleBar());
		
		// enable Sketches duplication
		duplicateMachine = attachDuplicateSM(view.getCopyButton());
		
		// enablt Sketches resizing
		resizeMachine = attachResizeSM(view.getResizeButton());
	}
	
	// change the relative point of the sketch
	public void setLocation(Point tlc) {
		model.move(tlc);
		view.putOnTop();
		view.update();
	}
	
	// change the size of the sketch
	public void setSize(int width, int height) {
		setSize(new Dimension(width, height));
	}
	
	public void setSize(Dimension dimension) {
		model.setSize(dimension);
		view.update();
	}
	
	// enable draw on the sketch
	private CStateMachine attachDrawSM(Canvas canvas) {
		return new CStateMachine(canvas) {
			
			CPolyLine line;
			
			State beforeDrawing = new State() {
				Transition press = new Press(BUTTON1, ">> drawing") {
					public void action() {
						line = new CPolyLine(getPoint());
						line.setFilled(false);
						model.addShape(line);
						view.update();
					}
				};
			};
			
			State drawing = new State() {
				Transition move = new Drag(">> drawing") {
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
						view.putOnTop();
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
	
	// enable duplication of sketches
	private CStateMachine attachDuplicateSM(CShape shape) {
		return new CStateMachine(shape) {
			
			State beforeClick = new State() {
				Transition press = new PressOnShape(BUTTON1, ">> clickDone") {
					public void action() {
						SketchModel cloneModel = new SketchModel(model);
						SketchView cloneView = new SketchView(cloneModel, view.getGraphicalParent());
						SketchController cloneController = new SketchController(cloneModel, cloneView);
						
						cloneModel.moveBy(40, 40);
						cloneView.putOnTop();
						cloneView.update();
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

						lastPoint = getPoint();
						
						Dimension size = model.getSize();
						size.width += width;
						size.height += height;
						
						view.update();
					};
				};
				
				Transition release = new Release(BUTTON1, ">> beforeDrag") {
					public void action() {
						drawMachine.resume();
					}
				};
			};
		};
	}
}
