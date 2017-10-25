package org.fluentjava.iwant.api.model;

import java.io.File;
import java.net.URL;

public interface CacheScopeChoices {

	File target(Target target);

	File source(Source target);

	File unmodifiableUrl(URL url);

}
