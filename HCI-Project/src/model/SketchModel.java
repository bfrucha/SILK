package model;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;

import view.WidgetView;
import controller.WidgetController;
import fr.lri.swingstates.canvas.CPolyLine;
import fr.lri.swingstates.canvas.CShape;

public class SketchModel {

	private String name;
	private Point topLeftCorner;
	private Dimension size;
	
	private ArrayList<CPolyLine> shapes;
	
	private ArrayList<WidgetController> widgets;
	
	public SketchModel(String name, Point tlc, Dimension dimension) {
		this.name = name;
		this.topLeftCorner = tlc;
		this.size = dimension;
		
		shapes = new ArrayList<CPolyLine>();
		widgets = new ArrayList<WidgetController>();
	}
	
	public SketchModel(SketchModel clone) {
		name = clone.getName();
		
		topLeftCorner = new Point();
		topLeftCorner.x = clone.getLocation().x;
		topLeftCorner.y = clone.getLocation().y;
		
		size = new Dimension();
		size.width = clone.getSize().width;
		size.height = clone.getSize().height;
		
		shapes = new ArrayList<CPolyLine>();
		for(CPolyLine line: clone.getShapes()) {
			CPolyLine cloneLine = (CPolyLine) line.copyTo(new CPolyLine());
			cloneLine.remove();
			addShape(cloneLine);
		}
		
		widgets = new ArrayList<WidgetController>();
		for(WidgetController widget: clone.getWidgets()) {
			WidgetModel cloneModel = new WidgetModel(widget.getModel());
			WidgetView cloneView = new WidgetView(cloneModel);
			
			// little problem here, no sketch controller to associate
			widgets.add(new WidgetController(null, cloneModel, cloneView));
		}
	}
	
	// change Sketch's name
	public void changeName(String newName) {
		name = newName;
	}
	 
	// get Sketch's name
	public String getName() {
		return name;
	}
	
	// move to the new relative point
	public void move(Point tlc) {
		topLeftCorner = tlc;
	}
	
	// move to absolute coordinates
	public void moveTo(int x, int y) {
		topLeftCorner.x = x;
		topLeftCorner.y = y;
	}
	
	// translate by vector (x, y)
	public void moveBy(int x, int y) {
		topLeftCorner.x += x;
		topLeftCorner.y += y;
	}
	
	// get Sketch's reference point
	public Point getLocation() {
		return topLeftCorner;
	}
	
	// set new size of the sketch
	public void setSize(Dimension dimension) {
		size = dimension;
	}
	
	// scale Sketch's dimension by scaleX on width and scaleY on height
	public void scale(int scaleX, int scaleY) {
		size.width *= scaleX;
		size.height *= scaleY;
	}
	
	// get Sketch's size
	public Dimension getSize() {
		return size;
	}
	
	// get all the shapes contained in this Sketch
	public ArrayList<CPolyLine> getShapes() {
		return shapes;
	}
	
	// add a shape to the list
	public void addShape(CPolyLine shape) {
		shapes.add(shape);
	}
	
	// remove specified shape from the list
	public boolean removeShape(CPolyLine line) {
		return shapes.remove(line);
	}
	
	// get all the widgets contained in this Sketch
	public ArrayList<WidgetController> getWidgets() {
		return widgets;
	}
	
	// add a widget to the list
	public void addWidget(WidgetController widget) {
		widgets.add(widget);
	}
	
	// remove specified widget from the list
	public boolean removeWidget(WidgetController widget) {
		return widgets.remove(widget);
	}
}
