package org.tomdang.mining.customminingability;

public interface MiningBlockReactiveAbility {

	void onBlockMined(MiningAbilityContext context, AbilityMiningHandler handler);

}
