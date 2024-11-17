package org.fluentjava.iwant.entrymocks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

public class NullCheckTest {

	@Test
	public void nullCausesFriendlyExceptionWithCallerMethodInMessage() {
		class UntaughthMock {
			public String a() {
				return NullCheck.nonNull(null);
			}
		}

		try {
			new UntaughthMock().a();
			fail();
		} catch (IllegalStateException e) {
			assertEquals("You forgot to teach a", e.getMessage());
		}
	}

	@Test
	public void nullCausesFriendlyExceptionWithGivenNameIfGiven() {
		class UntaughthMock {
			public String a() {
				return NullCheck.nonNull(null, "custom name");
			}
		}

		try {
			new UntaughthMock().a();
			fail();
		} catch (IllegalStateException e) {
			assertEquals("You forgot to teach custom name", e.getMessage());
		}
	}

	@Test
	public void nonNullIsReturnedAsSuch() {
		class TaughthMock {
			public String a() {
				return NullCheck.nonNull("a value");
			}
		}

		assertEquals("a value", new TaughthMock().a());
	}

}
