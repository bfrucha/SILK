package implementation;

import java.io.File;
import java.util.ArrayList;

import view.WidgetTag;
import controller.SketchController;
import fr.lri.swingstates.canvas.CPolyLine;
import fr.lri.swingstates.canvas.CTag;
import model.ProjectModel;

public class Implement 
{
	
	private ProjectModel project;
	private WidgetTag tag;
	
	public Implement(ProjectModel p)
	{
		project = p;
	}
	
	public void createClasses(File folder)
	{
		if (folder.isDirectory())
		{
			//Tout les sketchs du project : 1 sketch -> 1 classe
			ArrayList<SketchController> sketchs = project.getSketches();
			
			//Shapes de chaque sketch
			ArrayList<CPolyLine> shapes;
			//Widgets reconnus de chaque sketch
			ArrayList<CPolyLine> recognizedShapes = new ArrayList<CPolyLine>();
			
			//Pour chaque sketch
			for (SketchController s : sketchs)
			{
				shapes = s.getModel().getShapes();
				for (CPolyLine shape : shapes)
				{
					if (shape.hasTag("Button"))
						recognizedShapes.add(shape);
				}
				
				System.out.println(recognizedShapes.size());
				
				for (CPolyLine sh : recognizedShapes)
					System.out.println(sh.getBoundingBox().getReferenceX() + " " + sh.getBoundingBox().getReferenceY());
				
				recognizedShapes.clear();
			}
		}
		else
			System.err.println("Erreur Implement.java : createClasses : folder n'est pas un dossier");
	}
	
}
