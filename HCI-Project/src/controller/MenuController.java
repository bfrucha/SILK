package controller;

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;

import javax.swing.border.LineBorder;

import model.MenuModel;
import view.MainScreen;
import view.MenuView;
import fr.lri.swingstates.canvas.Canvas;
import fr.lri.swingstates.sm.JStateMachine;
import fr.lri.swingstates.sm.State;
import fr.lri.swingstates.sm.Transition;
import fr.lri.swingstates.sm.transitions.Enter;
import fr.lri.swingstates.sm.transitions.Leave;
import fr.lri.swingstates.sm.transitions.Press;
import fr.lri.swingstates.sm.transitions.Release;

public class MenuController {
	
	private MainScreen project;
	
	private MenuView view;
	private MenuModel model;
	
	//State machines
	private JStateMachine openMachine;
	private JStateMachine hoverMachine;
	
	//Bounds of the menu
	private Rectangle closedBounds;
	private Rectangle openedBounds;
	
	public MenuController(MainScreen parent, MenuView v, MenuModel m)
	{
		project = parent;
		
		view = v;
		model = m;
		
		closedBounds = new Rectangle(0, 0, MainScreen.WIDTH, model.getHeight());
		openedBounds = new Rectangle(0, 0, MainScreen.WIDTH, model.getHeight()*2);
		
		openMachine = menuOpeningSM();
		openMachine.attachTo(view);
		hoverMachine = menuHoveringSM();
		hoverMachine.attachTo(view);
	}
	
	public MenuView getView()
	{
		return view;
	}
	
	private JStateMachine menuHoveringSM()
	{
		return new JStateMachine()
		{
			Canvas h;
			
			State init = new State()
			{
				Transition hover = new Enter(">> hovered")
				{
					public void action()
					{
						h = (Canvas)view.getComponentAt((int)getPoint().getX(), (int)getPoint().getY());
						h.setBorder(new LineBorder(Color.GREEN));
						System.out.println("HOVER " + h.getName());
					};
				};
			};
			
			State hovered = new State()
			{	
				Transition leave = new Leave(">> init")
				{
					public void action()
					{
						h.setBorder(new LineBorder(Color.BLACK));
						h = null;
					};
				};
			};
		};
	}
	
	private JStateMachine menuOpeningSM()
	{
		return new JStateMachine()
		{
			Canvas h;
			
			State unpressed = new State() 
			{	
				Transition press = new Press(BUTTON1, ">> pressed")
				{
					public void action()
					{
						h = (Canvas)view.getComponentAt((int)getPoint().getX(), (int)getPoint().getY());
						System.out.println("PRESSED " + h.getName());
						view.setBounds(openedBounds);
						//cl.setBackground(Color.BLACK);
						//cl.setFillPaint(Color.WHITE);
						
						for (Component c : view.getComponents())
							if (c.getName().contains(view.getComponentAt((int)getPoint().getX(), (int)getPoint().getY()).getName() + " "))
								c.setVisible(true);
						
					}
				};
			};
			
			
			State pressed = new State()
			{
				Transition release = new Release(BUTTON1, ">> unpressed")
				{
					public void action ()
					{
						h.setBorder(new LineBorder(Color.BLACK));
						h = null;
						
						h = (Canvas)view.getComponentAt((int)getPoint().getX(), (int)getPoint().getY());
						System.out.println("RELEASED " + h.getName());
						view.setBounds(closedBounds);
						
						for (Component c : view.getComponents())
							if (c.getName().contains(" "))
								c.setVisible(false);
					}
				};
			};
		};
	}
	
	
}
