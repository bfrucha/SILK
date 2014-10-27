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
import javax.swing.JPanel;
import javax.swing.SpringLayout;

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

public class MainScreen extends JDialog {

	private static final long serialVersionUID = 1L;

	private Canvas mainCanvas;
	private SpringLayout mainLayout;
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
		mainLayout = new SpringLayout();
		mainCanvas = new Canvas();

		mainCanvas.setLayout(mainLayout);
		mainCanvas.setBackground(Color.GRAY);
		
		getContentPane().add(mainCanvas);
		
		// enable sketches creation
		enableSketchesCreation();
		
		// listen to multiple events
		listenMachine = attachEventSM();
	}
	
	public void enableSketchesCreation() {
		CStateMachine sm = new CStateMachine(mainCanvas) {

			Sketch newSketch;
			CRectangle ghost;
			Point2D initialPoint;
			
			// creation of the new sketch
			State creation = new State() {
				
				Transition press = new Press(BUTTON3, ">> dimension") {
					public void action() {
						initialPoint = getPoint();
						
						ghost = mainCanvas.newRectangle(initialPoint.getX(), initialPoint.getY(), 1, 1);
						ghost.setFilled(false);
						mainCanvas.validate();
					}
				};
			};
			
			// set the dimension of it
			State dimension = new State() {
				
				Transition drag = new Drag(">> dimension") {
					public void action() {
						ghost.setDiagonal(initialPoint, getPoint());
					}
				};
				
				// end creation of the sketch
				Transition release = new Release(BUTTON3, ">> creation") {
					public void action() {
						// creation of the new sketch
						newSketch = new Sketch(MainScreen.this, "New Sketch", (int) ghost.getWidth(), (int) ghost.getHeight());
						
						// put constraints to dipslay at the right position
						mainLayout.putConstraint(SpringLayout.WEST, newSketch, (int) initialPoint.getX(), SpringLayout.WEST, mainCanvas);
						mainLayout.putConstraint(SpringLayout.NORTH, newSketch, (int) initialPoint.getY(), SpringLayout.NORTH, mainCanvas);
						
						// remove ghost and add the new sketch
						mainCanvas.removeShape(ghost);
						mainCanvas.add(newSketch);
						mainCanvas.validate();
					}
				};
				
			};
		};
	}
	
	
	private CStateMachine attachEventSM() {
		return new CStateMachine(mainCanvas) {
			
			State listen = new State() {
				Transition get = new Event("move", ">> listen") {
					public void action() {
						System.out.println("A sketch wants to be moved");
					}
				};
			};
			
		};
	}
	
	// makes main canvas listen to events fired by sketches
	public void listen(CStateMachine machine) {
		machine.addStateMachineListener(listenMachine);
	}
	
	public static void main(String[] args) {
		JDialog mainScreen = new MainScreen(1000, 1000);
	}
}
