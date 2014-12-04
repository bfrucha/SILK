package view;

import java.awt.BasicStroke;
import java.awt.Color;

import model.WidgetModel;
import fr.lri.swingstates.canvas.CRectangle;

// represents a widget (rectangular shape for now)
public class WidgetView extends CRectangle {
	
	WidgetModel model;
	
	public final static Color BUTTON_PAINT = new Color(0x40, 0x0e, 0x9c);
	private Color buttonFP = new Color(BUTTON_PAINT.brighter().getRed(), BUTTON_PAINT.brighter().getGreen(), BUTTON_PAINT.brighter().getBlue(), 100);
	private Color buttonOP = new Color(BUTTON_PAINT.darker().getRed(), BUTTON_PAINT.darker().getGreen(), BUTTON_PAINT.darker().getBlue(), 100);
	
	public final static Color PANEL_PAINT = new Color(0xe5, 0x93, 0x00);
	private Color panelFP = new Color(PANEL_PAINT.brighter().getRed(), PANEL_PAINT.brighter().getGreen(), PANEL_PAINT.brighter().getBlue(), 100);
	private Color panelOP = new Color(PANEL_PAINT.darker().getRed(), PANEL_PAINT.darker().getGreen(), PANEL_PAINT.darker().getBlue(), 100);

	public final static Color TF_PAINT = new Color(0x00, 0xB7, 0x00);
	private Color tfFP = new Color(TF_PAINT.brighter().getRed(), TF_PAINT.brighter().getGreen(), TF_PAINT.brighter().getBlue(), 100);
	private Color tfOP = new Color(TF_PAINT.darker().getRed(), TF_PAINT.darker().getGreen(), TF_PAINT.darker().getBlue(), 100);
	
	public WidgetView(WidgetModel model) {
		super(model.getX(), model.getY(), model.getWidth(), model.getHeight());
		
		this.model = model;
		
		setStroke(new BasicStroke(2));
		update();
		
		setDrawable(false);
	}
	
	public void update() {
		Color fillPaint, outlinePaint;
		
		switch(model.getType()) {
		case WidgetModel.BUTTON:
			fillPaint = buttonFP;
			outlinePaint = buttonOP;
			break;
		case WidgetModel.TEXT_FIELD:
			fillPaint = tfFP;
			outlinePaint = tfOP;
			break;
		default:
			fillPaint = panelFP;
			outlinePaint = panelOP;
		}
		
		setFillPaint(fillPaint);
		setOutlinePaint(outlinePaint);
	}
}
