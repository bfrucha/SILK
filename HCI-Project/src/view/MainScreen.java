package view;

import implementation.Implement;
import implementation.Run;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.text.IconView;

import model.PaletteModel;
import model.ProjectModel;
import controller.ActionList;
import controller.PaletteController;
import controller.ProjectController;

public class MainScreen extends JFrame {

	private static final long serialVersionUID = 1L;
	
	public static UndoButton undo;
	public static RedoButton redo;
	
	private JMenuBar menuBar;
	
	ProjectController pc;
	
	public MainScreen(int width, int height) {
		super();

		setSize(new Dimension(width, height));
		setLocation(300, 300);
		setVisible(true);
		setTitle("SILK");
		
		// add a fresh project at the starting
		pc = newProject(width, height);
		
		//Menu initialization
		initMenu(pc);
		
		//Palette's initialization
		initPalette(pc);
		
		validate();
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		addComponentListener(new ComponentListener() {
			public void componentResized(ComponentEvent event) {
				pc.resize(getWidth(), getHeight());
			}

			@Override
			public void componentHidden(ComponentEvent arg0) {}

			@Override
			public void componentMoved(ComponentEvent arg0) {}

			@Override
			public void componentShown(ComponentEvent arg0) {}
		});
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
		menuBar = new JMenuBar();
		menuBar.setLayout(new BorderLayout());
		menuBar.setBackground(new Color(238, 238, 238));
		
		JMenu impl = new JMenu("Build/Run");
		JMenuItem build = new JMenuItem("Write java classes");
		
		build.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Implement i = new Implement(pc, MainScreen.this);
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
				Run r = new Run(pc, MainScreen.this);
				r.start();
			}
		});
	
		initUndoRedo(pc, menuBar);
		
		build.setEnabled(true);
		bar.setEnabled(true);
		impl.add(build);
		impl.add(bar);
		menuBar.add(impl, BorderLayout.WEST);
		setJMenuBar(menuBar);
	}
	
	public int getMenuBarHeight() {
		return menuBar.getHeight();
	}
	
	/* init UNDO/REDO buttons */
	public void initUndoRedo(final ProjectController controller, JMenuBar menuBar) {
		JPanel buttons = new JPanel();
		buttons.setLayout(new GridLayout(1, 2, 5, 0));
		
		undo = new UndoButton();
		buttons.add(undo);
		
		redo = new RedoButton();
		buttons.add(redo);
		
		menuBar.add(buttons, BorderLayout.EAST);
		
		undo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				controller.undo();
			}
		});
		
		redo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				controller.redo();
			}
		});
		
		setButtonsList(controller.getActionList());
	}
	
	public static void setButtonsList(ActionList list) {
		undo.setList(list);
		redo.setList(list);
		
		checkButtons();
	}
	
	// enabled buttons
	public static void checkButtons() {
		undo.checkList();
		redo.checkList();
	}

	public static void main(String[] args) {
		new MainScreen(1000, 1000);
	}
	
	
	/* other classes */
	public class Button extends JButton {
		
		private ImageIcon activeIcon;
		private ImageIcon inactiveIcon;
	
		
		public Button(String active, String inactive) {
			super();
			
			activeIcon = new ImageIcon(active);
			inactiveIcon = new ImageIcon(inactive);
			
			setBackground(new Color(238, 238, 238));
			setActive(false);
			setBorder(null);
		}
		
		public void setActive(boolean active) {
			if(active) { 
				setIcon(activeIcon);
				setEnabled(true);
			}
			else { 
				setIcon(inactiveIcon);
				setEnabled(false);
			}
		}
	}
	
	public class UndoButton extends Button {
		
		// current list used
		private ActionList list;
		
		public UndoButton() {
			super("images/undo_active.png", "images/undo_inactive.png");
			
			addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					checkButtons();
				}
			});
		}
		
		public void setList(ActionList list) {
			this.list = list;
			
			checkList();
		}
		
		public void checkList() {
			setActive(!list.firstAction());
		}
		
	}
	
	public class RedoButton extends Button {
		
		// current list used
		private ActionList list;
		
		public RedoButton() {
			super("images/redo_active.png", "images/redo_inactive.png");
			
			addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					checkButtons();
				}
			});
		}
		
		public void setList(ActionList list) {
			this.list = list;
			
			checkList();
		}
		
		public void checkList() {
			setActive(!list.lastAction());
		}
		
	}
}
