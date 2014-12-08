package implementation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextField;

import view.MainScreen;
import view.SketchView;
import controller.InteractionsController;
import controller.ProjectController;
import controller.SketchController;
import controller.WidgetController;

public class Implement 
{
	
	private ProjectController project;
	private InteractionsController interactions;
	private MainScreen mainscreen;
	
	public Implement(ProjectController p, MainScreen m)
	{
		project = p;
		mainscreen = m;
		
		interactions = project.getInteractionsController();
	}
	
	public void createClasses(File folder)
	{
		if (folder.isDirectory())
		{
			//Tout les sketchs du project : 1 sketch -> 1 classe
			ArrayList<SketchController> sketchs = project.getSketches();
			
			//Si tout les sketchs n'ont pas un nom différent
			if (!allDiffName(sketchs))
			{
				System.err.println("Erreur : Implement.java : createClasses : tout les sketchs n'ont pas un nom différent !");
				return;
			}

			//Pour chaque sketch
			for (SketchController s : sketchs)
			{
				String sketchName = s.getName().replaceAll("\\s+","");
				
				//Creation du fichier
				File classe = new File(folder + "/" + sketchName + ".java");
				
				//Pour ecrire dans ce fichier
				try 
				{
					FileWriter fw = new FileWriter(classe);
					
					//Ecriture du string
					fw.write(toWrite(s, sketchName));
					
					fw.close();
				} 
				catch (IOException e) 
				{
					System.err.println("Erreur Implement.java : createClasses : erreur lors de la création du FileWriter");
				}
				
				
			}
		}
		else
			System.err.println("Erreur Implement.java : createClasses : folder n'est pas un dossier");
	}

