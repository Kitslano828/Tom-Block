package org.tomdang.actorframework.presentation.bukkit;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.tomdang.actorframework.collision.ActorCollisionPolicy;

public class BukkitActorCollisionService {

	private final Team passThroughTeam;

	public BukkitActorCollisionService(Scoreboard scoreboard) {
		if (scoreboard == null) throw new IllegalArgumentException("Scoreboard cannot be null");

		Team team = scoreboard.getTeam("tb_actor_pass");
		if (team == null) {
			team = scoreboard.registerNewTeam("tb_actor_pass");
		}
		this.passThroughTeam = team;
		this.passThroughTeam.setOption(
				Team.Option.COLLISION_RULE,
				Team.OptionStatus.NEVER
		);
	}

	public void applyCollisionPolicy(Entity entity, ActorCollisionPolicy collisionPolicy) {
		if (entity == null) {
			throw new IllegalArgumentException("Entity cannot be null");
		}

		if (collisionPolicy == null) {
			throw new IllegalArgumentException("Collision policy cannot be null");
		}

		if (collisionPolicy == ActorCollisionPolicy.PASS_THROUGH) {
			if (entity instanceof LivingEntity)
				((LivingEntity) entity).setCollidable(false);
			passThroughTeam.addEntity(entity);
			return;
		}

		passThroughTeam.removeEntity(entity);
		if (entity instanceof  LivingEntity)
			((LivingEntity)entity).setCollidable(true);
	}

	public void removeCollisionState(Entity entity) {
		if (entity == null) {
			throw new IllegalArgumentException("Entity cannot be null");
		}
		passThroughTeam.removeEntity(entity);
	}

	public void applyCollisionPolicyToEntry(String scoreboardEntry, ActorCollisionPolicy collisionPolicy) {
		if (scoreboardEntry == null) throw new IllegalArgumentException("scoreboardEntry cannot be null");
		if (scoreboardEntry.isBlank()) throw new IllegalArgumentException("scoreboardEntry cannot be blank");
		if (collisionPolicy == null) throw new IllegalArgumentException("collisionPolicy cannot be null");

		switch (collisionPolicy) {
			case PASS_THROUGH -> passThroughTeam.addEntry(scoreboardEntry);
			case SOLID -> passThroughTeam.removeEntry(scoreboardEntry);
		}
	}

	public void removeCollisionEntry(String scoreboardEntry) {
		if (scoreboardEntry == null) throw new IllegalArgumentException("scoreboardEntry cannot be null");
		if (scoreboardEntry.isBlank()) throw new IllegalArgumentException("scoreboardEntry cannot be blank");
		passThroughTeam.removeEntry(scoreboardEntry);
	}

}
