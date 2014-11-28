package controller;

import java.awt.BasicStroke;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.HashMap;

import model.InteractionsModel;
import model.WidgetModel;
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

	// get all widget -> sketch interactions from the model
	public HashMap<WidgetController, SketchController> getInteractions() {
		return model.getInteractions();
	}
	
	// remove interactions for a given widget
	public void removeInteraction(WidgetController widget) {
		model.removeInteraction(widget);
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
						if(widgetCaught == null) {
							sketchCaught = project.getSketchAt(initialPoint);
						}
						
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
						if(sketchCaught == null) {
							// click on widget on first state
							sketchCaught = project.getSketchAt(getPoint());
						} else {
							// click on sketch on first state
							widgetCaught = project.getWidgetAt(getPoint());
						}
						
						if(widgetCaught != null && sketchCaught != null
						&& widgetCaught.getType() != WidgetModel.PANEL) {
							model.addInteraction(widgetCaught, sketchCaught);
						}
						
						ghost.remove();
						view.update();
						view.repaint();
						
						widgetCaught = null; sketchCaught = null;
					}
				};
			};
		};
	}
	
	public CStateMachine attachDeleteSM() {
		return new GestureSM(view, GestureSM.PIGTAIL) {
			public void gestureRecognized() {
				model.removeInteraction((CSegment) caught);
				view.removeShape(caught);
			}
		};
	}
}
