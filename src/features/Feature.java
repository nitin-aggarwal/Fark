package features;

import java.io.File;
import java.io.IOException;

import entities.AbstractDB;

abstract public class Feature {
	abstract public void calculateFeatureVector(AbstractDB object, StringBuilder strPOS, File file) throws IOException;
}
