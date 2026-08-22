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
		customitemMap.put(item.getId(), item);
	}

	public CustomItem getCustomItem(String id) {
		return customitemMap.get(id);
	}

}
