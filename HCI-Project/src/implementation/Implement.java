package implementation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import model.WidgetModel;
import view.MainScreen;
import view.SketchView;
import view.WidgetView;
import controller.InteractionsController;
import controller.ProjectController;
import controller.SketchController;
import controller.WidgetController;
import fr.lri.swingstates.canvas.CPolyLine;

public class Implement 
{
	
	private ProjectController project;
	private InteractionsController interactions;
	private HashMap<WidgetController, Object> inters;
	private boolean intersChanged = false;
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
	
	//Methodes qui cherche si des widgets sont alignés
	private ArrayList<WidgetController> prepareWidgets(SketchController s)
	{
		LinkedList<WidgetController> list = new LinkedList<WidgetController>();
		LinkedList<WidgetController> listRest = new LinkedList<WidgetController>();
		
		list.addAll(s.getWidgets());
		listRest.addAll(s.getWidgets());
		
		ArrayList<WidgetController> newWidgets = new ArrayList<WidgetController>();
		ArrayList<WidgetController> temp = new ArrayList<WidgetController>();
		
		inters = new HashMap<WidgetController, Object>();
		inters.putAll(interactions.getInteractions());
		
		int distance = 15;
		
		double xCurr, yCurr, wCurr, hCurr, pCurr;
		double xCheck, yCheck, wCheck, hCheck, pCheck;
		double xNew, yNew, wNew, hNew, pNew;
		
		boolean aligne = false, wOk = false;
		
		for (WidgetController widget : list)
		{
			if (listRest.contains(widget))
			{
				xCurr = widget.getBounds().getX();
				yCurr = widget.getBounds().getY();
				wCurr = widget.getBounds().getWidth();
				hCurr = widget.getBounds().getHeight();
				pCurr = (wCurr + hCurr) * 2;
				
				for (WidgetController widgetCheck : list)
				{
					if (listRest.contains(widgetCheck) && widget != widgetCheck && widgetCheck.getType() == widget.getType()) //Si les widgets ont le meme type
					{
						xCheck = widgetCheck.getBounds().getX();
						yCheck = widgetCheck.getBounds().getY();
						wCheck = widgetCheck.getBounds().getWidth();
						wNew = wCheck;
						hCheck = widgetCheck.getBounds().getHeight();
						pCheck = (wCheck + hCheck) * 2;
						
						//Check aligné en x1
						if (Math.abs(xCurr - xCheck) < distance)// && Math.abs((yCurr + hCurr) - (yCheck + hCheck)) < distance)
						{
							xNew = xCurr;
							aligne = true;
						}
						else
							xNew = xCheck;
						//check aligné en x2
						/*double diff = Math.abs((xCheck+wCheck)-(xCurr+wCurr));
						if (diff < distance)
						{	
							if (aligne)
							{
								if (wCurr > wCheck)
									wNew = wCurr - diff;
								else
									wNew = wCheck - diff;
								
								wOk = true;
							}
							else
							{
								xNew = xCheck + diff;
							}
						}*/
						//check aligné 
						if(Math.abs(yCurr - yCheck) < distance)
						{
							yNew = yCurr;
							aligne = true;
						}
						else
							yNew = yCheck;
						
						if (Math.abs(pCurr - pCheck) < distance*2 && aligne)
						{
							hNew = hCurr;
							if (!wOk)
								wNew = wCurr;
							aligne = true;
						}
						else
						{
							hNew = hCheck;
							wNew = wCheck;
						}
						
						if (aligne)
						{
							WidgetController newWidget = createWidget(widgetCheck.getType(), xNew, yNew, wNew, hNew, widgetCheck.getGhost(), s);
							temp.add(newWidget);
							listRest.remove(widgetCheck);
							if (inters.containsKey(widgetCheck))
							{
								inters.put(newWidget, inters.get(widgetCheck));
								inters.remove(widgetCheck);
								intersChanged = true;
							}
						}
						
						aligne = false;
					}
				}
				newWidgets.add(widget);
				newWidgets.addAll(temp);
				temp.clear();
			}
		}
		
		return newWidgets;
	}
	
	//Méthode créant un widget
	private WidgetController createWidget(int type, double x, double y, double w, double h, CPolyLine ghost, SketchController sketch)
	{
		WidgetModel model = new WidgetModel(type, x, y, w, h);
		WidgetView view = new WidgetView(model);
		
		WidgetController controller = new WidgetController(sketch, model, view, ghost);
		
		return controller;
	}

	//Methode consturisant le string a écrire dans le fichier .java
	//Ne gere que les bouttons pour l'instant
	private String toWrite(SketchController s, String sketchName)
	{
		int i;
		
		//On récupère les widgets identifiés de ce sketch
		//LinkedList<WidgetController> widgets = s.getWidgets();
		ArrayList<WidgetController> widgets = prepareWidgets(s);
		
		//Préparation du string a ecrire dans le fichier
		String toWrite = "";
		
		HashMap<WidgetController, Object> tempInters = new HashMap<WidgetController, Object>();
		if (intersChanged)
			tempInters.putAll(inters);
		else
			tempInters.putAll(interactions.getInteractions());
		
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
		/*Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize(); //taille de l'écran
		Dimension mainscreenSize = mainscreen.getSize();
		int x = (int)((s.getLocation().getX() / mainscreenSize.width) * screenSize.width);
		int y = (int)((s.getLocation().getY() / mainscreenSize.height) * screenSize.height);*/
		int x = 300, y = 100;
		toWrite += "\t\tsetLocation("+ x +", "+ y +");\n\n";
		
		i = 1;
		for (WidgetController w : widgets)
		{
			if (w.getModel().getType() == 0) // Btn
			{
				toWrite += "\t\t" + "btn"+i+" = new JButton();\n";
				if (tempInters.get(w) != null)
					toWrite += "\t\t" + "btn"+i+".setText(\" to " + ((SketchController) tempInters.get(w)).getName() + "\");\n";
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
		
		toWrite += "\n\t\trepaint();\n";
		
		toWrite += "\t}\n";
		//Fin constructeur
		
		//ActionPerformed
		toWrite += "\n\tpublic void actionPerformed(ActionEvent e) {\n";
		
		if (!tempInters.isEmpty()) //Si il existe des interactions
		{
			for (Entry<WidgetController, Object> e : tempInters.entrySet())
			{
				int idWid = widgets.indexOf(e.getKey()) + 1;
				if (idWid != 0)
				{
					toWrite += "\t\t" + "if (e.getSource() == btn"+ idWid + "){\n";
					if (((SketchController)e.getValue()).getName().equals(sketchName))
						toWrite += "\t\t\tdispose();";
					else
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
