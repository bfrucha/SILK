package view;

import java.awt.Dimension;

import javax.swing.JFrame;

import controller.ProjectController;
import model.ProjectModel;

public class MainScreen extends JFrame {

	private static final long serialVersionUID = 1L;

	
	public MainScreen(int width, int height) {
		super();
		setSize(new Dimension(width, height));	
		setLocation(300, 300);
		setVisible(true);
		setTitle("SILK");
		
		// add a fresh project at the starting
		newProject(width, height);
		
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
	
	public static void main(String[] args) {
		new MainScreen(1000, 1000);
	}
}
