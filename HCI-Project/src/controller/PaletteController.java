package controller;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Map;

import javax.swing.JPanel;

import model.PaletteModel;
import view.MainScreen;
import view.PaletteView;
import fr.lri.swingstates.canvas.CStateMachine;
import fr.lri.swingstates.canvas.Canvas;
import fr.lri.swingstates.sm.JStateMachine;
import fr.lri.swingstates.sm.State;
import fr.lri.swingstates.sm.Transition;
import fr.lri.swingstates.sm.transitions.Drag;
import fr.lri.swingstates.sm.transitions.Enter;
import fr.lri.swingstates.sm.transitions.Leave;
import fr.lri.swingstates.sm.transitions.Press;
import fr.lri.swingstates.sm.transitions.Release;

public class PaletteController {

	
	private MainScreen parent;
	private PaletteView view;
	private PaletteModel model;
	private ProjectController pc;
	
	
	public PaletteController(MainScreen p, PaletteView view, PaletteModel model, ProjectController pc) {
		parent = p;
		this.view = view;
		this.model = model;
		this.pc = pc;
		
		movePalette(view);
		
		//attach hoverPalette to each canvas
		for (Map.Entry<String, Canvas> e : model.getModes().entrySet())
		{
			e.getValue().attachSM(clickPalette(), false);
			hoverPalette().attachTo(e.getValue());
		}
	}
	
	public JStateMachine hoverPalette()
	{
		return new JStateMachine()
		{
			Canvas c;
			Canvas info;
			
			State init = new State()
			{
				Transition hover = new Enter(">> hover")
				{
					public void action()
					{
						c = (Canvas)view.getComponentAt((int)view.getMousePosition().getX(), (int)view.getMousePosition().getY());
						if (c != null && c.getName() != "null")
						{
							c.newImage(2.5, 2.5, "images/"+c.getName()+"_neg.png").setOutlined(false);
						}
					};
				};
			};
			
			
			State hover = new State()
			{	
				Transition leave = new Leave(">> init")
				{
					public void action()
					{
						if (c != null && c.getName() != "null")
							c.newImage(2.5, 2.5, "images/"+c.getName()+".png").setOutlined(false);
						c = null;
					};
				};
			};
		};
	}
	
	public CStateMachine clickPalette()
	{
		return new CStateMachine() 
		{
			State init = new State()
			{
				Transition click = new Press(BUTTON1, ">> init")
				{
					public void action()
					{
						Canvas c = (Canvas)view.getComponentAt((int)view.getMousePosition().getX(), (int)view.getMousePosition().getY());
						if (c.getName() == "annotate")
						{
							pc.changeMode(ProjectController.ANNOTATIONS_MODE);
						}
						else if (c.getName() == "draw")
						{
							pc.changeMode(ProjectController.WIDGETS_MODE);
						}
						else
						{
							pc.changeMode(ProjectController.INTERACTIONS_MODE);
						}
					};
				};
			};
		};
	}
	
	public CStateMachine movePalette(PaletteView v)
	{
		return new CStateMachine(v) 
		{
			// distance to the top-left corner of the canvas
			Point2D delta;
			
			State movable = new State() {
				
				Transition press = new Press(BUTTON1, ">> position") {
					public void action() {
						delta = getPoint();
					}
				};	
				
			};
			
			State position = new State() {
				Transition drag = new Drag(">> position") {
					public void action() {
						if (parent.getMousePosition(true) != null)
						{
							Point2D mouse = getPoint();
							Point2D movement = new Point2D.Double(mouse.getX() - delta.getX() , mouse.getY() - delta.getY());
						
							Point relPoint = model.getPosition();
							
							int x = (int) (relPoint.getX() + movement.getX());
							int y = (int) (relPoint.getY() + movement.getY());
							
							if (x <= 0)
								model.moveTo(model.getPosition().x, y);
							if (y <= 0)
								model.moveTo(x, model.getPosition().y);
							if (x >= parent.getWidth()-model.getSize().width)
								model.moveTo(model.getPosition().x, y);
							if (y >= parent.getHeight()-model.getSize().height)
								model.moveTo(x, model.getPosition().y);
							
							else
								model.moveTo((int) (relPoint.getX() + movement.getX()), (int) (relPoint.getY() + movement.getY()));
							
							view.repaint();
						}
					}
					
				};
				
				Transition release = new Release(BUTTON1, ">> movable") {
					// goes back to the first state
					public void action() { }
				};
			};
		};
	}
	
	
}
