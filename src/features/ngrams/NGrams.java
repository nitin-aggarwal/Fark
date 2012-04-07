package features.ngrams;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import entities.AbstractDB;
import features.Feature;

abstract public class NGrams extends Feature {

	abstract public void calculateFeatureVector(StringBuilder strPOS, AbstractDB article , File file , HashMap<String,Object> uniqueFeatureMap) throws IOException;
	Object dummyObject = new Object();
	public static File getFileHandle(String featureDirectory, String fileName)
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
	
	
	public void updateUniqueFeatureMap(HashMap<String,Integer> map , HashMap<String,Object> uniqueFeatureMap)
	{
		Set<Entry<String,Integer>> entrySet = map.entrySet();
		Iterator<Entry<String,Integer>> iterator = entrySet.iterator();

		while(iterator.hasNext())
		{
			Entry entry = iterator.next();
			if(uniqueFeatureMap.get(entry.getKey()) == null)
				uniqueFeatureMap.put((String)entry.getKey(), dummyObject);
		}
	}
	
	public static void main(String arg[])
	{
		//File f = getFileHandle("unigramPOS","23.txt");
		//System.out.println(f.getAbsolutePath());
	}
	
}
