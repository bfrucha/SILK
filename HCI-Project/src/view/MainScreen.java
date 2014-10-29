package view;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import controller.SketchController;
import model.SketchModel;
import fr.lri.swingstates.canvas.CElement;
import fr.lri.swingstates.canvas.CPolyLine;
import fr.lri.swingstates.canvas.CRectangle;
import fr.lri.swingstates.canvas.CStateMachine;
import fr.lri.swingstates.canvas.Canvas;
import fr.lri.swingstates.canvas.transitions.LeaveOnShape;
import fr.lri.swingstates.canvas.transitions.PressOnShape;
import fr.lri.swingstates.canvas.transitions.EnterOnShape;
import fr.lri.swingstates.sm.State;
import fr.lri.swingstates.sm.Transition;
import fr.lri.swingstates.sm.transitions.Drag;
import fr.lri.swingstates.sm.transitions.Event;
import fr.lri.swingstates.sm.transitions.Press;
import fr.lri.swingstates.sm.transitions.Release;

public class MainScreen extends JFrame {

	public static final Color BG_COLOR = Color.gray;
	private static final long serialVersionUID = 1L;

	private Canvas mainCanvas;
	private ArrayList<Canvas> sketches;
	
	private CStateMachine listenMachine;
	
	public MainScreen(int width, int height) {
		super();
		setSize(new Dimension(width, height));	
		setLocation(300, 300);
		setVisible(true);
		setTitle("Test");
		
		init();
		validate();
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}
		
	public void init() {
		// initialization
		sketches = new ArrayList<Canvas>();
		mainCanvas = new Canvas();

		// set layout to null => absolute positioning
		mainCanvas.setLayout(null);
		mainCanvas.setBackground(BG_COLOR);
		
		getContentPane().add(mainCanvas);
		
		
		// enable sketches creation
		enableSketchesCreation();
	}
	
	// create a new Sketch with basic attributes
	public SketchController createSketch(Point tlc) {
		SketchModel model = new SketchModel("New Sketch", tlc, new Dimension(1, 1));
		SketchView view = new SketchView(model, mainCanvas);
		return new SketchController(model, view);
	}
	
	public void enableSketchesCreation() {
		CStateMachine sm = new CStateMachine(mainCanvas) {

			SketchController sketch;
			Point2D initialPoint;
			
			// creation of the new sketch
			State creation = new State() {
				
				Transition press = new Press(BUTTON3, ">> dimension") {
					public void action() {
						initialPoint = getPoint();
						
						sketch = createSketch((Point) initialPoint);
						
						mainCanvas.repaint();
					}
				};
			};
			
			// set the dimension of it
			State dimension = new State() {
				
				Transition drag = new Drag(">> dimension") {
					public void action() {
						sketch.setSize((int) (getPoint().getX() - initialPoint.getX()), (int) (getPoint().getY() - initialPoint.getY()));
					}
				};
				
				// end creation of the sketch
				Transition release = new Release(BUTTON3, ">> creation") {
					public void action() {
						// creation of the new sketch
						//newSketch = new Sketch(MainScreen.this, "New Sketch", (int) ghost.getWidth(), (int) ghost.getHeight());
						sketch.setSize((int) (getPoint().getX() - initialPoint.getX()), (int) (getPoint().getY() - initialPoint.getY()));
						
						mainCanvas.repaint();
					}
				};
				
			};
		};
	}
	
	public static void main(String[] args) {
		JFrame mainScreen = new MainScreen(1000, 1000);
	}
}
