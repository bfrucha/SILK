package implementation;

import java.io.File;
import java.io.IOException;

import view.MainScreen;
import controller.ProjectController;

public class Run extends Thread
{
	ProjectController proj;
	MainScreen main;
	
	public Run (ProjectController p, MainScreen m)
	{
		proj = p;
		main = m;
	}
	
	@Override
	public void run() {
		File folder = new File("./tmp/");
		if (!folder.exists())
			folder.mkdirs();
		
		Implement i = new Implement(proj, main);
		i.createClasses(folder);
		
		Runtime rt = Runtime.getRuntime();
		
		//Compilation
		try {
			rt.exec("javac -sourcepath " + folder.getCanonicalPath() + "/ " + folder.getCanonicalPath() + "/" + proj.getHomeSketch().getName() + ".java").waitFor();
			Process pr = rt.exec("java -cp " + folder.getCanonicalPath() + "/" + " " + proj.getHomeSketch().getName());
			pr.waitFor();
		}
		catch (IOException e)
		{
			System.err.println("Erreur Run.java : erreur lors de la compilation");
		}
		catch (InterruptedException e)
		{
			System.err.println("Erreur Run.java : erreur lors de l'attente de la fin de la compilation");
		}
		
		for (File f : folder.listFiles())
			f.delete();
	}

}
