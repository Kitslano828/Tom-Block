package org.tomdang.island;

import java.util.Optional;
import java.util.UUID;

public interface PrivateIslandRepository {
	Optional<PrivateIsland> findByOwner(UUID ownerId);
	PrivateIsland createForOwner(UUID ownerId);
}
