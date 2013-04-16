package net.sf.iwant.entry3;

import junit.framework.Test;
import junit.framework.TestSuite;

public class IwantEntry3Suite {

	public static Test suite() {
		TestSuite suite = new TestSuite("net.sf.iwant.entry3");
		suite.addTestSuite(WsInfoTest.class);
		suite.addTestSuite(CachesImplTest.class);
		suite.addTestSuite(ExampleWsDefGeneratorTest.class);
		suite.addTestSuite(WishScriptGeneratorTest.class);
		suite.addTestSuite(IngredientCheckingTargetEvaluationContextTest.class);
		suite.addTestSuite(TargetRefreshTaskTest.class);
		suite.addTestSuite(PathDefinitionConflictCheckerTest.class);
		suite.addTestSuite(WishEvaluatorTest.class);
		suite.addTestSuite(UserPrefsImplTest.class);
		suite.addTestSuite(Iwant3Test.class);
		suite.addTestSuite(WorkspaceDefinitionContextImplTest.class);
		return suite;
	}

}
