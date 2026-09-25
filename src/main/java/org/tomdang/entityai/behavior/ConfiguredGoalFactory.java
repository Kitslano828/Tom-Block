package org.tomdang.entityai.behavior;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.tomdang.entityai.configuration.AiGoalDefinition;
import org.tomdang.entityai.configuration.AiProfile;
import org.tomdang.entityai.core.AiBehavior;
import org.tomdang.entityai.core.AiBehaviorStatus;
import org.tomdang.entityai.core.AiContext;
import org.tomdang.entityai.core.AiGoal;
import org.tomdang.entityai.core.AiVector;
import org.tomdang.entityai.core.CombatAiAgent;
import org.tomdang.entityai.core.PerceivedEntity;
import org.tomdang.entityai.navigation.NavigationRequest;
import org.tomdang.entityai.navigation.NavigationStatus;

/** Creates reusable goals for critters, combat mobs, NPCs, and companions. */
public final class ConfiguredGoalFactory {
    public List<AiGoal> create(AiProfile profile) { return profile.goals().stream().map(this::create).toList(); }

    private AiGoal create(AiGoalDefinition definition) {
        return switch (definition.type()) {
            case "IDLE" -> new Idle(definition);
            case "REST" -> new Rest(definition);
            case "WANDER", "PATROL" -> new Wander(definition);
            case "INVESTIGATE_PLAYER" -> new ApproachTarget(definition, entity -> true, false);
            case "FOLLOW_OWNER" -> new ApproachTarget(definition, PerceivedEntity::owner, false);
            case "CHASE" -> new ApproachTarget(definition, PerceivedEntity::hostile, true);
            case "FLEE_FROM_PLAYER" -> new Flee(definition);
            case "RETURN_HOME" -> new ReturnHome(definition);
            case "ATTACK" -> new Attack(definition);
            case "SETTLE" -> new Settle(definition);
            default -> throw new IllegalArgumentException("Unsupported AI goal type " + definition.type());
        };
    }

    private abstract static class Goal implements AiGoal {
        final AiGoalDefinition definition;
        Goal(AiGoalDefinition definition) { this.definition = definition; }
        @Override public String id() { return definition.id(); }
        @Override public int priority() { return definition.priority(); }
        double number(String key, double fallback) {
            String value = definition.parameters().get(key);
            return value == null ? fallback : Double.parseDouble(value);
        }
        long ticks(String key, double fallbackSeconds) {
            return Math.max(0, Math.round(number(key, fallbackSeconds) * 20));
        }
    }

    private static final class Idle extends Goal {
        Idle(AiGoalDefinition definition) { super(definition); }
        @Override public boolean canStart(AiContext context) { return true; }
        @Override public AiBehavior start(AiContext context) { return ignored -> AiBehaviorStatus.RUNNING; }
    }

    private static final class Settle extends Goal {
        Settle(AiGoalDefinition definition) { super(definition); }
        @Override public boolean canStart(AiContext context) { return context.memory().flag("settled"); }
        @Override public AiBehavior start(AiContext context) { return ignored -> AiBehaviorStatus.RUNNING; }
    }

    private static final class Rest extends Goal {
        Rest(AiGoalDefinition definition) { super(definition); }
        @Override public boolean canStart(AiContext context) {
            return context.tick() < context.memory().get("next-wander", Long.class).orElse(0L);
        }
        @Override public AiBehavior start(AiContext context) { return ignored -> AiBehaviorStatus.RUNNING; }
    }

    private static final class Wander extends Goal {
        Wander(AiGoalDefinition definition) { super(definition); }
        @Override public boolean canStart(AiContext context) {
            return !context.memory().flag("settled")
                    && context.tick() >= context.memory().get("next-wander", Long.class).orElse(0L);
        }
        @Override public AiBehavior start(AiContext context) {
            double radius = number("radius", 6);
            double height = number("preferred-height", 1.5);
            double angle = ThreadLocalRandom.current().nextDouble(Math.PI * 2);
            double distance = ThreadLocalRandom.current().nextDouble(radius * .35, radius);
            AiVector home = context.agent().home();
            AiVector destination = new AiVector(home.x() + Math.cos(angle) * distance,
                    home.y() + ThreadLocalRandom.current().nextDouble(-.5, .5),
                    home.z() + Math.sin(angle) * distance);
            return navigate(destination, number("speed", .8), .25, radius + 1, height,
                    current -> current.memory().put("next-wander",
                            current.tick() + ticks("rest-seconds", 3)));
        }
    }

