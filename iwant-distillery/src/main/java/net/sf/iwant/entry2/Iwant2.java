package net.sf.iwant.entry2;

import net.sf.iwant.entry.Iwant.IwantNetwork;

public class Iwant2 {

	@SuppressWarnings("unused")
	private final IwantNetwork network;

	public Iwant2(IwantNetwork network) {
		this.network = network;
	}

	public static void main(String[] args) {
		throw new UnsupportedOperationException("TODO test and implement");
	}

	public static Iwant2 using(IwantNetwork network) {
		return new Iwant2(network);
	}

}
