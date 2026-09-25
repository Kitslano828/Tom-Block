package org.tomdang.quest.orchestration;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
public final class InMemoryQuestRewardLedger implements QuestRewardLedger {
	private final Set<Key> claims = new HashSet<>();
	@Override public synchronized boolean claim(UUID playerId, String deliveryId) { return claims.add(new Key(playerId, deliveryId)); }
	@Override public synchronized void release(UUID playerId, String deliveryId) { claims.remove(new Key(playerId, deliveryId)); }
	private record Key(UUID playerId, String deliveryId) {}
}
