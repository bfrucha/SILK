package view;

import java.awt.BasicStroke;
import java.awt.Color;

import fr.lri.swingstates.canvas.CExtensionalTag;
import fr.lri.swingstates.canvas.CShape;

// tag added to all recognized widgets
public class WidgetTag extends CExtensionalTag {

	public void added(CShape s) {
		s.setStroke(new BasicStroke(2));
		s.setOutlinePaint(Color.GREEN);
	}
	
}
