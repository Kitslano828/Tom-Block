package org.tomdang.player.skill;

import org.bukkit.entity.Player;

@FunctionalInterface
public interface SkillProgressNotificationSink {
    void show(Player player, SkillAwardResult result);
}
