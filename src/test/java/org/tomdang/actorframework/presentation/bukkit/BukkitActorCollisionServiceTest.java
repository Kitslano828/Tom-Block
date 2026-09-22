package org.tomdang.actorframework.presentation.bukkit;

import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.junit.jupiter.api.Test;
import org.tomdang.actorframework.collision.ActorCollisionPolicy;

import java.util.Set;

import static org.mockito.Mockito.*;

class BukkitActorCollisionServiceTest {
	@Test
	void newViewerBoardReceivesExistingAndFutureNpcEntries() {
		Scoreboard main = mock(Scoreboard.class);
		Team mainActor = team("tb_actor_pass", Set.of());
		Team mainPass = team("tb_npc_pass", Set.of("existing"));
		Team mainSolid = team("tb_npc_solid", Set.of());
		when(main.registerNewTeam("tb_actor_pass")).thenReturn(mainActor);
		when(main.registerNewTeam("tb_npc_pass")).thenReturn(mainPass);
		when(main.registerNewTeam("tb_npc_solid")).thenReturn(mainSolid);
		BukkitActorCollisionService service = new BukkitActorCollisionService(main);

		Scoreboard viewer = mock(Scoreboard.class);
		Team viewerActor = team("tb_actor_pass", Set.of());
		Team viewerPass = team("tb_npc_pass", Set.of());
		Team viewerSolid = team("tb_npc_solid", Set.of());
		when(viewer.registerNewTeam("tb_actor_pass")).thenReturn(viewerActor);
		when(viewer.registerNewTeam("tb_npc_pass")).thenReturn(viewerPass);
		when(viewer.registerNewTeam("tb_npc_solid")).thenReturn(viewerSolid);
		service.attachScoreboard(viewer);
		verify(viewerPass).addEntry("existing");

		service.applyCollisionPolicyToEntry("new", ActorCollisionPolicy.PASS_THROUGH);
		verify(viewerPass).addEntry("new");
		service.removeCollisionEntry("new");
		verify(viewerPass, atLeastOnce()).removeEntry("new");
		service.detachScoreboard(viewer);
		service.applyCollisionPolicyToEntry("later", ActorCollisionPolicy.SOLID);
		verify(viewerSolid, never()).addEntry("later");
	}

	private static Team team(String name, Set<String> entries) {
		Team team = mock(Team.class);
		when(team.getName()).thenReturn(name);
		when(team.getEntries()).thenReturn(entries);
		return team;
	}
}
