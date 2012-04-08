import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import constants.ConfigurationConstants;


public class CalculateUniqueFeature {

	private HashMap<String,Object> map = new HashMap<String, Object>();
	Object dummyObject = new Object();
	public void readFromFile(String folder)
	{
		StringBuilder parentDirectoryPath =  new StringBuilder(System.getProperty("user.dir"));
		parentDirectoryPath.append(File.separator).append("files"); 
		File uniqueFeatureFile = new File(parentDirectoryPath +(new StringBuilder()).append(File.separator).
				append(ConfigurationConstants.UNIQUE_FEATURE_DIRECTORY).append(File.separator).append(folder).toString());
		BufferedReader br = null;
		
		try
		{
			if(uniqueFeatureFile.exists())
			{
				br = new BufferedReader(new FileReader(uniqueFeatureFile));
				String line;
				while((line = br.readLine())!= null)
				{
						map.put(line, dummyObject);
				}
				br.close();
			} 
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		File f = new File(parentDirectoryPath.append(File.separator).append(folder).append(File.separator).toString());
		System.out.println(f.getAbsolutePath());
		try
		{
			int count = 0;
			System.out.println(f.list().length);
			for(File file:f.listFiles())
			{
				br = new BufferedReader(new FileReader(file));
				String line;
				while((line = br.readLine())!= null)
				{
					String[] words = line.split("\\s");
					StringBuilder feature = new StringBuilder();
					for(int i = 0 ; i < words.length-2;i++)
					{
						feature.append(words[i]+" ");
					}
					feature.append(words[words.length-2]);
					if(map.get(feature.toString()) == null)
					{
						map.put(feature.toString(), dummyObject);
					}
				}
				br.close();
				System.out.println(++count);
			}	
			BufferedWriter bw = null;
			bw = new BufferedWriter(new FileWriter(uniqueFeatureFile));
			copyIntoFile(map,bw);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	public void copyIntoFile(HashMap<String,Object> obj, BufferedWriter bw)
	{
		Set<String> entrySet = map.keySet();
		Iterator<String> iterator = entrySet.iterator();

		try
		{
			while(iterator.hasNext())
			{
				String feature = iterator.next();
				bw.write(feature);
				bw.write("\n");
			}
			bw.flush();
			bw.close();
			
		}
		catch(Exception e)
		{
			
		}
		
	}
	
	
	public static void main(String args[])
	{
		CalculateUniqueFeature obj = new CalculateUniqueFeature();
		for(String folder: ConfigurationConstants.FOLDER_NAMES)
		{
			obj.readFromFile(folder);
			obj.map.clear();
		}
	}
	
}
