package junit.framework;

import java.util.ArrayList;
import java.util.List;

public class TestSuite implements Test {

	private final String name;

	private final List<Class> subTests = new ArrayList();

	public TestSuite(String name) {
		this.name = name;
	}

	public void addTestSuite(Class suite) {
		subTests.add(suite);
	}

	public String getName() {
		return name;
	}

	public List<Class> getSubTests() {
		return subTests;
	}

}
