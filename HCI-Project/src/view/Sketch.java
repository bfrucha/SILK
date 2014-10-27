package view;

import java.awt.Color;
import java.awt.Container;
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
	
	public Sketch(MainScreen parent, String title, int width, int height) {
		super(width, height);
		
		this.parent = parent;
		
		init(title);
		validate();
	}
	
	// create title bar of the canvas
	public void init(String title) {
		// set main layout
		mainLayout = new SpringLayout();
		setLayout(mainLayout);
		
		// fixes title bar at the top of the sketch
		titleBar = new Canvas();
		titleBar.setBackground(Color.RED);
		mainLayout.putConstraint(SpringLayout.WEST, titleBar, 0, SpringLayout.WEST, this);
		mainLayout.putConstraint(SpringLayout.EAST, titleBar, 0, SpringLayout.EAST, this);
		mainLayout.putConstraint(SpringLayout.NORTH, titleBar, 0, SpringLayout.NORTH, this);
		mainLayout.putConstraint(SpringLayout.SOUTH, titleBar, 35, SpringLayout.NORTH, this);
		
		titleText = titleBar.newText(10., 10., title);
		
		add(titleBar);
		
		// enable draw on the canvas
		attachDrawSM();
		
		// enable sketch to move
		parent.listen(attachMoveSM());
	}
	
	public void changeTitle(String newTitle) {
		titleText.setText(newTitle);
	}
	
	
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
			
			Point2D initialPoint;
			
			State movable = new State() {
				
				Transition press = new Press(BUTTON1, ">> position") {
					public void action() {
						initialPoint = getPoint();
					}
				};
				
			};
			
			State position = new State() {
				
				Transition release = new Release(BUTTON1, ">> movable") {
					public void action() {
						fireEvent("move");
					}
				};
				
			};
		};
	}
}
