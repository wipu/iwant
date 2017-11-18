package org.fluentjava.iwant.api.model;

import java.io.File;
import java.util.List;

public interface IngredientDefinitionContext
		extends TemporaryDirectoryProvider {

	List<? extends Target> targets();

	File locationOf(Source source);

}
