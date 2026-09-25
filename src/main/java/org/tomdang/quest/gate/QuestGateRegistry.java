package org.tomdang.quest.gate;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public final class QuestGateRegistry {
	private final Map<String, QuestGateDefinition> gates = new LinkedHashMap<>();
	public void register(QuestGateDefinition gate) {
		if (gate == null) throw new IllegalArgumentException("gate cannot be null");
		if (gates.putIfAbsent(gate.id(), gate) != null) throw new IllegalArgumentException("Duplicate quest gate: " + gate.id());
	}
	public Collection<QuestGateDefinition> all() { return java.util.List.copyOf(gates.values()); }
}
