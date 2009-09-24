package junit.framework;

public abstract class TestCase {

	public void fail() {
		throw new AssertionFailedError();
	}

	public void fail(String string) {
		throw new AssertionFailedError(string);
	}

	public void assertEquals(Object o1, Object o2) {
		if (!o1.equals(o2)) {
			fail("Not equal:\n" + o1 + "\nand\n" + o2);
		}
	}

}
