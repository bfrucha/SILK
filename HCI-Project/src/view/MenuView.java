package view;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.border.LineBorder;

import model.MenuModel;
import fr.lri.swingstates.canvas.Canvas;

public class MenuView extends Canvas 
{
	private static final long serialVersionUID = 1L;
	
	private static MenuModel model;
	
	private int width;
	
	public MenuView (MenuModel m, int width)
	{
		super(width, m.getHeight());
		setBounds(new Rectangle(0, 0, width, m.getHeight()));
		
		setLayout(null);
		
		model = m;
		
		this.width = width;
		
		drawMenu();
		
		validate();
	}
	
	private void drawMenu()
	{
		int nbMenus = model.getMenus().size();
		int menu_width = width/nbMenus;
		int i = 0;
		
		for (Map.Entry<String, ArrayList<String>> e : model.getMenus().entrySet())
		{
			//Menu Canvas
			Canvas m = new Canvas();
			m.setLayout(null);
			m.setBounds(i*menu_width, 0, menu_width, model.getHeight());
			m.setBorder(new LineBorder(Color.BLACK));
			m.newText(10, 20, e.getKey());
			m.setName(e.getKey());
			
			//Submenu Canvases
			int nbSubMenus = e.getValue().size();
			int subMenu_width = menu_width/nbSubMenus;
			int j = 0;
			
			
			for (String sub : e.getValue())
			{
				Canvas s = new Canvas();
				s.setBounds(i*menu_width + j*subMenu_width, model.getHeight(), subMenu_width, model.getHeight());
				s.setBorder(new LineBorder(Color.BLACK));
				s.newText(10, 20, sub);
				s.setName(e.getKey() + " " + sub);
				s.setVisible(false);
				
				//m.add(s);
				add(s);
				j++;
			}
			
			add(m);
			i++;
		}
	}

}
