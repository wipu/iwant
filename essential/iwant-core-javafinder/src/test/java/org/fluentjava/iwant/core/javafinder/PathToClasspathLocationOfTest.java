package org.fluentjava.iwant.core.javafinder;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.fluentjava.iwant.api.javamodules.JavaModule;
import org.fluentjava.iwant.api.model.ExternalSource;
import org.fluentjava.iwant.api.model.Path;
import org.junit.Assert;
import org.junit.Test;

public class PathToClasspathLocationOfTest {

	@Test
	public void directoryLocationOfIwantsOwnClass() {
		Class<?> marker = JavaModule.class;

		Path loc = PathToClasspathLocationOf.class_(marker);

		ExternalSource extLoc = (ExternalSource) loc;
		File asFile = new File(extLoc.name());
		assertTrue(asFile.isDirectory());
	}

	@Test
	public void jarLocationOfA3rdPartyClass() {
		Class<?> marker = Assert.class;

		Path loc = PathToClasspathLocationOf.class_(marker);

		ExternalSource extLoc = (ExternalSource) loc;
		File asFile = new File(extLoc.name());
		assertFalse(asFile.isDirectory());
		assertTrue(asFile.getAbsolutePath().endsWith(".jar"));
	}

}
