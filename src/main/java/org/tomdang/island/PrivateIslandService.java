package org.tomdang.island;

import java.util.UUID;

public final class PrivateIslandService {
	private final PrivateIslandRepository repository;
	public PrivateIslandService(PrivateIslandRepository repository) { this.repository = repository; }
	public PrivateIsland getOrCreate(UUID ownerId) {
		return repository.findByOwner(ownerId).orElseGet(() -> repository.createForOwner(ownerId));
	}
}
