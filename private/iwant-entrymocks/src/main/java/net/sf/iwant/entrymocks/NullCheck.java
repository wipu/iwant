package net.sf.iwant.entrymocks;

public class NullCheck {

	public static <T> T nonNull(T value) {
		return nonNull(value,
				new Exception().getStackTrace()[1].getMethodName());
	}

	public static <T> T nonNull(T value, String objectName) {
		if (value == null) {
			throw new IllegalStateException(
					"You forgot to teach " + objectName);
		}
		return value;
	}

}
