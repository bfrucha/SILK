package controller;

import java.awt.geom.Point2D;

import view.ProjectView;
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
import fr.lri.swingstates.sm.transitions.Press;
import fr.lri.swingstates.sm.transitions.Release;

public abstract class GestureSM extends CStateMachine {
	
	public static final Dollar1Classifier PIGTAIL = Dollar1Classifier.newClassifier("classifier/delete.cl");
	public static final Dollar1Classifier CIRCLE_TRIGO = Dollar1Classifier.newClassifier("classifier/circle.cl");
	
	private Canvas view;
	
	// classifier and gesture which will help classify user's input
	protected Dollar1Classifier classifier;
	protected Gesture gesture = null;
	
	private CPolyLine ghost = null;
	
	protected CShape caught = null;
	
	
	public GestureSM(Canvas view, Dollar1Classifier classifier) {
		super(view);
		this.view = view;
		this.classifier = classifier;
	}
	
	
	public State init = new State() {
		Transition press = new Press(BUTTON3, ">> erasing") {
			public void action() {
				Point2D mouse = getPoint();
				
				gesture = new Gesture();	
				gesture.addPoint(mouse.getX(), mouse.getY());
				
				ghost = view.newPolyLine(mouse);
				ghost.setFilled(false);
				ghost.setOutlinePaint(ProjectView.SUPPRESSION_ACTION_COLOR);
				ghost.setPickable(false);
			}
		};
	};
	
	public State erasing = new State() {
		Transition drawing = new Drag() {
			public void action() {
				Point2D mouse = getPoint();
				
				gesture.addPoint(mouse.getX(), mouse.getY());
				
				ghost.lineTo(mouse);
				
				// catch the shape below the gesture
				if(caught == null || caught instanceof CRectangle) {
					caught = view.pick(mouse);
				}
			}
		};
		
		Transition release = new Release(BUTTON3, ">> init") {
			public void action() {
				view.removeShape(ghost);
				if(classifier.classify(gesture) != null && !(caught instanceof CRectangle)) {
					gestureRecognized();
					
					caught = null;
					view.repaint();
				}
			}
		};
	};
	
	// called when gesture is recognized
	public abstract void gestureRecognized();
	
}
