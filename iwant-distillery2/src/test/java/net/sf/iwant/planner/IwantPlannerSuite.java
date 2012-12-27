package net.sf.iwant.planner;

import junit.framework.Test;
import junit.framework.TestSuite;

public class IwantPlannerSuite {

	public static Test suite() {
		TestSuite suite = new TestSuite("net.sf.iwant.planner");
		suite.addTestSuite(TaskDirtinessTest.class);
		suite.addTestSuite(TaskQueueTest.class);
		suite.addTestSuite(PlannerTest.class);
		return suite;
	}
}
