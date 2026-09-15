package org.tomdang.combat.hit;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.tomdang.combat.PlayerCombatHitContext;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PlayerCombatHitPublisherTest {
	@Test
	void notifiesObserversInRegistrationOrder() {
		Logger logger = mock(Logger.class);
		PlayerCombatHitObserver first = mock(PlayerCombatHitObserver.class);
		PlayerCombatHitObserver second = mock(PlayerCombatHitObserver.class);
		PlayerCombatHitContext context = mock(PlayerCombatHitContext.class);
		PlayerCombatHitPublisher publisher = new PlayerCombatHitPublisher(logger);
		publisher.register(first);
		publisher.register(second);

		publisher.publish(context);

		InOrder ordered = inOrder(first, second);
		ordered.verify(first).onHit(context);
		ordered.verify(second).onHit(context);
	}

	@Test
	void logsFailingObserverAndContinuesPublishing() {
		Logger logger = mock(Logger.class);
		IllegalStateException failure = new IllegalStateException("broken observer");
		PlayerCombatHitObserver failing = context -> { throw failure; };
		PlayerCombatHitObserver healthy = mock(PlayerCombatHitObserver.class);
		PlayerCombatHitContext context = mock(PlayerCombatHitContext.class);
		PlayerCombatHitPublisher publisher = new PlayerCombatHitPublisher(logger);
		publisher.register(failing);
		publisher.register(healthy);

		publisher.publish(context);

		verify(healthy).onHit(context);
		verify(logger).log(eq(Level.SEVERE), anyString(), eq(failure));
	}

	@Test
	void unregisterStopsFutureNotifications() {
		PlayerCombatHitObserver observer = mock(PlayerCombatHitObserver.class);
		PlayerCombatHitContext context = mock(PlayerCombatHitContext.class);
		PlayerCombatHitPublisher publisher = new PlayerCombatHitPublisher(mock(Logger.class));
		publisher.register(observer);
		publisher.unregister(observer);

		publisher.publish(context);

		org.mockito.Mockito.verifyNoInteractions(observer);
	}

	@Test
	void rejectsNullDependenciesAndArguments() {
		PlayerCombatHitPublisher publisher = new PlayerCombatHitPublisher(mock(Logger.class));
		assertThrows(IllegalArgumentException.class, () -> new PlayerCombatHitPublisher(null));
		assertThrows(IllegalArgumentException.class, () -> publisher.register(null));
		assertThrows(IllegalArgumentException.class, () -> publisher.unregister(null));
		assertThrows(IllegalArgumentException.class, () -> publisher.publish(null));
	}
}
