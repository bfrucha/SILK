package implementation;

import java.io.File;
import java.io.IOException;

import controller.ProjectController;

public class Run
{
	
	public Run(File folder, ProjectController p)
	{
		if (folder.isDirectory())
		{
			Implement i = new Implement(p);
			i.createClasses(folder);
			
			
			Runtime rt = Runtime.getRuntime();
			
			//Compilation
			try {
				System.out.println("javac " + folder.getAbsolutePath() + "/" + p.getModel().getSketches().get(0).getModel().getName() + ".java");
				rt.exec("javac " + folder.getAbsolutePath() + "/" + p.getModel().getSketches().get(0).getModel().getName() + ".java").waitFor();
				
				System.out.println("java " + folder.getAbsolutePath() + "/" + p.getModel().getSketches().get(0).getModel().getName());
				rt.exec("cd "+ folder.getAbsolutePath() + "/");
				rt.exec("java " + p.getModel().getSketches().get(0).getModel().getName());
				
			} catch (IOException e) {
				//e.printStackTrace();
				System.err.println("Erreur Run.java : erreur lors de la compilation");
			} catch (InterruptedException e) {
				//e.printStackTrace();
				System.err.println("Erreur Run.java : erreur lors de l'attente de la fin de la compilation");
			}
			
		}
		else
			System.err.println("Erreur Run.java : Run() : folder n'est pas un dossier");
	}

}