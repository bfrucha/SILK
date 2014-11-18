package view;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;

import model.PaletteModel;
import model.ProjectModel;
import controller.PaletteController;
import controller.ProjectController;

public class MainScreen extends JFrame {

	private static final long serialVersionUID = 1L;

	
	public MainScreen(int width, int height) {
		super();
		setSize(new Dimension(width, height));	
		setLocation(300, 300);
		setVisible(true);
		setTitle("SILK");
		
		// add a fresh project at the starting
		ProjectController pc = newProject(width, height);
		
		//Palette's initialization
		initPalette(pc);
		
		validate();
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	// creation of a new project
	public ProjectController newProject(int width, int height) {
		ProjectModel projectModel = new ProjectModel();
		ProjectView projectView = new ProjectView(projectModel, width, height);
		
		getContentPane().add(projectView);
		
		return new ProjectController(projectModel, projectView);
	}
	
	private void initPalette(ProjectController pc) {
		JPanel glass = (JPanel)getGlassPane();
		glass.setVisible(true);
		
		PaletteModel m = new PaletteModel();
		PaletteView v = new PaletteView(m);
		PaletteController c = new PaletteController(this, v, m, pc);
		
		glass.add(v);
	}
	
	public static void main(String[] args) {
		new MainScreen(1000, 1000);
	}
}