    private static final class ApproachTarget extends Goal {
        private final Predicate<PerceivedEntity> selector;
        private final boolean requireRange;
        ApproachTarget(AiGoalDefinition definition, Predicate<PerceivedEntity> selector, boolean requireRange) {
            super(definition);
            this.selector = selector;
            this.requireRange = requireRange;
        }
        @Override public boolean canStart(AiContext context) { return target(context).isPresent(); }
        @Override public AiBehavior start(AiContext context) {
            PerceivedEntity target = target(context).orElseThrow();
            AiVector away = context.agent().position().subtract(target.position()).normalized();
            AiVector destination = target.position().add(away.multiply(number("stop-distance", 2.2)));
            return navigate(destination, number("speed", .8), .35,
                    number("territory-radius", 16), number("preferred-height", 1.5), ignored -> {});
        }
        private java.util.Optional<PerceivedEntity> target(AiContext context) {
            double range = number("trigger-distance", requireRange ? 12 : Double.MAX_VALUE);
            return context.perception().entities().stream().filter(PerceivedEntity::visible).filter(selector)
                    .filter(entity -> entity.position().distanceSquared(context.agent().position()) <= range * range)
                    .min(java.util.Comparator.comparingDouble(
                            entity -> entity.position().distanceSquared(context.agent().position())));
        }
    }

    private static final class Flee extends Goal {
        Flee(AiGoalDefinition definition) { super(definition); }
        @Override public boolean canStart(AiContext context) {
            double range = number("trigger-distance", 4);
            double threshold = number("minimum-player-speed", .09);
            return !context.memory().flag("settled") && context.perception().nearest(context.agent().position())
                    .filter(entity -> entity.position().distanceSquared(context.agent().position()) <= range * range
                            && entity.velocity().lengthSquared() >= threshold * threshold).isPresent();
        }
        @Override public AiBehavior start(AiContext context) {
            PerceivedEntity threat = context.perception().nearest(context.agent().position()).orElseThrow();
            AiVector away = context.agent().position().subtract(threat.position()).normalized();
            if (away.lengthSquared() < .1) away = new AiVector(1, 0, 0);
            AiVector destination = context.agent().position().add(away.multiply(number("distance", 6)))
                    .add(new AiVector(0, number("rise", .75), 0));
            return navigate(destination, number("speed", 1.4), .35, number("territory-radius", 10),
                    number("preferred-height", 1.5), current -> current.memory().put("next-wander",
                            current.tick() + ticks("rest-seconds", 2)));
        }
    }

    private static final class ReturnHome extends Goal {
        ReturnHome(AiGoalDefinition definition) { super(definition); }
        @Override public boolean canStart(AiContext context) {
            double distance = number("trigger-distance", 8);
            return context.agent().position().distanceSquared(context.agent().home()) > distance * distance;
        }
        @Override public AiBehavior start(AiContext context) {
            return navigate(context.agent().home(), number("speed", 1), number("arrival-radius", .5),
                    0, number("preferred-height", 0), ignored -> {});
        }
    }

    private static final class Attack extends Goal {
        Attack(AiGoalDefinition definition) { super(definition); }
        @Override public boolean canStart(AiContext context) {
            if (!(context.agent() instanceof CombatAiAgent)) return false;
            double range = number("range", 2.5);
            return context.perception().entities().stream().filter(PerceivedEntity::visible)
                    .filter(PerceivedEntity::hostile).anyMatch(entity ->
                            entity.position().distanceSquared(context.agent().position()) <= range * range);
        }
        @Override public AiBehavior start(AiContext context) {
            PerceivedEntity target = context.perception().entities().stream().filter(PerceivedEntity::visible)
                    .filter(PerceivedEntity::hostile).min(java.util.Comparator.comparingDouble(entity ->
                            entity.position().distanceSquared(context.agent().position()))).orElseThrow();
            return current -> ((CombatAiAgent) current.agent()).attack(target.id())
                    ? AiBehaviorStatus.SUCCEEDED : AiBehaviorStatus.FAILED;
        }
    }

    private static AiBehavior navigate(AiVector destination, double speed, double arrivalRadius,
            double maximumHomeDistance, double preferredHeight, Consumer<AiContext> completion) {
        return new Navigate(new NavigationRequest(destination, speed, arrivalRadius,
                maximumHomeDistance, preferredHeight), completion);
    }

    private static final class Navigate implements AiBehavior {
        private final NavigationRequest request;
        private final Consumer<AiContext> completion;
        Navigate(NavigationRequest request, Consumer<AiContext> completion) {
            this.request = request;
            this.completion = completion;
        }
        @Override public AiBehaviorStatus tick(AiContext context) {
            NavigationStatus status = context.navigator().navigate(context.agent(), request, context.tick());
            if (status == NavigationStatus.RUNNING) return AiBehaviorStatus.RUNNING;
            if (status == NavigationStatus.ARRIVED) {
                completion.accept(context);
                return AiBehaviorStatus.SUCCEEDED;
            }
            return AiBehaviorStatus.FAILED;
        }
        @Override public void stop(AiContext context, boolean interrupted) {
            context.navigator().cancel(context.agent());
        }
    }
}
