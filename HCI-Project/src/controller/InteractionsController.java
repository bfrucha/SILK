package controller;

import java.awt.BasicStroke;
import java.awt.geom.Point2D;
import java.util.HashMap;

import controller.ActionList.Action;
import model.InteractionsModel;
import model.WidgetModel;
import view.InteractionsView;
import view.ProjectView;
import fr.lri.swingstates.canvas.CSegment;
import fr.lri.swingstates.canvas.CStateMachine;
import fr.lri.swingstates.sm.JStateMachine;
import fr.lri.swingstates.sm.State;
import fr.lri.swingstates.sm.Transition;
import fr.lri.swingstates.sm.transitions.Drag;
import fr.lri.swingstates.sm.transitions.KeyType;
import fr.lri.swingstates.sm.transitions.Press;
import fr.lri.swingstates.sm.transitions.Release;
import fr.lri.swingstates.sm.transitions.KeyRelease;

public class InteractionsController {

	private ProjectController project;
	private InteractionsModel model;
	private InteractionsView view;
	
	private CStateMachine drawMachine;
	private CStateMachine deleteMachine;
	
	private ActionList actionList;
	
	public InteractionsController(ProjectController project, InteractionsModel model, InteractionsView view) {
		this.project = project;
		this.model = model;
		this.view = view;
		
		drawMachine = attachDrawSM();
		deleteMachine = attachDeleteSM();
		
		actionList = new ActionList();
		
		new CStateMachine() {
			State init = new State() {
				Transition keyInput = new KeyType('u') {
					public void action() {
						System.out.println("Undo !");
						undo();
					}
				};
				
				Transition keyInput2 = new KeyType('r') {
					public void action() {
						System.out.println("Redo !");
						redo();
					}
				};
			};
		}.attachTo(view);
	}

	// get all widget -> sketch interactions from the model
	public HashMap<WidgetController, Object> getInteractions() {
		return model.getInteractions();
	}
	
	// remove interactions for a given widget
	public void removeInteraction(WidgetController widget) {
		actionList.addAction(widget, model.getInteractions().get(widget), ActionList.DELETE);
				
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
								// avoid action duplication
								Object controller = model.getInteractions().get(initialWidget);
								if(controller == null || !controller.equals(widgetCaught))
									{ actionList.addAction(initialWidget, widgetCaught, ActionList.CREATE); }
								
								model.addInteraction(initialWidget, widgetCaught);
							}
							// link a widget with a sketch
							else if(sketchCaught != null && initialWidget.getType() == WidgetModel.BUTTON) {
								Object controller = model.getInteractions().get(initialWidget);
								if(controller == null || !controller.equals(sketchCaught))
									{ actionList.addAction(initialWidget, sketchCaught, ActionList.CREATE); }
								
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
				WidgetController widget = model.getWidgetFromSegment((CSegment) caught);
				actionList.addAction(widget, model.getInteractions().get(widget), ActionList.DELETE);
				
				model.removeInteraction((CSegment) caught);
				view.removeShape(caught);
			}
		};
	}
	
	public void undo() {
		Action link = actionList.undo();
		
		if(link != null) { restoreState(link, true); }
		view.update();
	}
	
	public void redo() {
		Action link = actionList.redo();
		
		if(link != null) { restoreState(link, false); }
		view.update();
	}
	
	private void restoreState(Action link, boolean undo) {
		if(link.getMode() == ActionList.CREATE && !undo) {
			if(link.getSecond() instanceof WidgetController) { model.addInteraction((WidgetController) link.getFirst(), (WidgetController) link.getSecond()); }
			else { model.addInteraction((WidgetController) link.getFirst(), (SketchController) link.getSecond());} 
		}
		// interaction already exists => delete it
		else { 
			model.removeInteraction((WidgetController) link.getFirst());
		}
	}
}
