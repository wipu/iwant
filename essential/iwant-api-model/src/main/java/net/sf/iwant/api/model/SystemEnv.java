package net.sf.iwant.api.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class SystemEnv {

	private final Map<String, Value<?>> values;

	private SystemEnv(Map<String, Value<?>> values) {
		this.values = values;
	}

	public static SystemEnvBuilder with() {
		return new SystemEnvBuilder();
	}

	public interface SystemEnvPlease {

		SystemEnvPlease string(String name, String value);

		SystemEnvPlease path(String name, Path value);

	}

	public static class SystemEnvBuilder implements SystemEnvPlease {

		private final Map<String, Value<?>> values = new LinkedHashMap<>();

		public SystemEnv end() {
			return new SystemEnv(values);
		}

		@Override
		public SystemEnvBuilder string(String name, String value) {
			values.put(name, new StringValue(value));
			return this;
		}

		@Override
		public SystemEnvBuilder path(String name, Path value) {
			values.put(name, new PathValue(value));
			return this;
		}

	}

	private static abstract class Value<T> {

		private T value;

		Value(T value) {
			this.value = value;
		}

		T value() {
			return value;
		}

		abstract SystemEnvPlease writeTo(String name, SystemEnvPlease out);

	}

	private static class StringValue extends Value<String> {

		public StringValue(String value) {
			super(value);
		}

		@Override
		SystemEnvPlease writeTo(String name, SystemEnvPlease out) {
			return out.string(name, value());
		}

		@Override
		public String toString() {
			return "string:" + value();
		}

	}

	private static class PathValue extends Value<Path> {

		public PathValue(Path value) {
			super(value);
		}

		@Override
		SystemEnvPlease writeTo(String name, SystemEnvPlease out) {
			return out.path(name, value());
		}

		@Override
		public String toString() {
			return "path:" + value();
		}

	}

	public Object get(String name) {
		Value<?> value = values.get(name);
		return value == null ? null : value.value();
	}

	public void shovelTo(SystemEnvPlease out) {
		SystemEnvPlease currentOut = out;
		for (Entry<String, Value<?>> entry : values.entrySet()) {
			currentOut = entry.getValue().writeTo(entry.getKey(), currentOut);
		}
	}

	@Override
	public String toString() {
		return "SystemEnv" + values;
	}

}
