package net.sf.iwant.api;

import java.io.File;

public interface WsInfo {

	String wsName();

	File wsRoot();

	File wsdefdefModule();

	File wsdefdefSrc();

	String wsdefClass();

	File wsdefdefJava();

	String wsdefdefPackage();

	String wsdefdefClassSimpleName();

}