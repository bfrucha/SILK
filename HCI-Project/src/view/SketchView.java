package view;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.border.LineBorder;

import model.SketchModel;
import fr.lri.swingstates.canvas.CImage;
import fr.lri.swingstates.canvas.CPolyLine;
import fr.lri.swingstates.canvas.CShape;
import fr.lri.swingstates.canvas.CText;
import fr.lri.swingstates.canvas.Canvas;

public class SketchView extends Canvas {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int TB_HEIGHT = 35;
	
	// model information are taken from
	private SketchModel model;
	
	// canvas from which the sketch will be drawn
	private Canvas parent;
	
	// menu variables
	private Canvas titleBar;
	private CText titleText;
	private CImage copyIcon;
	
	// bottom-right corner of the sketch => resize
	private CImage corner;
	
	public SketchView(SketchModel model, Canvas parent) {
		super();
		
		this.model = model;
		this.parent = parent;
		parent.add(this);
		
		titleBar = new Canvas();
		copyIcon = titleBar.newImage(0, 0, "images/copy-2.png");
		copyIcon.setOutlined(false);
		copyIcon.scaleBy(0.9);
		titleText = titleBar.newText(0, 0, model.getName());
		
		corner = newImage(0, 0, "images/corner.png");
		corner.setOutlined(false);
		
		update();
		
		init();
		
		repaint();
	}
	
	
	// create title bar of the canvas
	public void init() {
		// set absolute layout
		setLayout(null);
		setBorder(new LineBorder(MainScreen.BG_COLOR));
		
		// fixes title bar at the top of the sketch
		titleBar.setBackground(Color.RED);
		titleBar.setLocation(1, 1);
		titleText.translateTo(40., 20.);
		
		add(titleBar);
	}
	
	// get the graphical parent of this Sketch
	public Canvas getGraphicalParent() {
		return parent;
	}
	
	// get Sketch view's title bar
	public Canvas getTitleBar() {
		return titleBar;
	}
	
	// give a shape that will be used as a button to copy the sketch
	public CShape getCopyButton() {
		return copyIcon;
	}
	
	// give a shape that will be used as a button to resize the sketch
	public CShape getResizeButton() {
		return corner;
	}
	
	// put on top of all other components from parent
	public void putOnTop() {
		parent.setComponentZOrder(this, 0);
		parent.repaint();
	}
	
	public void update() {
		Dimension size = model.getSize();
		setSize(size);
		setLocation(model.getRefPoint());
		
		titleBar.setSize(size.width - 2, TB_HEIGHT);
		copyIcon.translateTo(size.width - 20, 17);
		titleText.setText(model.getName());
		
		corner.translateTo(size.width - 14, size.height - 14);
		
		// add all shapes from the model that are not already
		for(CPolyLine line: model.getShapes()) {
			if(!Canvas.contains(this, line)) {
				addShape(line);
			}
		}
		
		corner.aboveAll();
		repaint();
	}
}
