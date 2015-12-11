package com.example.mixedscala.editversionfirst;

public class JavaThatDependsOnScala {

	public static void main(String[] args) {
		System.out.println(
				new com.example.mixedscala.editversionfirst.ScalaThatDependsOnJava()
						.stringFromScala());
	}

}
