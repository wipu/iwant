package org.fluentjava.iwant.core.javafinder;

import java.io.File;

import org.fluentjava.iwant.api.model.ExternalSource;
import org.fluentjava.iwant.api.model.Path;

public class PathToClasspathLocationOf {

	public static Path class_(Class<?> class_) {
		try {
			File loc = new File(class_.getProtectionDomain().getCodeSource()
					.getLocation().toURI());
			return ExternalSource.at(loc);
		} catch (Exception e) {
			throw new IllegalStateException("Failed to find class location", e);
		}
	}

}
