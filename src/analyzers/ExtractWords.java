package analyzers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import constants.ConfigurationConstants;

public class ExtractWords {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		
		SortedSet<String> map = new TreeSet<String>();
		
		StringBuilder parentDirectoryPath =  new StringBuilder(System.getProperty("user.dir"));
		parentDirectoryPath.append(File.separator).append("files"); 
		
		File uniqueFile = new File(parentDirectoryPath +(new StringBuilder()).append(File.separator).
				append(ConfigurationConstants.UNIQUE_FEATURE_DIRECTORY).append(File.separator).append("stopWords2").toString());
		
		BufferedReader br = null;
		
		// Create a linked list for all the distinct features
		try
		{
			if(uniqueFile.exists())
			{
				br = new BufferedReader(new FileReader(uniqueFile));
				String line;
				while((line = br.readLine())!= null)
				{
					for(String s: line.split("\\s"))
						map.add(s);
				}
				br.close();
			} 
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(uniqueFile+"3"));
		for(String s: map)
		{
			bw.write(s);
			bw.write("\n");
		}
		bw.flush();
		bw.close();
	}

}
