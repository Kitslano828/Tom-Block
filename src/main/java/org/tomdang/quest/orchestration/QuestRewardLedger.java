package org.tomdang.quest.orchestration;
import java.util.UUID;
public interface QuestRewardLedger {
	boolean claim(UUID playerId, String deliveryId);
	void release(UUID playerId, String deliveryId);
}
