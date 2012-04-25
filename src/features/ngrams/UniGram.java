package features.ngrams;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import constants.ConfigurationConstants;
import entities.AbstractDB;

/*
 * This class is for features computation related to
 * Uni-gram POS
 */
public class UniGram extends NGrams {

	private static UniGram uniGram = null;

	private UniGram() {
	}

	public static UniGram getInstance() {
		if (uniGram == null)
			uniGram = new UniGram();
		return uniGram;
	}

	/**
	 * Compute UNIGRAM POS features for an POS tagged article content strPOS, 
	 * and create a file in folder unigramPOS
	 */
	public void calculateFeatureVector(AbstractDB object, StringBuilder strPOS, File file) throws IOException {

		HashMap<String, Integer> map = new HashMap<String, Integer>();
		Integer count;
		String temp = strPOS.toString().trim();
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(file));

			if (ConfigurationConstants.debugMode)
				System.out.println(strPOS);
			String[] spacesSplitter = temp.split("\\s");

			for (String spaceSeperated : spacesSplitter) {
				String[] splitter = spaceSeperated.split("/");
				if (splitter.length == 2) {
					String tag = splitter[1].toUpperCase();
					if ((count = map.get(tag)) != null)
						map.put(tag, ++count);
					else
						map.put(tag, 1);
				}
			}

			Set<Entry<String, Integer>> entrySet = map.entrySet();
			Iterator<Entry<String, Integer>> iterator = entrySet.iterator();

			while (iterator.hasNext()) {
				Entry<String, Integer> entry = iterator.next();
				if (ConfigurationConstants.debugMode)
					System.out.println(entry.getKey() + " " + entry.getValue());

				bw.write(entry.getKey() + " " + entry.getValue());
				bw.write("\n");
			}
			bw.flush();
		} catch (IOException e) {
			System.out.println("Exception Inside Unigram Feature Computation");
			e.printStackTrace();
		} finally {
			bw.close();
		}
	}
}
