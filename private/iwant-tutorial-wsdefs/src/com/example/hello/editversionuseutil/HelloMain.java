package com.example.hello.editversionuseutil;

import com.example.helloutil.editversionfirst.HelloUtil;

public class HelloMain {

	public static void main(String[] args) {
		System.out.println(greetingTo(args[0]));
	}

	public static String greetingTo(String target) {
		return HelloUtil.spaceSeparatedWords("Hello", target);
	}

}
