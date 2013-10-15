package net.sf.iwant.api.javamodules;

import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sf.iwant.api.model.Path;

public abstract class JavaModule implements Comparable<JavaModule> {

	private final SortedSet<Class<? extends JavaModuleCharacteristic>> characteristics = new TreeSet<Class<? extends JavaModuleCharacteristic>>(
			new ClassComparator());

	public JavaModule(
			Set<Class<? extends JavaModuleCharacteristic>> characteristics) {
		this.characteristics.addAll(characteristics);
	}

	public abstract String name();

	public abstract Path mainArtifact();

	@Override
	public int compareTo(JavaModule o) {
		return name().compareTo(o.name());
	}

	public abstract Set<JavaModule> mainDeps();

	public final SortedSet<Class<? extends JavaModuleCharacteristic>> characteristics() {
		return characteristics;
	}

	public final boolean doesHave(
			Class<? extends JavaModuleCharacteristic> characteristic) {
		for (Class<? extends JavaModuleCharacteristic> actual : characteristics) {
			if (characteristic.isAssignableFrom(actual)) {
				return true;
			}
		}
		return false;
	}

	private static class ClassComparator implements Comparator<Class<?>> {

		@Override
		public int compare(Class<?> o1, Class<?> o2) {
			return o1.getCanonicalName().compareTo(o2.getCanonicalName());
		}

	}

	@Override
	public final int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name() == null) ? 0 : name().hashCode());
		return result;
	}

	@Override
	public final boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		JavaModule other = (JavaModule) obj;
		if (name() == null) {
			if (other.name() != null) {
				return false;
			}
		} else if (!name().equals(other.name())) {
			return false;
		}
		return true;
	}

}
