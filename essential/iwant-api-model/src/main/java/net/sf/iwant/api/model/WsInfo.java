package net.sf.iwant.api.model;

import java.io.File;

public interface WsInfo {

	String wsName();

	File wsRoot();

	File wsdefdefModule();

	File wsdefdefSrc();

	String wsdefdefClass();

	File wsdefdefJava();

	String wsdefdefPackage();

	String wsdefdefClassSimpleName();

	String relativeAsSomeone();

}