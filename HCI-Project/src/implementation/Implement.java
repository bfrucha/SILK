package implementation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map.Entry;

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
				
				String sketchName = s.getModel().getName().replaceAll("\\s+","");
				
				//Creation du fichier
				File classe = new File(folder + "/" + sketchName + ".java");
				
				//Pour ecrire dans ce fichier
				try 
				{
					FileWriter fw = new FileWriter(classe);
					
					//Ecriture du string
					fw.write(toWrite(s, widgets, sketchName));
					
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
	private String toWrite(SketchController s, ArrayList<WidgetController> widgets, String sketchName)
	{
		//Préparation du string a ecrire dans le fichier
		String toWrite = "";
		
		//Ajout des imports
		toWrite += "import java.awt.Dimension;\nimport java.awt.Rectangle;\nimport java.awt.event.ActionEvent;\nimport java.awt.event.ActionListener;\n";
		toWrite += "import javax.swing.JButton;\nimport javax.swing.JFrame;\n";
		
		//Declaration de la classe
		toWrite += "public class "+ sketchName +" extends JFrame implements ActionListener{\n";
		
		//Déclaration des boutons
		int i = 1;
		for (WidgetController w : widgets)
		{
			toWrite += "\tprivate JButton btn"+ i +";\n";
			i++;
		}
		
		toWrite += "\n";
		
		//Constructeur
		toWrite += "\tpublic "+ sketchName +"(){\n";
		toWrite += "\t\tsuper();\n\t\tsetTitle(\""+ sketchName+ "\");\n\t\tsetLayout(null);\n\t\tsetVisible(true);\n";
		toWrite += "\t\tsetSize(new Dimension("+(int)s.getModel().getSize().getWidth() + ","+ (int)s.getModel().getSize().getHeight() +"));\n\t\tsetLocation(100, 100);\n\n";
		
		i = 1;
		for (WidgetController w : widgets)
		{
			toWrite += "\t\t" + "btn"+i+" = new JButton();\n";
			toWrite += "\t\t" + "btn"+i+".setBounds(new Rectangle("+(int)w.getBounds().getX() + ","+(int)w.getBounds().getY() + "," + (int)w.getBounds().getWidth() + "," + (int)w.getBounds().getHeight() +"));\n";
			toWrite += "\t\t" + "btn"+i+".addActionListener(this);\n";
			toWrite += "\t\t" + "add(btn"+i+");\n\n";
			i++;
		}
		
		toWrite += "\t}\n";
		//Fin constructeur
		
		//ActionPerformed
		toWrite += "\n\tpublic void actionPerformed(ActionEvent e) {\n";
		
		if (!interactions.getModel().getInteractions().isEmpty()) //Si il y a des interactions
		{
			for (Entry<WidgetController, SketchController> e : interactions.getModel().getInteractions().entrySet())
			{
				toWrite += "\t\t" + "if (e.getSource() == btn"+widgets.indexOf(e.getKey())+1 + "){\n";
				toWrite += "\t\t\t" + e.getValue().getModel().getName() + " frame = new " + e.getValue().getModel().getName() + "();\n";
				toWrite += "\t\t" + "}\n";
			}
			
			toWrite += "\t\tdispose();\n";
		}
		
		toWrite += "\t}\n";
		//Fin ActionPerformed
		
		toWrite += "\n";
		
		//Main
		toWrite += "\tpublic static void main(String[] args) {\n\t\tnew "+sketchName+"();\n\t}\n";
		//Fin main
		
		toWrite += "\n}";
		//Fin
		
		System.out.println(toWrite);
		
		return toWrite;
	}
	
	/*private ArrayList<String> getAllDiffSketchs()
	{
		ArrayList<String> sketchs = new ArrayList<String>();
		
		for (Entry<WidgetController, SketchController> s : interactions.getModel().getInteractions().entrySet())
			if (!sketchs.contains(s.getValue().getModel().getName()))
				sketchs.add(s.getValue().getModel().getName());
		
		return sketchs;
	}*/
}
