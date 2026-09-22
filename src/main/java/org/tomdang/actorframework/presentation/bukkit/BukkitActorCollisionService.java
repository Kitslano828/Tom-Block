package org.tomdang.actorframework.presentation.bukkit;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.tomdang.actorframework.collision.ActorCollisionPolicy;

import java.util.IdentityHashMap;
import java.util.Map;

public class BukkitActorCollisionService {

	private final Team passThroughTeam;
	private final Team packetNpcPassThroughTeam;
	private final Team packetNpcSolidTeam;
	private final Map<Scoreboard, TeamSet> viewerTeams = new IdentityHashMap<>();

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

		this.packetNpcPassThroughTeam = getOrCreateTeam(scoreboard, "tb_npc_pass");
		this.packetNpcPassThroughTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
		this.packetNpcPassThroughTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);

		this.packetNpcSolidTeam = getOrCreateTeam(scoreboard, "tb_npc_solid");
		this.packetNpcSolidTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.ALWAYS);
		this.packetNpcSolidTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
	}

	private Team getOrCreateTeam(Scoreboard scoreboard, String teamName) {
		Team team = scoreboard.getTeam(teamName);
		return team == null ? scoreboard.registerNewTeam(teamName) : team;
	}

	/** Mirrors TomBlock's actor teams onto a player's private HUD scoreboard. */
	public void attachScoreboard(Scoreboard scoreboard) {
		if (scoreboard == null) throw new IllegalArgumentException("scoreboard cannot be null");
		if (viewerTeams.containsKey(scoreboard)) return;
		Team actor = copyTeam(scoreboard, passThroughTeam);
		Team npcPass = copyTeam(scoreboard, packetNpcPassThroughTeam);
		Team npcSolid = copyTeam(scoreboard, packetNpcSolidTeam);
		viewerTeams.put(scoreboard, new TeamSet(actor, npcPass, npcSolid));
	}

	public void detachScoreboard(Scoreboard scoreboard) {
		viewerTeams.remove(scoreboard);
	}

	private Team copyTeam(Scoreboard scoreboard, Team source) {
		Team target = getOrCreateTeam(scoreboard, source.getName());
		target.setOption(Team.Option.COLLISION_RULE, source.getOption(Team.Option.COLLISION_RULE));
		target.setOption(Team.Option.NAME_TAG_VISIBILITY, source.getOption(Team.Option.NAME_TAG_VISIBILITY));
		for (String entry : source.getEntries()) target.addEntry(entry);
		return target;
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
			viewerTeams.values().forEach(teams -> teams.actor().addEntity(entity));
			return;
		}

		passThroughTeam.removeEntity(entity);
		viewerTeams.values().forEach(teams -> teams.actor().removeEntity(entity));
		if (entity instanceof  LivingEntity)
			((LivingEntity)entity).setCollidable(true);
	}

	public void removeCollisionState(Entity entity) {
		if (entity == null) {
			throw new IllegalArgumentException("Entity cannot be null");
		}
		passThroughTeam.removeEntity(entity);
		viewerTeams.values().forEach(teams -> teams.actor().removeEntity(entity));
	}

	public void applyCollisionPolicyToEntry(String scoreboardEntry, ActorCollisionPolicy collisionPolicy) {
		if (scoreboardEntry == null) throw new IllegalArgumentException("scoreboardEntry cannot be null");
		if (scoreboardEntry.isBlank()) throw new IllegalArgumentException("scoreboardEntry cannot be blank");
		if (collisionPolicy == null) throw new IllegalArgumentException("collisionPolicy cannot be null");

		packetNpcPassThroughTeam.removeEntry(scoreboardEntry);
		packetNpcSolidTeam.removeEntry(scoreboardEntry);
		viewerTeams.values().forEach(teams -> {
			teams.npcPass().removeEntry(scoreboardEntry);
			teams.npcSolid().removeEntry(scoreboardEntry);
		});

		switch (collisionPolicy) {
			case PASS_THROUGH -> {
				packetNpcPassThroughTeam.addEntry(scoreboardEntry);
				viewerTeams.values().forEach(teams -> teams.npcPass().addEntry(scoreboardEntry));
			}
			case SOLID -> {
				packetNpcSolidTeam.addEntry(scoreboardEntry);
				viewerTeams.values().forEach(teams -> teams.npcSolid().addEntry(scoreboardEntry));
			}
		}
	}

	public void removeCollisionEntry(String scoreboardEntry) {
		if (scoreboardEntry == null) throw new IllegalArgumentException("scoreboardEntry cannot be null");
		if (scoreboardEntry.isBlank()) throw new IllegalArgumentException("scoreboardEntry cannot be blank");
		packetNpcPassThroughTeam.removeEntry(scoreboardEntry);
		packetNpcSolidTeam.removeEntry(scoreboardEntry);
		viewerTeams.values().forEach(teams -> {
			teams.npcPass().removeEntry(scoreboardEntry);
			teams.npcSolid().removeEntry(scoreboardEntry);
		});
	}

	private record TeamSet(Team actor, Team npcPass, Team npcSolid) {}

}
