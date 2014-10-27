package view;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.EventObject;

import javax.swing.JPanel;
import javax.swing.SpringLayout;

import fr.lri.swingstates.canvas.CPolyLine;
import fr.lri.swingstates.canvas.CStateMachine;
import fr.lri.swingstates.canvas.CText;
import fr.lri.swingstates.canvas.Canvas;
import fr.lri.swingstates.sm.State;
import fr.lri.swingstates.sm.StateMachine;
import fr.lri.swingstates.sm.Transition;
import fr.lri.swingstates.sm.transitions.Drag;
import fr.lri.swingstates.sm.transitions.Event;
import fr.lri.swingstates.sm.transitions.Press;
import fr.lri.swingstates.sm.transitions.Release;

public class Sketch extends Canvas {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private MainScreen parent;
	
	private Canvas titleBar;
	private CText titleText;
	private SpringLayout mainLayout;
	
	private static final int TB_HEIGHT = 35;
	
	public Sketch(MainScreen parent, String title, int width, int height) {
		super(width, height);
		
		this.parent = parent;
		titleBar = new Canvas();
		setSize(width, height);
		
		init(title);

		validate();
	}
	
	// create title bar of the canvas
	public void init(String title) {
		// set absolute layout
		setLayout(null);
		
		// fixes title bar at the top of the sketch
		titleBar.setBackground(Color.RED);
		titleBar.setLocation(0, 0);
		
		titleText = titleBar.newText(10., 10., title);
		
		add(titleBar);
		
		// enable draw on the canvas
		attachDrawSM();
		
		// enable sketch to move
		parent.listen(attachMoveSM());
	}
	
	// resizes title bar with sketch
	@Override
	public void setSize(int width, int height) {
		super.setSize(width, height);
		titleBar.setSize(width, TB_HEIGHT);
		validate();
	}
	
	
	public void changeTitle(String newTitle) {
		titleText.setText(newTitle);
	}
	
	// enable draw on the sketch
	private CStateMachine attachDrawSM() {
		return new CStateMachine(this) {
			
			Sketch sketch = Sketch.this;
			CPolyLine line;
			
			State beforeDrawing = new State() {
				Transition press = new Press(BUTTON1, ">> drawing") {
					public void action() {
						line = sketch.newPolyLine(getPoint());
						line.setFilled(false);
					}
				};
			};
			
			State drawing = new State() {
				Transition move = new Drag(">> drawing") {
					public void action() {
						line.lineTo(getPoint());
					}
				};
				
				Transition release = new Release(BUTTON1, ">> beforeDrawing") {
					public void action() {}
				};
				
			};
		};
	}
	
	// enable sketches to be move in the main canvas
	private CStateMachine attachMoveSM() {
		return new CStateMachine(titleBar) {
			
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
						Point2D mouse = getPoint();
						Point2D movement = new Point2D.Double(mouse.getX() - delta.getX() , mouse.getY() - delta.getY());
						
						Point relPoint = getLocation();
						setLocation((int) (relPoint.getX() + movement.getX()), (int) (relPoint.getY() + movement.getY()));
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
