package implementation;

import java.io.File;
import java.io.IOException;

import view.MainScreen;
import controller.ProjectController;

public class Run
{
	public Run (ProjectController p, MainScreen m)
	{
		File folder = new File("./tmp/");
		if (!folder.exists())
			folder.mkdirs();
		
		Implement i = new Implement(p);//, m);
		i.createClasses(folder);
		
		Runtime rt = Runtime.getRuntime();
		
		//Compilation
		try {
			System.out.println("javac -sourcepath " + folder.getCanonicalPath() + "/ " + folder.getCanonicalPath() + "/" + p.getSketches().get(0).getName() + ".java");
			rt.exec("javac -sourcepath " + folder.getCanonicalPath() + "/ " + folder.getCanonicalPath() + "/" + p.getSketches().get(0).getName() + ".java").waitFor();
			System.out.println("java -cp " + folder.getCanonicalPath() + "/" + " " + p.getSketches().get(0).getName());
			Process pr = rt.exec("java -cp " + folder.getCanonicalPath() + "/" + " " + p.getSketches().get(0).getName());
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
		
		System.out.println("END");
	}

}
