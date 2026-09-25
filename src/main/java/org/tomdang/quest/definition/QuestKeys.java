package org.tomdang.quest.definition;

import org.tomdang.platform.identity.ContentKey;

import java.util.Locale;

public final class QuestKeys {
	public static final String DEFAULT_NAMESPACE = "tomblock";

	private QuestKeys() {}

	/** Converts existing persisted quest IDs without changing their PostgreSQL representation. */
	public static ContentKey<QuestDefinition> fromStoredId(String storedId) {
		if (storedId == null || storedId.isBlank()) throw new IllegalArgumentException("Quest id cannot be blank");
		String trimmed = storedId.trim();
		if (trimmed.contains(":")) return ContentKey.parse(trimmed);
		return ContentKey.of(DEFAULT_NAMESPACE, trimmed.toLowerCase(Locale.ROOT));
	}
}
