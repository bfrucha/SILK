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
			ArrayList<SketchController> sketchs = project.getSketches();
			
			//Si tout les sketchs n'ont pas un nom différent
			if (!allDiffName(sketchs))
			{
				System.err.println("Erreur : Implement.java : createClasses : tout les sketchs n'ont pas un nom différent !");
				return;
			}
			
			//Shapes de chaque sketch
			ArrayList<CPolyLine> shapes;
			
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
		
		//On récupère les widgets identifiés de ce sketch
		ArrayList<WidgetController> widgets = s.getWidgets();
		
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
		toWrite += "\t\tsetSize(new Dimension("+(int)s.getSize().getWidth() + ","+ (int)s.getSize().getHeight() +"));\n\t\tsetLocation(100, 100);\n\n";
		
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
		
		if (!interactions.getInteractions().isEmpty()) //Si il existe des interactions
		{
			for (Entry<WidgetController, SketchController> e : interactions.getInteractions().entrySet())
			{
				int idWid = widgets.indexOf(e.getKey()) + 1;
				if (idWid != 0)
				{
					toWrite += "\t\t" + "if (e.getSource() == btn"+ idWid + "){\n";
					toWrite += "\t\t\t" + e.getValue().getName().replaceAll("\\s+","") + " frame = new " + e.getValue().getName().replaceAll("\\s+","") + "();\n";
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
	
	/*private ArrayList<String> getAllDiffSketchs()
	{
		ArrayList<String> sketchs = new ArrayList<String>();
		
		for (Entry<WidgetController, SketchController> s : interactions.getModel().getInteractions().entrySet())
			if (!sketchs.contains(s.getValue().getModel().getName()))
				sketchs.add(s.getValue().getModel().getName());
		
		return sketchs;
	}*/
}
