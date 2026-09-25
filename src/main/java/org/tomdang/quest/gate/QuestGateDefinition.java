package org.tomdang.quest.gate;

public record QuestGateDefinition(
		String id, String questId, String world,
		double centerX, double centerZ, double normalX, double normalZ,
		double halfWidth, double blockedSideMargin, double returnDistance,
		int minimumY, int maximumY, int blinkTicks, long messageCooldownMillis,
		String actorId, String dialogueId, String promptMessage
) {
	public QuestGateDefinition {
		if (id == null || id.isBlank() || questId == null || questId.isBlank() || world == null || world.isBlank())
			throw new IllegalArgumentException("Gate id, quest, and world are required");
		double length = Math.hypot(normalX, normalZ);
		if (length < 0.0001) throw new IllegalArgumentException("Gate normal cannot be zero");
		normalX /= length; normalZ /= length;
		if (halfWidth <= 0 || returnDistance <= 0 || minimumY > maximumY || blinkTicks < 0 || messageCooldownMillis < 0)
			throw new IllegalArgumentException("Invalid gate dimensions or timing: " + id);
		if ((actorId == null) != (dialogueId == null)) throw new IllegalArgumentException(
				"Gate actor and dialogue must either both be set or both be absent: " + id);
		if (promptMessage != null && promptMessage.isBlank()) promptMessage = null;
	}
}
