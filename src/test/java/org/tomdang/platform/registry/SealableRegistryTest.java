package org.tomdang.platform.registry;

import org.junit.jupiter.api.Test;
import org.tomdang.platform.identity.ContentKey;
import org.tomdang.platform.validation.ContentValidationException;
import org.tomdang.platform.validation.ValidationReport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SealableRegistryTest {
	@Test void rejectsDuplicatesAndMutationAfterSeal() {
		SealableRegistry<String> registry = new SealableRegistry<>("test");
		ContentKey<String> key = ContentKey.of("tomblock", "one");
		registry.register(key, "value");
		assertThrows(IllegalArgumentException.class, () -> registry.register(key, "other"));
		registry.seal();
		assertEquals("value", registry.require(key));
		assertThrows(IllegalStateException.class,
				() -> registry.register(ContentKey.of("tomblock", "two"), "value"));
	}

	@Test void collectsValidationFailuresBeforeThrowing() {
		SealableRegistry<String> registry = new SealableRegistry<>("test");
		registry.register(ContentKey.of("tomblock", "bad"), "bad");
		ValidationReport report = new ValidationReport();
		registry.validate(report, (result, value) -> result.error(value, "invalid definition"));
		assertThrows(ContentValidationException.class, report::throwIfInvalid);
	}
}
