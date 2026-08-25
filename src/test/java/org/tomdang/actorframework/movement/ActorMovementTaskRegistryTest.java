package org.tomdang.actorframework.movement;

import org.bukkit.scheduler.BukkitTask;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

public class ActorMovementTaskRegistryTest {

	@Test
	void registeredTaskCanBeRetrieved() {
		ActorMovementTaskRegistry actorMovementTaskRegistry = new ActorMovementTaskRegistry();
		UUID uuid = UUID.randomUUID();

		BukkitTask task = mock(BukkitTask.class);

		actorMovementTaskRegistry.registerTask(uuid, task);

		assertTrue(actorMovementTaskRegistry.hasActiveMovement(uuid));
		assertSame(task, actorMovementTaskRegistry.getMovement(uuid));
	}

	@Test
	void cancellingMovementRemovesAndCancelsTask() {
		ActorMovementTaskRegistry actorMovementTaskRegistry = new ActorMovementTaskRegistry();
		UUID uuid = UUID.randomUUID();

		BukkitTask task = mock(BukkitTask.class);

		actorMovementTaskRegistry.registerTask(uuid, task);

		assertTrue(actorMovementTaskRegistry.cancelMovement(uuid));
		assertFalse(actorMovementTaskRegistry.hasActiveMovement(uuid));
		verify(task).cancel();
	}

	@Test
	void cancellingMissingMovementReturnsFalse() {
		ActorMovementTaskRegistry actorMovementTaskRegistry = new ActorMovementTaskRegistry();
		UUID uuid = UUID.randomUUID();

		assertFalse(actorMovementTaskRegistry.cancelMovement(uuid));
	}

	@Test
	void completingOldTaskDoesNotRemoveCurrentTask() {
		ActorMovementTaskRegistry actorMovementTaskRegistry = new ActorMovementTaskRegistry();
		UUID uuid = UUID.randomUUID();

		BukkitTask oldTask = mock(BukkitTask.class);
		BukkitTask newTask = mock(BukkitTask.class);

		actorMovementTaskRegistry.registerTask(uuid, oldTask);
		actorMovementTaskRegistry.cancelMovement(uuid);

		actorMovementTaskRegistry.registerTask(uuid, newTask);
		assertFalse(actorMovementTaskRegistry.completeMovement(uuid, oldTask));
		assertSame(newTask, actorMovementTaskRegistry.getMovement(uuid));
	}

	@Test
	void completingCurrentTaskRemovesIt() {
		ActorMovementTaskRegistry actorMovementTaskRegistry = new ActorMovementTaskRegistry();
		UUID uuid = UUID.randomUUID();

		BukkitTask task = mock(BukkitTask.class);

		actorMovementTaskRegistry.registerTask(uuid, task);

		assertTrue(actorMovementTaskRegistry.completeMovement(uuid, task));
		assertFalse(actorMovementTaskRegistry.hasActiveMovement(uuid));
		verify(task, never()).cancel();
	}

	@Test
	void registeringSecondTaskForSameActorIsRejected() {
		ActorMovementTaskRegistry actorMovementTaskRegistry = new ActorMovementTaskRegistry();
		UUID uuid = UUID.randomUUID();

		BukkitTask firstTask = mock(BukkitTask.class);
		BukkitTask secondTask = mock(BukkitTask.class);

		actorMovementTaskRegistry.registerTask(uuid, firstTask);
		assertThrows(IllegalStateException.class, () -> {
			actorMovementTaskRegistry.registerTask(uuid, secondTask);
				});

		assertSame(firstTask, actorMovementTaskRegistry.getMovement(uuid));
	}

	@Test
	void cancelAllCancelsAndRemovesEveryTask() {
		ActorMovementTaskRegistry actorMovementTaskRegistry = new ActorMovementTaskRegistry();
		UUID uuid = UUID.randomUUID();
		UUID anotherUUID = UUID.randomUUID();

		BukkitTask firstTask = mock(BukkitTask.class);
		BukkitTask secondTask = mock(BukkitTask.class);

		actorMovementTaskRegistry.registerTask(uuid, firstTask);
		actorMovementTaskRegistry.registerTask(anotherUUID, secondTask);

		actorMovementTaskRegistry.cancelAll();

		assertFalse(actorMovementTaskRegistry.hasActiveMovement(uuid));
		assertFalse(actorMovementTaskRegistry.hasActiveMovement(anotherUUID));

		verify(firstTask).cancel();
		verify(secondTask).cancel();
	}

}
