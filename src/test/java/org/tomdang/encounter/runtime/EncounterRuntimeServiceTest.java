package org.tomdang.encounter.runtime;

import org.junit.jupiter.api.Test;
import org.tomdang.encounter.definition.*;
import org.tomdang.gameplay.event.GameplayEventBus;
import org.tomdang.gameplay.event.type.EncounterCompleted;
import org.tomdang.platform.threading.MainThreadGuard;

import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class EncounterRuntimeServiceTest {
	@Test void isolatesPlayersSuspendsResumesCompletesAndPublishes() {
		Fixture fixture = new Fixture(); UUID first=UUID.randomUUID(), second=UUID.randomUUID();
		List<EncounterCompleted> completed=new ArrayList<>(); fixture.events.subscribe(EncounterCompleted.class,completed::add);
		EncounterSession one=fixture.service.start("TEST",first,Set.of(first));
		EncounterSession two=fixture.service.start("TEST",second,Set.of(second));
		assertNotEquals(one.instanceId(),two.instanceId());
		assertThrows(IllegalStateException.class,()->fixture.service.start("TEST",first,Set.of(first)));
		fixture.service.disconnect(first); assertEquals(EncounterState.SUSPENDED,fixture.service.findFor(first).orElseThrow().state());
		fixture.service.reconnect(first); assertEquals(EncounterState.ACTIVE,fixture.service.findFor(first).orElseThrow().state());
		fixture.service.complete(one.instanceId());
		assertTrue(fixture.service.findFor(first).isEmpty()); assertTrue(fixture.service.findFor(second).isPresent());
		assertEquals(List.of(new EncounterCompleted(first,"TEST")),completed);
		assertEquals(2,fixture.behavior.started.get()); assertEquals(1,fixture.behavior.suspended.get());
		assertEquals(1,fixture.behavior.resumed.get()); assertEquals(1,fixture.behavior.completed.get());
	}

	@Test void failsTimedOutAndDisconnectedEncounters() {
		Fixture fixture=new Fixture(); UUID player=UUID.randomUUID();
		EncounterSession session=fixture.service.start("TEST",player,Set.of(player));
		fixture.service.disconnect(player); fixture.clock.advance(Duration.ofSeconds(11)); fixture.service.tick();
		assertTrue(fixture.service.findFor(player).isEmpty()); assertEquals(1,fixture.behavior.failed.get());
		assertEquals("DISCONNECT_TIMEOUT",fixture.behavior.lastFailure);
	}

	private static final class Fixture {
		final MutableClock clock=new MutableClock(); final GameplayEventBus events=new GameplayEventBus(new MainThreadGuard(()->true));
		final TestBehavior behavior=new TestBehavior(); final EncounterRuntimeService service;
		Fixture(){EncounterRegistry definitions=new EncounterRegistry();definitions.register(new EncounterDefinition("TEST","TEST",EncounterMode.PLAYER,Duration.ofMinutes(5),Duration.ofSeconds(10),Map.of()));definitions.seal();EncounterBehaviorRegistry behaviors=new EncounterBehaviorRegistry();behaviors.register("TEST",behavior);behaviors.seal();service=new EncounterRuntimeService(definitions,behaviors,new InMemoryEncounterRepository(),events,clock);}
	}
	private static final class TestBehavior implements EncounterBehavior {
		final AtomicInteger started=new AtomicInteger(),suspended=new AtomicInteger(),resumed=new AtomicInteger(),completed=new AtomicInteger(),failed=new AtomicInteger();String lastFailure;
		@Override public void start(EncounterRuntimeContext context){started.incrementAndGet();}
		@Override public void suspend(EncounterRuntimeContext context){suspended.incrementAndGet();}
		@Override public void resume(EncounterRuntimeContext context){resumed.incrementAndGet();}
		@Override public void complete(EncounterRuntimeContext context){completed.incrementAndGet();}
		@Override public void fail(EncounterRuntimeContext context,String reason){failed.incrementAndGet();lastFailure=reason;}
	}
	private static final class MutableClock extends Clock {
		private Instant now=Instant.parse("2026-09-24T00:00:00Z"); void advance(Duration duration){now=now.plus(duration);}
		@Override public ZoneId getZone(){return ZoneOffset.UTC;} @Override public Clock withZone(ZoneId zone){return this;} @Override public Instant instant(){return now;}
	}
}
