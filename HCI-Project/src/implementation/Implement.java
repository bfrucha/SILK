package implementation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import view.WidgetTag;
import controller.InteractionsController;
import controller.ProjectController;
import controller.SketchController;
import controller.WidgetController;
import fr.lri.swingstates.canvas.CPolyLine;

public class Implement 
{
	
	private ProjectController project;
	private InteractionsController interactions;
	private WidgetTag tag;
	
	public Implement(ProjectController p)
	{
		project = p;
		interactions = project.getInteractionsController();
	}
	
	public void createClasses(File folder)
	{
		if (folder.isDirectory())
		{
			//Tout les sketchs du project : 1 sketch -> 1 classe
			ArrayList<SketchController> sketchs = project.getModel().getSketches();
			
			//Shapes de chaque sketch
			ArrayList<CPolyLine> shapes;
			
			//Pour chaque sketch
			for (SketchController s : sketchs)
			{
				//On récupère les widgets identifiés de ce sketch
				ArrayList<WidgetController> widgets = s.getModel().getWidgets();
				
				String sketchName = s.getModel().getName();
				
				//Préparation du string a ecrire dans le fichier
				String toWrite = "";
				
				//Ajout des imports
				toWrite += "import java.awt.Dimension;\nimport java.awt.Rectangle;\nimport java.awt.event.ActionEvent;\nimport java.awt.event.ActionListener;\n";
				toWrite += "import javax.swing.JButton;\nimport javax.swing.JFrame;";
				
				//Declaration de la classe
				toWrite += "public class "+ sketchName +" extends JFrame implements ActionListener{\n";
				
				//Déclaration des boutons
				int i = 1;
				for (WidgetController w : widgets)
				{
					toWrite += "\tprivate JButton btn"+ i +";\n";
					i++;
				}
				
				//Constructeur
				toWrite += "\tpublic "+ sketchName +"(){\n";
				toWrite += "\t\tsuper();\n\t\tsetTitle(\""+ sketchName+ "\");\n\t\tsetLayout(null);\n\t\tsetVisible(true);\n";
				toWrite += "\t\tsetSize(new Dimension("+s.getModel().getSize()+"));\n\t\tsetLocation(100, 100);\n\n";
				
				i = 1;
				for (WidgetController w : widgets)
				{
					toWrite += "\t\t" + "btn"+i+" = new JButton();\n";
					toWrite += "\t\t" + "btn"+i+".setBounds(new Rectangle("+w.getBounds()+"));\n";
					toWrite += "\t\t" + "btn"+i+".addActionListener(this);\n";
					toWrite += "\t\t" + "add(btn"+i+");\n\n";
					i++;
				}
				
				toWrite += "\t}\n";
				//Fin constructeur
				
				//ActionPerformed
				/*	public void actionPerformed(ActionEvent e) 
					{
						<SketchName2> t;
						
						<if (e.getSource() == btn1)
							t = new <SketchName2>();>//si interaction sur ce bouton, * nb d'interaction
						
						dispose();
					}*/
				toWrite += "public void actionPerformed(ActionEvent e) {\n";
				toWrite += "";
				//Fin ActionPerformed
				
				//Fichier où l'on écrit la classe
				File classe = new File(folder + File.pathSeparator + sketchName + ".java");
				//Pour ecrire dans ce fichier
				try 
				{
					FileWriter fw = new FileWriter(classe);
					
					//
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
	
}
