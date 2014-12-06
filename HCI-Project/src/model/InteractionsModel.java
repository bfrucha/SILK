package model;

import java.util.HashMap;

import controller.SketchController;
import controller.WidgetController;
import fr.lri.swingstates.canvas.CSegment;

public class InteractionsModel {

	private HashMap<WidgetController, Object> interactions;
	
	private HashMap<CSegment, WidgetController> segments;
	
	public InteractionsModel() {
		interactions = new HashMap<WidgetController, Object>();
		segments = new HashMap<CSegment, WidgetController>();
	}

	// add a link between a widget and a sketch
	public void addInteraction(WidgetController widget, Object controller) {
		interactions.put(widget, controller);
	}
	
	// remove interaction for a given widget
	public void removeInteraction(WidgetController widget) {
		interactions.remove(widget);
	}
	
	public HashMap<WidgetController, Object> getInteractions() {
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
	
	public WidgetController getWidgetFromSegment(CSegment segment) {
		return segments.get(segment);
	}
}
