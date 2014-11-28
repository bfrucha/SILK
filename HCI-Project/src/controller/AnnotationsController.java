package controller;

import java.awt.BasicStroke;
import java.util.ArrayList;

import fr.lri.swingstates.canvas.CPolyLine;
import fr.lri.swingstates.canvas.CRectangle;
import fr.lri.swingstates.canvas.CShape;
import fr.lri.swingstates.canvas.CStateMachine;
import fr.lri.swingstates.sm.State;
import fr.lri.swingstates.sm.Transition;
import fr.lri.swingstates.sm.transitions.Drag;
import fr.lri.swingstates.sm.transitions.Press;
import fr.lri.swingstates.sm.transitions.Release;
import view.AnnotationsView;
import view.ProjectView;

public class AnnotationsController {

	private AnnotationsView view;
	
	private CStateMachine drawMachine;
	private CStateMachine deleteMachine;
	
	public AnnotationsController(AnnotationsView view) {
		this.view = view;
		
		drawMachine = attachDrawSM();
		deleteMachine = attachDeleteSM();
	}
	
	public CStateMachine attachDrawSM() {
		return new CStateMachine(view) {
			CPolyLine annotation;
			
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
		};
	}
	
	
	public CStateMachine attachDeleteSM() {
		return new CStateMachine(view) {
			CPolyLine ghost;
			
			ArrayList<CShape> caughtShapes;
			
			State wait = new State() {
				Transition press = new Press(BUTTON3, ">> drawing") {
					public void action() {
						ghost = view.newPolyLine(getPoint());
						ghost.setFilled(false);
						ghost.setOutlinePaint(ProjectView.SUPPRESSION_ACTION_COLOR);
						ghost.setStroke(new BasicStroke(2));
						ghost.setPickable(false);
						
						caughtShapes = new ArrayList<CShape>();
					}
				};
			};
			
			State drawing = new State() {
				Transition drag = new Drag() {
					public void action() {
						ghost.lineTo(getPoint());
						
						CShape shape = view.pick(getPoint());
						
						if(shape != null && !(shape instanceof CRectangle)) {
							caughtShapes.add(shape);
						}
					}
				};
				
				Transition release = new Release(BUTTON3, ">> wait") {
					public void action() {
						ghost.lineTo(getPoint());
						view.removeShape(ghost);
						
						for(CShape caught: caughtShapes) {
							view.removeShape(caught);
						}
						view.repaint();
					}
				};
			};
		};
	}
}
