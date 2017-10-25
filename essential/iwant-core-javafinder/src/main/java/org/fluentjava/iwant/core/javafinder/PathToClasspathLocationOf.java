package net.sf.iwant.core.javafinder;

import java.io.File;

import net.sf.iwant.api.model.ExternalSource;
import net.sf.iwant.api.model.Path;

public class PathToClasspathLocationOf {

	public static Path class_(Class<?> class_) {
		try {
			File loc = new File(class_.getProtectionDomain().getCodeSource()
					.getLocation().toURI());
			return new ExternalSource(loc);
		} catch (Exception e) {
			throw new IllegalStateException("Failed to find class location", e);
		}
	}

}
