package org.tomdang.combat.hit;

import org.tomdang.combat.PlayerCombatHitContext;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlayerCombatHitPublisher {
	private final List<PlayerCombatHitObserver> observers = new CopyOnWriteArrayList<>();
	private final Logger logger;

	public PlayerCombatHitPublisher(Logger logger) {
		if (logger == null) throw new IllegalArgumentException("logger cannot be null");
		this.logger = logger;
	}

	public void register(PlayerCombatHitObserver observer) {
		if (observer == null) throw new IllegalArgumentException("observer cannot be null");
		observers.add(observer);
	}

	public void unregister(PlayerCombatHitObserver observer) {
		if (observer == null) throw new IllegalArgumentException("observer cannot be null");
		observers.remove(observer);
	}

	public void publish(PlayerCombatHitContext context) {
		if (context == null) throw new IllegalArgumentException("context cannot be null");
		for (PlayerCombatHitObserver observer : observers) {
			try {
				observer.onHit(context);
			} catch (RuntimeException exception) {
				logger.log(Level.SEVERE,
						"Player combat hit observer failed: " + observer.getClass().getName(), exception);
			}
		}
	}
}
