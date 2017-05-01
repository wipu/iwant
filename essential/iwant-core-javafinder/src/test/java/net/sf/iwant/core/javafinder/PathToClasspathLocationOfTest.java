package net.sf.iwant.core.javafinder;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import net.sf.iwant.api.javamodules.JavaModule;
import net.sf.iwant.api.model.ExternalSource;
import net.sf.iwant.api.model.Path;

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
