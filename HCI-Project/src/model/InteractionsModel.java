package model;

import java.util.HashMap;

import controller.SketchController;
import controller.WidgetController;
import fr.lri.swingstates.canvas.CSegment;

public class InteractionsModel {

	private HashMap<WidgetController, SketchController> interactions;
	
	private HashMap<CSegment, WidgetController> segments;
	
	public InteractionsModel() {
		interactions = new HashMap<WidgetController, SketchController>();
		segments = new HashMap<CSegment, WidgetController>();
	}

	// add a link between a widget and a sketch
	public void addInteraction(WidgetController widget, SketchController sketch) {
		interactions.put(widget, sketch);
	}
	
	// remove interaction for a given widget
	public void removeInteraction(WidgetController widget) {
		interactions.remove(widget);
	}
	
	public HashMap<WidgetController, SketchController> getInteractions() {
		return interactions;
	}
	
	// associates segment to a widget to prepare for future suppression
	public void addSegment(CSegment segment, WidgetController widget) {
		segments.put(segment, widget);
	}
	
	// remove interaction of a widget based on its visual representation
	public void removeInteraction(CSegment segment) {
		System.out.println(interactions.remove(segments.get(segment)));
	}
}
