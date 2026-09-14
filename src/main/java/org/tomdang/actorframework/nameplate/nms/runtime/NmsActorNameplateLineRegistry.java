package org.tomdang.actorframework.nameplate.nms.runtime;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class NmsActorNameplateLineRegistry {

	private final Map<UUID, NmsActorNameplateLine> nameplateLines = new HashMap<>();

	public void register(NmsActorNameplateLine nameplateLine) {
		if (nameplateLine == null) throw new IllegalArgumentException("nameplateLine cannot be null");

		UUID presentationUUID = nameplateLine.getPresentationUUID();

		if (nameplateLines.containsKey(presentationUUID)) throw new IllegalStateException("UUID is already present in nameplateLines");
		nameplateLines.put(presentationUUID, nameplateLine);
	}

	public NmsActorNameplateLine get(UUID presentationUUID) {
		if (presentationUUID == null) throw new IllegalArgumentException("presentationUUID cannot be null");
		return nameplateLines.get(presentationUUID);
	}

	public boolean contains(UUID presentationUUID) {
		if (presentationUUID == null) throw new IllegalArgumentException("presentationUUID cannot be null");
		return nameplateLines.containsKey(presentationUUID);
	}

	public NmsActorNameplateLine remove(UUID presentationUUID) {
		if (presentationUUID == null) throw new IllegalArgumentException("presentationUUID cannot be null");
		return nameplateLines.remove(presentationUUID);
	}

	public List<NmsActorNameplateLine> getAllLines() {
		return List.copyOf(nameplateLines.values());
	}

}
