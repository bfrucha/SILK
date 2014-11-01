package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.border.LineBorder;

import controller.ProjectController;
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
	
	// menu variables
	private TitleBar titleBar;
	
	// bottom-right corner of the sketch => resize
	private CImage corner;
	
	public SketchView(SketchModel model) {
		super();
		
		this.model = model;
		
		// initialization of title bar and its components
		titleBar = new TitleBar();
		
		// icon at the bottom-right corner of the sketch
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
		setBorder(new LineBorder(ProjectView.BG_COLOR));
		
		// fixes title bar at the top of the sketch
		titleBar.init();		
		add(titleBar);
	}
	
	// get CText representing sketch's title
	public CText getTitle() {
		return titleBar.getTitle();
	}
	
	// get Sketch view's title bar
	public Canvas getTitleBar() {
		return titleBar;
	}
	
	// give a shape that will be used as a button to copy the sketch
	public CShape getCopyButton() {
		return titleBar.getCopyIcon();
	}
	
	// give a shape that will be used as a button to resize the sketch
	public CShape getResizeButton() {
		return corner;
	}
	
	public void update() {
		Dimension size = model.getSize();
		setSize(size);
		setLocation(model.getRefPoint());
		
		titleBar.update(size.width);
		
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
	
	
	private class TitleBar extends Canvas {

		private CText title;
		private CImage copyIcon;
		
		public TitleBar() {
			super();
			setLayout(new FlowLayout(FlowLayout.LEFT));
			
			copyIcon = newImage(0, 0, "images/copy-2.png");
			copyIcon.setOutlined(false);
			copyIcon.scaleBy(0.9);
			
			title = newText(0, 0, model.getName());
		}
		
		// initialization of the title bar
		public void init() {
			setBackground(Color.RED);
			setLocation(1, 1);
			title.setReferencePointToBaseline().translateTo(7, 20);
		}
		
		// get component containing sketch's title
		public CText getTitle() {
			return title;
		}
		
		// get copy icon
		public CImage getCopyIcon() {
			return copyIcon;
		}
		
		// update relative to the new width of the sketch
		public void update(int width) {
			setSize(width - 2, TB_HEIGHT);
			copyIcon.translateTo(width - 20, 17);
			title.setText(model.getName());
		}
	}
}
