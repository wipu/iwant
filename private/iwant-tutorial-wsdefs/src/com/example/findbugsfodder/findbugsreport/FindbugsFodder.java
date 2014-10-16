package com.example.findbugsfodder.findbugsreport;

public class FindbugsFodder {

	@SuppressWarnings("null")
	public void nullReference(Object o) {
		if (o == null) {
			System.out.println(o.toString());
		}
	}

}
