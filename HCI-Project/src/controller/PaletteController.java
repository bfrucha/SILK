package controller;

import java.awt.Component;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Map;

import javax.swing.border.LineBorder;

import fr.lri.swingstates.canvas.CStateMachine;
import fr.lri.swingstates.canvas.Canvas;
import fr.lri.swingstates.sm.State;
import fr.lri.swingstates.sm.Transition;
import fr.lri.swingstates.sm.jtransitions.PressOnComponent;
import fr.lri.swingstates.sm.transitions.Drag;
import fr.lri.swingstates.sm.transitions.Enter;
import fr.lri.swingstates.sm.transitions.Leave;
import fr.lri.swingstates.sm.transitions.Press;
import fr.lri.swingstates.sm.transitions.Release;
import model.PaletteModel;
import view.MainScreen;
import view.PaletteView;

public class PaletteController {

	
	private MainScreen parent;
	private PaletteView view;
	private PaletteModel model;
	
	
	public PaletteController(MainScreen p, PaletteView view, PaletteModel model) {
		parent = p;
		this.view = view;
		this.model = model;
		
		movePalette(view);
		
		//attach hoverPalette to each canvas
		for (Map.Entry<String, Canvas> e : model.getModes().entrySet())
		{
			e.getValue().attachSM(clickPalette(), false);
			e.getValue().attachSM(hoverPalette(), false);
		}
	}
	
	public CStateMachine hoverPalette()
	{
		return new CStateMachine()
		{
			Canvas c;
			
			State init = new State()
			{
				Transition hover = new Enter(">> init")
				{
					public void action()
					{
						c = (Canvas)view.getComponentAt((int)view.getMousePosition().getX(), (int)view.getMousePosition().getY());
						System.out.println("Hover " + c.getName());
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
						System.out.println(c.getName());
						//TODO
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
						//if (parent.getMousePosition(true) != null)
						{
							Point2D mouse = getPoint();
							Point2D movement = new Point2D.Double(mouse.getX() - delta.getX() , mouse.getY() - delta.getY());
						
							Point relPoint = model.getPosition();
							
							int x = (int) (relPoint.getX() + movement.getX());
							int y = (int) (relPoint.getY() + movement.getY());
							
							if (x <= 0)
								model.moveTo(model.getPosition().x, y);
							else if (y <= 0)
								model.moveTo(x, model.getPosition().y);
							else if (x >= parent.getWidth()-model.getSize().width)
								model.moveTo(model.getPosition().x, y);
							else if (y >= parent.getHeight()-model.getSize().height)
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
