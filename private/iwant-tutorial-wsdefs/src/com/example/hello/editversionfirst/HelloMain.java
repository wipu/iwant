package com.example.hello.editversionfirst;

public class HelloMain {

	public static void main(String[] args) {
		System.out.println(greetingTo(args[0]));
	}

	public static String greetingTo(String target) {
		return "Hello " + target;
	}

}
