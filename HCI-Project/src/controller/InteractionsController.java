package controller;

import java.awt.BasicStroke;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.HashMap;

import model.InteractionsModel;
import view.InteractionsView;
import view.ProjectView;
import fr.lri.swingstates.canvas.CPolyLine;
import fr.lri.swingstates.canvas.CRectangle;
import fr.lri.swingstates.canvas.CSegment;
import fr.lri.swingstates.canvas.CStateMachine;
import fr.lri.swingstates.canvas.Canvas;
import fr.lri.swingstates.sm.State;
import fr.lri.swingstates.sm.Transition;
import fr.lri.swingstates.sm.transitions.Drag;
import fr.lri.swingstates.sm.transitions.Press;
import fr.lri.swingstates.sm.transitions.Release;

public class InteractionsController {

	private ProjectController project;
	private InteractionsModel model;
	private InteractionsView view;
	
	private CStateMachine drawMachine;
	private CStateMachine deleteMachine;
	
	public InteractionsController(ProjectController project, InteractionsModel model, InteractionsView view) {
		this.project = project;
		this.model = model;
		this.view = view;
		
		drawMachine = attachDrawSM();
		deleteMachine = attachDeleteSM();
	}
	
	public InteractionsModel getModel() {
		return model;
	}
	
	public CStateMachine attachDrawSM() {
		return new CStateMachine(view) {
		 	CSegment ghost;
			Point2D initialPoint;
			
			WidgetController widgetCaught;
			SketchController sketchCaught;
		 	
			State wait = new State() {
				Transition press = new Press(BUTTON1, ">> drawing") {
					public void action() {
						initialPoint = getPoint();
						
						widgetCaught = project.getWidgetAt(initialPoint);
						
						ghost = view.newSegment(initialPoint, initialPoint);
						ghost.setFilled(false);
						ghost.setStroke(new BasicStroke(2));
						ghost.setOutlinePaint(ProjectView.ACTION_COLOR);
					}
				};
			};
			
			State drawing = new State() {
				Transition drag = new Drag() {
					public void action() {
						ghost.setPoints(initialPoint, getPoint());
					}
				};
				
				Transition release = new Release(">> wait") {
					public void action() {
						sketchCaught = project.getSketchAt(getPoint());
						
						if(widgetCaught != null && sketchCaught != null) {
							model.addInteraction(widgetCaught, sketchCaught);
						}
						
						ghost.remove();
						view.update();
						view.repaint();
					}
				};
			};
		};
	}
	
	public CStateMachine attachDeleteSM() {
		return new DeleteShapeSM(view) {
			public void gestureRecognized() {
				model.removeInteraction((CSegment) caught);
				view.removeShape(caught);
			}
		};
	}
}
