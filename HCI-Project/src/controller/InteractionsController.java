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
	public HashMap<WidgetController, Object> getInteractions() {
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
			
			WidgetController initialWidget;
			SketchController initialSketch;
		 	
			State wait = new State() {
				Transition press = new Press(BUTTON1, ">> drawing") {
					public void action() {
						initialPoint = getPoint();
						
						initialWidget = project.getWidgetAt(initialPoint);
						
						initialSketch = project.getSketchAt(initialPoint);
						
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
						// draw only if a widget has been caught
						if(initialWidget != null) {
							ghost.setPoints(initialPoint, getPoint());
						}
					}
				};
				
				Transition release = new Release(">> wait") {
					public void action() {
						if(initialWidget != null) {
							// get elements under last mouse position
							WidgetController widgetCaught = project.getWidgetAt(getPoint());
							SketchController sketchCaught = project.getSketchAt(getPoint());
							
							
							HashMap<WidgetController, Object> interactions = model.getInteractions();
							// link two widgets
							if(initialSketch == sketchCaught && widgetCaught != null) {
								model.addInteraction(initialWidget, widgetCaught);
							}
							// link a widget with a sketch
							else if(sketchCaught != null && initialWidget.getType() == WidgetModel.BUTTON) {
								System.out.println("Widget => sketch");
								model.addInteraction(initialWidget, sketchCaught);
							}
							
							ghost.remove();
							view.update();
							view.repaint();
							
							initialWidget = null; initialSketch = null;
						}
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
