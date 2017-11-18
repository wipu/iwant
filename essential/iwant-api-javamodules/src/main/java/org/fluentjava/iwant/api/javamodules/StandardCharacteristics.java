package org.fluentjava.iwant.api.javamodules;

@SuppressWarnings("all")
public class StandardCharacteristics {

	public interface BuildtimeData extends JavaModuleCharacteristic {
	}

	public interface BuildConfiguration extends BuildtimeData {
	}

	public interface BuildUtility extends BuildtimeData {
	}

	public interface ProductionRuntimeData extends JavaModuleCharacteristic {
	}

	public interface ProductionCode extends ProductionRuntimeData {
	}

	public interface ProductionConfiguration extends ProductionRuntimeData {
	}

	public interface TestRuntimeData extends JavaModuleCharacteristic {
	}

	public interface TestCode extends TestRuntimeData {
	}

	public interface TestConfiguration extends TestRuntimeData {
	}

	public interface TestUtility extends TestRuntimeData {
	}

}
