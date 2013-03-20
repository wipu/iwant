package net.sf.iwant.api;

public class TestedIwantDependencies {

	public static Path emma() {
		return FromRepository.ibiblio().group("emma").name("emma")
				.version("2.0.5312");
	}

}
