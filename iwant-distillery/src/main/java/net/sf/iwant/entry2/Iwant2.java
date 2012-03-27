package net.sf.iwant.entry2;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry.Iwant.IwantNetwork;

public class Iwant2 {

	private final IwantNetwork network;

	public Iwant2(IwantNetwork network) {
		this.network = network;
	}

	public static void main(String[] args) {
		File iwantWs = new File(args[0]);
		String[] args2 = new String[args.length - 1];
		System.arraycopy(args, 1, args2, 0, args2.length);
		try {
			using(Iwant.usingRealNetwork().network()).evaluate(iwantWs, args2);
		} catch (IwantException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}

	public static Iwant2 using(IwantNetwork network) {
		return new Iwant2(network);
	}

	public static class IwantException extends RuntimeException {

		public IwantException(String message) {
			super(message);
		}

	}

	public void evaluate(File iwantWs, String... args) {
		try {
			Iwant iwant = Iwant.using(network);

			File allIwantClasses = iwant.toCachePath(new File(iwantWs,
					"all-classes").toURI().toURL());
			Iwant.ensureDir(allIwantClasses);

			List<File> src = new ArrayList<File>();
			src.add(new File(iwantWs, "iwant-distillery2/" + "src/main/java/"
					+ "net/sf/iwant/entry3/Iwant3.java"));
			iwant.compiledClasses(allIwantClasses, src);

			File[] classLocations = { allIwantClasses };
			Iwant.runJavaMain(false, false, false,
					"net.sf.iwant.entry3.Iwant3", classLocations, args);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
