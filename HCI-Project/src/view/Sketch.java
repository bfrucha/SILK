package view;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.EventObject;

import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import fr.lri.swingstates.canvas.CImage;
import fr.lri.swingstates.canvas.CPolyLine;
import fr.lri.swingstates.canvas.CShape;
import fr.lri.swingstates.canvas.CStateMachine;
import fr.lri.swingstates.canvas.CText;
import fr.lri.swingstates.canvas.Canvas;
import fr.lri.swingstates.canvas.transitions.PressOnShape;
import fr.lri.swingstates.canvas.transitions.ReleaseOnShape;
import fr.lri.swingstates.sm.State;
import fr.lri.swingstates.sm.StateMachine;
import fr.lri.swingstates.sm.Transition;
import fr.lri.swingstates.sm.transitions.Drag;
import fr.lri.swingstates.sm.transitions.Event;
import fr.lri.swingstates.sm.transitions.Press;
import fr.lri.swingstates.sm.transitions.Release;

public class Sketch extends Canvas implements Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int TB_HEIGHT = 35;
	
	
	private Canvas parent;
	
	// menu variables
	private Canvas titleBar;
	private CText titleText;
	private CImage copyIcon;
	
	
	public Sketch(Canvas parent, String title, int width, int height) {
		super(width, height);
		
		this.parent = parent;
		titleBar = new Canvas();
		copyIcon = titleBar.newImage(0, 0, "images/copy-2.png");
		copyIcon.setOutlined(false);
		copyIcon.scaleBy(0.9);
		
		setSize(width, height);
		
		init(title);

		validate();
	}
	
	// create title bar of the canvas
	public void init(String title) {
		// set absolute layout
		setLayout(null);
		setBorder(new LineBorder(MainScreen.BG_COLOR));
		
		// fixes title bar at the top of the sketch
		titleBar.setBackground(Color.RED);
		titleBar.setLocation(1, 1);
		
		titleText = titleBar.newText(10., 10., title);
		
		add(titleBar);
		
		// enable draw on the canvas
		attachDrawSM();
		
		// enable sketch to move
		attachMoveSM(titleBar);
		
		// enable duplication of sketches
		attachDuplicateSM(copyIcon);
	}
	
	// resizes title bar with sketch
	@Override
	public void setSize(int width, int height) {
		super.setSize(width, height);
		titleBar.setSize(width - 2, TB_HEIGHT);
		copyIcon.translateTo(width - 20, 17);
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
	private CStateMachine attachMoveSM(Canvas canvas) {
		return new CStateMachine(canvas) {
			
			// distance to the top-left corner of the canvas
			Point2D delta;
			
			State movable = new State() {
				
				Transition press = new Press(BUTTON1, ">> position") {
					public void action() {
						System.out.println("Press on title bar");
						delta = getPoint();
						// problem when writing on the sketch on top of another => wrong priority
						parent.setComponentZOrder(Sketch.this, 0);
						parent.repaint();
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
	
	// enable duplication of sketches
	private void attachDuplicateSM(CShape shape) {
		new CStateMachine(shape) {
			
			State beforeClick = new State() {
				Transition press = new PressOnShape(BUTTON1, ">> clickDone") {
					public void action() {}
				};
			};
			
			State clickDone = new State() {
				Transition release = new ReleaseOnShape(BUTTON1, ">> beforeClick") {
					public void action() {
						// clone current sketch
						Sketch clone = new Sketch(parent, titleText.getText(), getWidth(), getHeight());
						
						// offset to create a visual difference between sketches
						Point2D location = getLocation();
						clone.setLocation(((int) location.getX()) + 40, ((int) location.getY()) + 40);
						
						parent.add(clone);
						parent.setComponentZOrder(clone, 0);
						
						parent.repaint();
					}
				};	
			};
		};
	}
}
