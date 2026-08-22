package org.tomdang.customabilityframework;

import org.tomdang.customabilityframework.customability.CustomAbility;

import java.util.HashMap;
import java.util.Map;

public class CustomAbilityRegistry {

	private final Map<String, CustomAbility> customAbilityMap = new HashMap<>();

		public CustomAbilityRegistry() {

	}

	public void registerAbility(CustomAbility ability) {
		customAbilityMap.put(ability.getAbilityID(), ability);
	}

	public CustomAbility getCustomAbility(String id) {
		return customAbilityMap.get(id);
	}

}