	//Methode consturisant le string a écrire dans le fichier .java
	//Ne gere que les bouttons pour l'instant
	private String toWrite(SketchController s, String sketchName)
	{
		int i;
		
		//On récupère les widgets identifiés de ce sketch
		LinkedList<WidgetController> widgets = s.getWidgets();
		
		//Préparation du string a ecrire dans le fichier
		String toWrite = "";
		
		//Ajout des imports
		toWrite += "import java.awt.Dimension;\nimport java.awt.Rectangle;\nimport java.awt.event.ActionEvent;\nimport java.awt.event.ActionListener;\n";
		toWrite += "import javax.swing.JButton;\nimport javax.swing.JFrame;\nimport javax.swing.JPanel;\nimport javax.swing.JTextField;\nimport java.awt.Color;\nimport javax.swing.BorderFactory;\n\n";
		
		//Declaration de la classe
		toWrite += "public class "+ sketchName +" extends JFrame implements ActionListener{\n\n";
		
		i=1;
		//Déclaration des boutons
		for (WidgetController w : widgets)
		{
			if (w.getModel().getType() == 0)
				toWrite += "\tprivate JButton btn"+ i +";\n";
			else if (w.getModel().getType() == 1)
				toWrite += "\tprivate JTextField txt" + i +";\n";
			else if(w.getModel().getType() == 2)
				toWrite += "\tprivate JPanel panel" + i + ";\n";
			i++;
		}
		
		toWrite += "\n";
		
		//Constructeur
		toWrite += "\tpublic "+ sketchName +"(){\n";
		toWrite += "\t\tsuper();\n\t\tsetTitle(\""+ sketchName+ "\");\n\t\tsetLayout(null);\n\t\tsetVisible(true);\n";
		toWrite += "\t\tsetSize(new Dimension("+(int)s.getSize().getWidth() + ","+ (int)(s.getSize().getHeight() - SketchView.TB_HEIGHT/2) +"));\n";
		
		//Determine la position du sketch dans l'interface pour la mapper à l'écran
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize(); //taille de l'écran
		Dimension mainscreenSize = mainscreen.getSize();
		int x = (int)((s.getLocation().getX() / mainscreenSize.width) * screenSize.width);
		int y = (int)((s.getLocation().getY() / mainscreenSize.height) * screenSize.height);
		toWrite += "\t\tsetLocation("+ x +", "+ y +");\n\n";
		
		i = 1;
		for (WidgetController w : widgets)
		{
			if (w.getModel().getType() == 0) // Btn
			{
				toWrite += "\t\t" + "btn"+i+" = new JButton();\n";
				if (interactions.getInteractions().get(w) != null)
					toWrite += "\t\t" + "btn"+i+".setText(\" to " + ((SketchController) interactions.getInteractions().get(w)).getName() + "\");\n";
				toWrite += "\t\t" + "btn"+i+".setBounds(new Rectangle("+(int)w.getBounds().getX() + ","+(int)(w.getBounds().getY()-SketchView.TB_HEIGHT) + "," + (int)w.getBounds().getWidth() + "," + (int)w.getBounds().getHeight() +"));\n";
				toWrite += "\t\t" + "btn"+i+".addActionListener(this);\n";
				toWrite += "\t\t" + "add(btn"+i+");\n\n";
			}
			else if (w.getModel().getType() == 1) //TextField
			{
				toWrite += "\t\t" + "txt"+i + " = new JTextField();\n";
				toWrite += "\t\t" + "txt"+i + ".setVisible(true);\n";
				toWrite += "\t\t" + "txt"+i +".setBounds(new Rectangle("+(int)w.getBounds().getX() + ","+(int)(w.getBounds().getY()-SketchView.TB_HEIGHT) + "," + (int)w.getBounds().getWidth() + "," + (int)w.getBounds().getHeight() +"));\n";
				toWrite += "\t\t" + "add(txt"+i+");\n\n";
			}
			else if (w.getModel().getType() == 2) //Panel
			{
				toWrite += "\t\t" + "panel"+i + " = new JPanel();\n";
				toWrite += "\t\t" + "panel"+i +".setBounds(new Rectangle("+(int)w.getBounds().getX() + ","+(int)(w.getBounds().getY()-SketchView.TB_HEIGHT) + "," + (int)w.getBounds().getWidth() + "," + (int)w.getBounds().getHeight() +"));\n";
				toWrite += "\t\t" + "panel"+i +".setBorder(BorderFactory.createLineBorder(Color.black));\n";
				toWrite += "\t\t" + "panel"+i +".setOpaque(false);\n";
				toWrite += "\t\t" + "add(panel"+i+");\n\n";
			}
			
			i++;
		}
		
		toWrite += "\n\t\tvalidate();\n";
		
		toWrite += "\t}\n";
		//Fin constructeur
		
		//ActionPerformed
		toWrite += "\n\tpublic void actionPerformed(ActionEvent e) {\n";
		
		if (!interactions.getInteractions().isEmpty()) //Si il existe des interactions
		{
			for (Entry<WidgetController, Object> e : interactions.getInteractions().entrySet())
			{
				int idWid = widgets.indexOf(e.getKey()) + 1;
				if (idWid != 0)
				{
					toWrite += "\t\t" + "if (e.getSource() == btn"+ idWid + "){\n";
					toWrite += "\t\t\t" + ((SketchController) e.getValue()).getName().replaceAll("\\s+","") + " frame = new " + ((SketchController) e.getValue()).getName().replaceAll("\\s+","") + "();\n";
					toWrite += "\t\t" + "}\n";
				}
			}
			
			toWrite += "\t\tdispose();\n";
		}
		
		toWrite += "\t}\n";
		//Fin ActionPerformed
		
		toWrite += "\n";
		
		//Main
		toWrite += "\tpublic static void main(String[] args)\n\t{\n\t\tnew "+sketchName+"();\n\t}\n";
		//Fin main
		
		toWrite += "\n}";
		//Fin
		
		return toWrite;
	}
	
	//Méthode permettant de savoir si il existe 2 sketchs de même nom
	private boolean allDiffName(ArrayList<SketchController> sketchs)
	{
		boolean allDiff = true;
		ArrayList<String> list = new ArrayList<String>();
		
		for (SketchController s : sketchs)
		{
			if (list.contains(s.getName()))
			{
				allDiff = false;
				break;
			}
			list.add(s.getName());
		}
		
		return allDiff;
	}
}
