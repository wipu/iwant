package com.example.findbugsfodder.editversionfindbugsreport;

public class FindbugsFodder {

	@SuppressWarnings("null")
	public void nullReference(Object o) {
		if (o == null) {
			System.out.println(o.toString());
		}
	}

}
