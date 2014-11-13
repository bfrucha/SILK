package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.geom.Point2D;

import javax.swing.border.LineBorder;

import model.SketchModel;
import fr.lri.swingstates.canvas.CImage;
import fr.lri.swingstates.canvas.CPolyLine;
import fr.lri.swingstates.canvas.CRectangle;
import fr.lri.swingstates.canvas.CShape;
import fr.lri.swingstates.canvas.CText;
import fr.lri.swingstates.canvas.Canvas;

public class SketchView extends Canvas {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int TB_HEIGHT = 35;

	public static final LineBorder VALIDE_BORDER = new LineBorder(ProjectView.BG_COLOR);
	public static final LineBorder INVALIDE_BORDER = new LineBorder(Color.red);
	
	// model information are taken from
	private SketchModel model;
	
	// all recognized widgets have this tag
	private WidgetTag widgetTag = new WidgetTag();
	
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
	
	
	// returns true if the given point is on sketch's title
	public boolean onTitle(Point2D point) {
		return titleBar.getTitle().contains(point) != null;
	}
	
	// call this method when a widget is recognized
	public void recognizedWidget(CShape shape) {
		shape.addTag(widgetTag);
	}
	
	public void update() {
		Dimension size = model.getSize();
		
		setLocation(model.getRefPoint());
		setSize(size);
		
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
	
	
	
	/* PRIVATE CLASS */
	private class TitleBar extends Canvas {

		// simulation of a background to have a gradient
		private CRectangle bg;
		private final GradientPaint bgPaint = new GradientPaint(0, 0, new Color(100, 100, 100), 0, TB_HEIGHT, new Color(200, 200, 200));
		
		private CText title;
		private CImage copyIcon;
		
		public TitleBar() {
			super();
			setLayout(new FlowLayout(FlowLayout.LEFT));
			
			setBorder(new LineBorder(Color.black, 2));
			
			bg = newRectangle(0, 0, 1, 35);
			bg.setOutlined(false);
			bg.setFillPaint(bgPaint);
			
			copyIcon = newImage(0, 0, "images/copy-2.png");
			copyIcon.setOutlined(false);
			copyIcon.scaleBy(0.9);
			
			title = newText(0, 0, model.getName());
		}
		
		// initialization of the title bar
		public void init() {
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
			setSize(width, TB_HEIGHT);
			bg.scaleTo(2, 1);
			bg.scaleBy(width, 1);
			copyIcon.translateTo(width - 20, 17);
			title.setText(model.getName());
		}
	}
}
