package org.tomdang.platform.registry;

import org.tomdang.platform.identity.ContentKey;

import java.util.Collection;
import java.util.Optional;

public interface DefinitionRegistry<T> {
	void register(ContentKey<T> key, T value);
	Optional<T> find(ContentKey<T> key);
	T require(ContentKey<T> key);
	Collection<T> all();
	Collection<ContentKey<T>> keys();
	boolean isSealed();
	void seal();
}
