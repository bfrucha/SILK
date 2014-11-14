package model;

import java.util.ArrayList;
import java.util.TreeMap;

public class MenuModel {
	
	//Height of the menu
	private final static int HEIGHT = 50;
	
	private TreeMap<String, ArrayList<String>> menus;
	
	public MenuModel()
	{
		menus = new TreeMap<String, ArrayList<String>>();
		
		//Menu initialisation
		ArrayList<String> osm = new ArrayList<String>();
		osm.add("Sketch");
		osm.add("Project");
		menus.put("Open", osm);

		ArrayList<String> ssm = new ArrayList<String>();
		ssm.add("Sketch");
		ssm.add("Project");
		menus.put("Save", ssm);
		
		ArrayList<String> sesm = new ArrayList<String>();
		sesm.add("Sketch");
		sesm.add("Widget");
		menus.put("Search", sesm);
		
		System.out.println(menus);

	}
	
	public TreeMap<String, ArrayList<String>> getMenus() {
		return menus;
	}

	public int getHeight() {
		return HEIGHT;
	}
	
	
}
