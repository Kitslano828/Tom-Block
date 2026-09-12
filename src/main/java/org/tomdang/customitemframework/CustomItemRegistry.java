package org.tomdang.customitemframework;

import java.util.HashMap;
import java.util.Map;

public class CustomItemRegistry {

	private final Map<String, CustomItem> customitemMap;

	public CustomItemRegistry() {
		customitemMap = new HashMap<>();
	}

	public boolean isACustomItem(CustomItem item) {
		return customitemMap.containsKey(item.getId());
	}

	public void addItemToRegistry(CustomItem item) {
		if (containsCustomItem(item.getId())) throw new IllegalStateException("item " + item.getId() + " already exists");
		customitemMap.put(item.getId(), item);
	}

	public CustomItem getCustomItem(String id) {
		return customitemMap.get(id);
	}

	public boolean containsCustomItem(String id) {
		if (id == null || id.isBlank()) throw new IllegalArgumentException("id cannot be null or blank");
		return customitemMap.containsKey(id);
	}

}
