package controller;

import java.awt.BasicStroke;

import fr.lri.swingstates.canvas.CPolyLine;
import fr.lri.swingstates.canvas.CStateMachine;
import fr.lri.swingstates.canvas.Canvas;
import fr.lri.swingstates.sm.State;
import fr.lri.swingstates.sm.Transition;
import fr.lri.swingstates.sm.transitions.Drag;
import fr.lri.swingstates.sm.transitions.Press;
import fr.lri.swingstates.sm.transitions.Release;
import view.AnnotationsView;

public class AnnotationsController {

	private AnnotationsView view;
	
	private CStateMachine drawMachine;
	
	public AnnotationsController(AnnotationsView view) {
		this.view = view;
		
		drawMachine = attachDrawSM();
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
}
