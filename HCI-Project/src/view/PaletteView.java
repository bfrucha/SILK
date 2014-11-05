package view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Point2D;
import java.util.Map;

import javax.swing.border.LineBorder;

import model.PaletteModel;
import fr.lri.swingstates.canvas.CImage;
import fr.lri.swingstates.canvas.Canvas;

public class PaletteView extends Canvas {
	
	private PaletteModel model;
	private Canvas titleBar;
	
	///Icons
	private final String ICON_ANNOTATE 			= "images/annotate.png";
	private final String ICON_DRAW 				= "images/draw.png";
	private final String ICON_INTERACT 			= "images/interact.png";
	/*private final String ICON_ANNOTATE_NEG 		= "images/annotate_neg.png";
	private final String ICON_DRAW_NEG 			= "images/draw_neg.png";
	private final String ICON_INTERACT_NEG 		= "images/interact_neg.png";*/
	
	public PaletteView(PaletteModel m)
	{
		super(m.getSize().width, m.getSize().height);
		model = m;
		
		setLayout(null);
		setSize(model.getSize());
		setBackground(model.getBg());
		setBorder(new LineBorder(model.getBorder()));
		
		initModes();
		
		validate();
	}
	
	public void initModes() 
	{
		//Modes
		int init = 10;
		for (Map.Entry<String, Canvas> e : model.getModes().entrySet())
		{
			e.getValue().setSize(model.getModes_size());
			e.getValue().setBorder(new LineBorder(model.getBorder()));
			e.getValue().setBackground(model.getMODES_BG());
			e.getValue().setLocation(0, init);
			
			if (e.getKey() == "Annotate")
			{
				e.getValue().setName("Annotate");
				e.getValue().newImage(2.5, 2.5, ICON_ANNOTATE).setOutlined(false);
			}
			else if (e.getKey() == "Draw")
			{
				e.getValue().setName("Draw");
				e.getValue().newImage(2.5, 2.5, ICON_DRAW).setOutlined(false);
			}
			else
			{
				e.getValue().setName("Interact");
				e.getValue().newImage(2.5, 2.5, ICON_INTERACT).setOutlined(false);
			}
			
			this.add(e.getValue());
			
			init += model.getModes_size().height;
		}
	}



	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		setLocation(model.getPosition());
	}
}