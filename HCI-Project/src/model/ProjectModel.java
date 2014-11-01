package model;

import java.util.ArrayList;

import controller.SketchController;

public class ProjectModel {

	private ArrayList<SketchController> sketches;
	
	// should contain interaction between sketches
	public ProjectModel() {
		sketches = new ArrayList<SketchController>();
	}
	
	// get all the sketches contained in this project
	public ArrayList<SketchController> getSketches() {
		return sketches;
	}
	
	// add a new sketch to the project
	public void addSketch(SketchController sketch) {
		sketches.add(sketch);
	}
	
	// remove a sketch from the project
	public boolean removeSketch(SketchController sketch) {
		return sketches.remove(sketch);
	}
}
