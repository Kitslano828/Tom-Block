package org.tomdang.combat.weapons.configuration;

import org.bukkit.Material;
import org.tomdang.customitemframework.Rarity;

import java.util.List;

public record WeaponDefinition(String id, Material material, String displayName, Rarity rarity, double damage, double strength, List<String> abilityIDs) {
}
