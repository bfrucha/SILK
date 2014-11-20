package view;

import implementation.Implement;
import implementation.Run;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
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
		
		//Menu initialization
		initMenu(pc);
		
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
		new PaletteController(this, v, m, pc);
		
		glass.add(v);
	}

	
	private void initMenu(final ProjectController pc) {
		JMenuBar menuBar = new JMenuBar();
		JMenu impl = new JMenu("Build/Run");
		JMenuItem build = new JMenuItem("Write java classes");
		
		build.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Implement i = new Implement(pc);
				//"/Users/lsaublet/Desktop/ProjectSilkOutput/"
				JFileChooser f = new JFileChooser(new java.io.File("."));
				f.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				
				int valR = f.showOpenDialog(MainScreen.this);
				if (valR == JFileChooser.APPROVE_OPTION)
					i.createClasses(new File(f.getSelectedFile().getAbsolutePath()));
			}
		});
			
		// affectation du raccourci alt+m Plutot cool !
		//build.setMnemonic(KeyEvent.VK_M);
		JMenuItem bar = new JMenuItem("Build&Run");
		bar.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser f = new JFileChooser(new java.io.File("."));
				f.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int valR = f.showOpenDialog(MainScreen.this);
				if (valR == JFileChooser.APPROVE_OPTION) {
					Run r = new Run(new File(f.getSelectedFile().getAbsolutePath()), pc);
				}
			}
		});
		
		build.setEnabled(true);
		bar.setEnabled(true);
		impl.add(build);
		impl.add(bar);
		menuBar.add(impl);
		setJMenuBar(menuBar);
	}

	public static void main(String[] args) {
		new MainScreen(1000, 1000);
	}
}