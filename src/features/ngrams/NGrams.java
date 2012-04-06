package features.ngrams;

import java.io.File;
import java.io.IOException;

import entities.AbstractDB;
import features.Feature;

abstract public class NGrams extends Feature {

	abstract public void calculateFeatureVector(StringBuilder strPOS, AbstractDB article , File file) throws IOException;
	
	public File getFileHandle(String featureDirectory, String fileName)
	{
		 try
		 {
			 StringBuilder parentDirectoryPath =  new StringBuilder(System.getProperty("user.dir")); 
			 parentDirectoryPath.append(File.separator).append("files").append(File.separator).
			 						append(featureDirectory).append(File.separator).append(fileName);
			 File f = new File(parentDirectoryPath.toString());
			 if(f.exists())
				 throw new Exception();
			 f.createNewFile();
			 return f;
		 }
		 catch(Exception e) 
		 {
			 //System.out.println("Exceptione is ="+e.getMessage());
			 return null;
		 }
	}
	
	public static void main(String arg[])
	{
		//File f = getFileHandle("unigramPOS","23.txt");
		//System.out.println(f.getAbsolutePath());
	}
	
}
