package model;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.TreeMap;

import fr.lri.swingstates.canvas.CImage;
import fr.lri.swingstates.canvas.Canvas;

public class PaletteModel {
	
	//Palette
	private final Dimension size = new Dimension(50, 160);
	private Point position;
	
	private final Color BG_INIT = new Color(90, 90, 90, 200);
	private final Color BG_CLICKED = new Color(90, 90, 90);
	private final Color BORDER = new Color(29,29,29);
	private Color bg;
	
	//Titlebar
	private final int TITLEBAR_HEIGHT = 10;
	private final Color TITLEBAR_BG = Color.BLACK;
	
	//Modes
	private TreeMap<String, Canvas> modes;
	private Dimension modes_size = new Dimension(50, 50);
	private final Color MODES_BG = new Color(200, 200, 200, 200);
	
	public PaletteModel ()
	{
		bg = BG_INIT;
		position = new Point(1, 150);
		modes = new TreeMap<String, Canvas>();
		modes.put("Draw", 		new Canvas());
		modes.put("Interact", 	new Canvas());
		modes.put("Annotate", 	new Canvas());
	}
	
	public void moveTo(int x, int y)
	{
		position.x = x;
		position.y = y;
	}

	public Point getPosition() { return position; }

	public void setPosition(Point position) { this.position = position; }
	
	public Dimension getSize() { return size; }

	public int getTB_HEIGHT() { return TITLEBAR_HEIGHT; }

	public Color getBg() { return bg; }
	
	public void setBg(Color nb) { bg = nb; }

	public Color getBG_INIT() {return BG_INIT;}

	public Color getTB_BG() { return TITLEBAR_BG; }

	public Color getBorder() { return BORDER; }

	public TreeMap<String, Canvas> getModes() {
		return modes;
	}

	public Color getBG_CLICKED() {
		return BG_CLICKED;
	}

	public Dimension getModes_size() {
		return modes_size;
	}

	public Color getMODES_BG() {
		return MODES_BG;
	}
	
	
	
	
}
