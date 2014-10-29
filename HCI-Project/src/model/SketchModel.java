package model;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;

import fr.lri.swingstates.canvas.CPolyLine;
import fr.lri.swingstates.canvas.CShape;

public class SketchModel {

	private String name;
	private Point topLeftCorner;
	private Dimension size;
	private ArrayList<CPolyLine> shapes;
	
	public SketchModel(String name, Point tlc, Dimension dimension) {
		this.name = name;
		this.topLeftCorner = tlc;
		this.size = dimension;
		shapes = new ArrayList<CPolyLine>();
	}
	
	public SketchModel(SketchModel clone) {
		name = clone.getName();
		
		topLeftCorner = new Point();
		topLeftCorner.x = clone.getRefPoint().x;
		topLeftCorner.y = clone.getRefPoint().y;
		
		size = new Dimension();
		size.width = clone.getSize().width;
		size.height = clone.getSize().height;
		
		shapes = new ArrayList<CPolyLine>();
		for(CPolyLine line: clone.getShapes()) {
			CPolyLine cloneLine = (CPolyLine) line.copyTo(new CPolyLine());
			cloneLine.remove();
			addShape(cloneLine);
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
	public Point getRefPoint() {
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
}
