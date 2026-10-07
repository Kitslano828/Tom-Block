package org.tomdang.critter.presentation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.tomdang.critter.definition.CritterRegistry;
import org.tomdang.critter.hunting.HuntEngine;
import org.tomdang.critter.hunting.HuntGrade;
import org.tomdang.critter.hunting.HuntPhase;
import org.tomdang.critter.hunting.HuntRules;
import org.tomdang.critter.hunting.HuntState;
import org.tomdang.critter.hunting.HuntTransition;
import org.tomdang.critter.runtime.CritterRuntimeService;
import org.tomdang.encounter.runtime.EncounterBehavior;
import org.tomdang.encounter.runtime.EncounterRuntimeContext;
import org.tomdang.encounter.runtime.EncounterRuntimeService;
import org.tomdang.entityai.behavior.ConfiguredGoalFactory;
import org.tomdang.entityai.bukkit.BukkitMobGroundAgent;
import org.tomdang.entityai.bukkit.BukkitPlayerSensor;
import org.tomdang.entityai.configuration.ConfiguredNavigatorFactory;
import org.tomdang.entityai.core.AiBrain;
import org.tomdang.entityai.core.AiCapability;
import org.tomdang.entityai.core.AiVector;
import org.tomdang.entityai.runtime.EntityAiRuntime;
import org.tomdang.player.playeractionbar.PlayerActionBarService;
import org.tomdang.hud.hunting.HuntingHudModel;
import org.tomdang.hud.hunting.HuntingHudService;
import org.tomdang.activity.ActivityAccessService;
import org.tomdang.activity.ActivityInstance;
import org.tomdang.activity.bukkit.BukkitActivityEntityController;

/** Private encounter presentation for data-defined critters using native ground navigation. */
public final class GroundCritterEncounterBehavior implements EncounterBehavior, Listener, AutoCloseable {
    private static final long MINIMUM_GUIDANCE_TICKS = 120;
    private final Plugin plugin;
    private final CritterRegistry definitions;
    private final CritterRuntimeService critters;
    private final EntityAiRuntime ai;
    private final PlayerActionBarService messages;
    private final HuntingHudService huntingHud;
    private final Supplier<EncounterRuntimeService> encounters;
    private final ActivityAccessService activityAccess;
    private final BukkitActivityEntityController activityEntities;
    private final ConfiguredGoalFactory goals = new ConfiguredGoalFactory();
    private final ConfiguredNavigatorFactory navigators = new ConfiguredNavigatorFactory();
    private final Map<UUID, Clue> byClueHitbox = new HashMap<>();
    private final Map<UUID, Cover> byCoverHitbox = new HashMap<>();
    private final Map<UUID, View> byEncounter = new HashMap<>();
    private final BukkitTask presentationTask;

    public GroundCritterEncounterBehavior(Plugin plugin, CritterRegistry definitions,
            CritterRuntimeService critters, EntityAiRuntime ai,
            Supplier<EncounterRuntimeService> encounters, PlayerActionBarService messages,
            HuntingHudService huntingHud, ActivityAccessService activityAccess,
            BukkitActivityEntityController activityEntities) {
        if (plugin == null || definitions == null || critters == null || ai == null || encounters == null
                || messages == null || huntingHud == null || activityAccess == null || activityEntities == null) {
            throw new IllegalArgumentException("Ground critter behavior dependencies are required");
        }
        this.plugin = plugin;
        this.definitions = definitions;
        this.critters = critters;
        this.ai = ai;
        this.encounters = encounters;
        this.messages = messages;
        this.huntingHud = huntingHud;
        this.activityAccess = activityAccess;
        this.activityEntities = activityEntities;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        presentationTask = Bukkit.getScheduler().runTaskTimer(plugin, this::updatePresentations, 1L, 1L);
    }

    @Override public void start(EncounterRuntimeContext context) {
        Player player = Bukkit.getPlayer(context.session().ownerId());
        if (player != null) spawn(context, player);
    }
    @Override public void resume(EncounterRuntimeContext context) {
        Player player = Bukkit.getPlayer(context.session().ownerId());
        if (player != null && !byEncounter.containsKey(context.session().instanceId())) spawn(context, player);
    }
    @Override public void suspend(EncounterRuntimeContext context) { remove(context.session().instanceId()); }
    @Override public void complete(EncounterRuntimeContext context) { remove(context.session().instanceId()); }
    @Override public void fail(EncounterRuntimeContext context, String reason) { remove(context.session().instanceId()); }

    private void spawn(EncounterRuntimeContext context, Player player) {
        String critterId = context.definition().parameters().getOrDefault("critter", "MOSSBACK");
        var definition = definitions.require(critterId);
        Location location = spawnLocation(player,
                parseDouble(context.definition().parameters().get("spawn-distance"), 6));
        World world = player.getWorld();
        EntityType carrierType = carrierType(context.definition().parameters().get("carrier"));
        var rawCarrier = world.spawnEntity(location, carrierType);
        if (!(rawCarrier instanceof Mob carrier)) {
            rawCarrier.remove();
            throw new IllegalArgumentException("Ground critter carrier must be a mob: " + carrierType);
        }
        carrier.setInvisible(true);
        carrier.setInvulnerable(true);
        carrier.setSilent(true);
        carrier.setCollidable(false);
        carrier.setCanPickupItems(false);
        carrier.setPersistent(false);
        carrier.setRemoveWhenFarAway(false);
        carrier.setVisibleByDefault(false);
        Bukkit.getMobGoals().removeAllGoals(carrier);
        ItemDisplay display = world.spawn(location, ItemDisplay.class, value -> {
            ItemStack model = ItemStack.of(Material.PAPER);
            var metadata = model.getItemMeta();
            String modelId = context.definition().parameters().getOrDefault("model",
                    critterId.toLowerCase(Locale.ROOT));
            NamespacedKey modelKey = NamespacedKey.fromString(
                    modelId.contains(":") ? modelId : "tomblock:" + modelId);
            if (modelKey == null) throw new IllegalArgumentException("Invalid critter model " + modelId);
            metadata.setItemModel(modelKey);
            model.setItemMeta(metadata);
            value.setItemStack(model);
            value.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);
            value.setBillboard(Display.Billboard.FIXED);
            value.setTeleportDuration(2);
            value.setPersistent(false);
            value.setVisibleByDefault(false);
        });
        Interaction hitbox = world.spawn(location, Interaction.class, value -> {
            value.setInteractionWidth((float) parseDouble(
                    context.definition().parameters().get("hitbox-width"), 1.2));
            value.setInteractionHeight((float) parseDouble(
                    context.definition().parameters().get("hitbox-height"), .9));
            value.setResponsive(true);
            value.setPersistent(false);
            value.setVisibleByDefault(false);
        });
        var critter = critters.spawn(critterId, player.getUniqueId());
        var agent = new BukkitMobGroundAgent(carrier, critterId, Set.of(AiCapability.INTERACTING));
        var brain = new AiBrain(agent,
                new BukkitPlayerSensor(player.getUniqueId(), Math.max(16, definition.hunting().awarenessRadius() * 2)),
                navigators.create(definition.ai()), goals.create(definition.ai()));
        List<AiVector> coverPoints = coverPoints(context, location);
        brain.memory().put("cover-points", coverPoints);
        brain.memory().put("cover-count", coverPoints.size());
        int cluesRequired = parseInt(context.definition().parameters().get("clues-required"), 3);
        HuntRules huntRules = new HuntRules(cluesRequired,
                parseInt(context.definition().parameters().get("maximum-alertness"), 100),
                parseInt(context.definition().parameters().get("reckless-alertness"), 25),
                parseInt(context.definition().parameters().get("calm-recovery"), 10),
                definition.hunting().maximumRelocations(),
                parseLong(context.definition().parameters().get("capture-ready-ticks"), 20),
                definition.hunting().captureWindow().toSeconds() * 20);
        ActivityInstance activity = context.activity();
        activityEntities.publishControl(hitbox, activity);
        View view = new View(context.session().instanceId(), activity, critter.instanceId(), critterId,
                carrier, display, hitbox, brain,
                parseDouble(context.definition().parameters().get("model-y-offset"), .15),
                parseDouble(context.definition().parameters().get("model-yaw-offset"), 0),
                huntRules, new HuntEngine(huntRules), coverPoints,
                Boolean.parseBoolean(context.definition().parameters().getOrDefault("assisted", "false")));
        byEncounter.put(view.encounterId, view);
        syncDiagnostics(view);
        if (view.assisted) {
            for (int i = 0; i < cluesRequired; i++) view.huntState = view.hunt.inspectClue(view.huntState).state();
            spawnCoverChoices(view, player);
            int snareIndex = Math.floorMod(parseInt(
                    context.definition().parameters().get("preplaced-snare-index"), 0), view.covers.size());
            armTrap(player, view.covers.get(snareIndex));
        } else spawnClues(view, player, location, cluesRequired);
        updateHud(view, player, Bukkit.getCurrentTick());
        context.resources().own(() -> remove(view.encounterId));
        notify(player, Component.text(view.assisted
                ? "Will's snares are set. Move opposite the armed snare and pressure the " + name(view) + "."
                : "Follow the disturbed trail and inspect " + cluesRequired + " tracks.",
                NamedTextColor.GOLD), 80);
    }

    @EventHandler(ignoreCancelled = true)
    public void interact(PlayerInteractEntityEvent event) {
        Cover cover = byCoverHitbox.get(event.getRightClicked().getUniqueId());
        if (cover == null) return;
        event.setCancelled(true);
        if (!activityAccess.canInteract(cover.view.activity, event.getPlayer().getUniqueId())) return;
        armTrap(event.getPlayer(), cover);
    }

    private void updatePresentations() {
        for (View view : new ArrayList<>(byEncounter.values())) {
            if (!view.carrier.isValid() || view.carrier.isDead()) {
                if (byEncounter.containsKey(view.encounterId)) encounters.get().fail(view.encounterId, "CARRIER_LOST");
                continue;
            }
            Location base = view.carrier.getLocation();
            Location model = base.clone().add(0, view.modelYOffset, 0);
            model.setYaw(model.getYaw() + (float) view.modelYawOffset);
            view.display.teleport(model);
            view.hitbox.teleport(base);
            tickHunt(view);
        }
    }

    private void tickHunt(View view) {
        Player player = Bukkit.getPlayer(view.activity.ownerId());
        if (player == null) return;
        long tick = Bukkit.getCurrentTick();
        if (view.huntState.phase() == HuntPhase.TRACKING) inspectVisibleClue(view, player);
        if (view.flushActive) {
            resolveFlush(view, player);
            return;
        }
        if (tick - view.lastHudTick >= 2) {
            view.lastHudTick = tick;
            updateHud(view, player, tick);
        }
        if (view.huntState.phase() != HuntPhase.APPROACH || tick - view.lastAwarenessTick < 5) return;
        view.lastAwarenessTick = tick;
        double awareness = definitions.require(view.definitionId).hunting().awarenessRadius();
        if (!player.getWorld().equals(view.carrier.getWorld())
                || player.getLocation().distanceSquared(view.carrier.getLocation()) > awareness * awareness) return;
        var velocity = player.getVelocity();
        double horizontalSpeed = Math.hypot(velocity.getX(), velocity.getZ());
        boolean reckless = player.isSprinting() || !player.isOnGround()
                || (!player.isSneaking() && horizontalSpeed > .075);
        int previous = view.huntState.alertness();
        var update = view.hunt.sampleApproach(view.huntState, reckless);
        view.huntState = update.state();
        syncDiagnostics(view);
        if (update.transition() == HuntTransition.FLUSH_READY) {
            startFlush(view, player);
        } else if (reckless && previous < 50 && view.huntState.alertness() >= 50) {
            notify(player, Component.text("Pressure rising—keep the snare behind the " + name(view) + ".",
                    NamedTextColor.YELLOW), 35);
        }
    }

    private void inspectVisibleClue(View view, Player player) {
        int index = view.huntState.cluesFound();
        if (index >= view.clues.size()) return;
        Clue clue = view.clues.get(index);
        Location eye = player.getEyeLocation();
        org.bukkit.util.Vector toward = clue.marker.getLocation().toVector().subtract(eye.toVector());
        double distance = toward.length();
        if (distance > 2.2 || distance < .01) { clue.focusTicks = 0; return; }
        double alignment = eye.getDirection().normalize().dot(toward.normalize());
        if (alignment < .78 || !player.hasLineOfSight(clue.marker)) { clue.focusTicks = 0; return; }
        if (++clue.focusTicks >= 12) inspectClue(player, clue);
    }

    private void armTrap(Player player, Cover selected) {
        View view = selected.view;
        var update = view.hunt.armTrap(view.huntState);
        if (update.transition() != HuntTransition.TRAP_ARMED) return;
        view.huntState = update.state();
        view.snare = selected;
        for (Cover cover : new ArrayList<>(view.covers)) {
            byCoverHitbox.remove(cover.hitbox.getUniqueId());
            activityEntities.unregister(cover.hitbox);
            cover.hitbox.remove();
            if (cover != selected && !view.assisted) {
                activityEntities.unregister(cover.marker);
                cover.marker.remove();
            }
        }
        selected.marker.setItemStack(ItemStack.of(Material.COBWEB));
        activityEntities.publishVisual(selected.marker, view.activity);
        activityEntities.publishVisual(view.display, view.activity);
        ai.register(view.brain);
        syncDiagnostics(view);
        notify(player, Component.text("Snare armed. Move opposite it and pressure the " + name(view) + " toward it.",
                NamedTextColor.GREEN), 60);
    }

    private void startFlush(View view, Player player) {
        view.flushActive = true;
        view.brain.memory().flag("settled", false);
        view.brain.memory().flag("seek-cover", true);
        Location location = player.getLocation();
        view.brain.memory().put("threat-position", new AiVector(location.getX(), location.getY(), location.getZ()));
        notify(player, Component.text("The " + name(view) + " bolts for cover!", NamedTextColor.GOLD), 35);
    }

    private void resolveFlush(View view, Player player) {
        if (view.snare != null && view.carrier.getLocation().distanceSquared(view.snare.location) <= 1.3 * 1.3) {
            view.huntState = view.hunt.trapCaptured(view.huntState).state();
            HuntGrade grade = HuntGrade.fromRelocations(view.huntState.relocations());
            critters.approach(view.critterId, false);
            critters.observe(view.critterId);
            critters.capture(view.critterId, grade);
            encounters.get().complete(view.encounterId);
            notify(player, Component.text(name(view) + " safely snared and released — " + grade.name() + " hunt!",
                    NamedTextColor.GREEN), 60);
            return;
        }
        if (!view.brain.memory().flag("settled")) return;
        var update = view.hunt.missTrap(view.huntState);
        view.huntState = update.state();
        view.flushActive = false;
        removeCovers(view);
        if (update.transition() == HuntTransition.ESCAPED) {
            escape(view, player, "The " + name(view) + " evaded too many snares and escaped.");
            return;
        }
        spawnCoverChoices(view, player);
        syncDiagnostics(view);
        notify(player, Component.text("It reached different cover. Choose a new snare position.",
                NamedTextColor.RED), 55);
    }

    private void spawnClues(View view, Player player, Location destination, int count) {
        Location origin = player.getLocation();
        for (int index = 0; index < count; index++) {
            double progress = (index + 1.0) / (count + 1.0);
            double x = origin.getX() + (destination.getX() - origin.getX()) * progress;
            double z = origin.getZ() + (destination.getZ() - origin.getZ()) * progress;
            Location location = ground(player.getWorld(), x, z, origin.getBlockY())
                    .orElse(origin.clone()).add(0, .04, 0);
            ItemDisplay marker = player.getWorld().spawn(location, ItemDisplay.class, value -> {
                value.setItemStack(ItemStack.of(Material.MOSS_CARPET));
                value.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.GROUND);
                value.setBillboard(Display.Billboard.FIXED);
                value.setPersistent(false);
                value.setVisibleByDefault(false);
            });
            Interaction hitbox = player.getWorld().spawn(location, Interaction.class, value -> {
                value.setInteractionWidth(.9f);
                value.setInteractionHeight(.45f);
                value.setResponsive(true);
                value.setPersistent(false);
                value.setVisibleByDefault(false);
            });
            if (index == 0) {
                player.showEntity(plugin, marker);
                player.showEntity(plugin, hitbox);
            }
            Clue clue = new Clue(view, index, marker, hitbox);
            view.clues.add(clue);
            byClueHitbox.put(hitbox.getUniqueId(), clue);
            activityEntities.publishControl(marker, view.activity);
            activityEntities.publishControl(hitbox, view.activity);
            if (index != 0) {
                player.hideEntity(plugin, marker);
                player.hideEntity(plugin, hitbox);
            }
        }
    }

    private void inspectClue(Player player, Clue clue) {
        View view = clue.view;
        if (!activityAccess.canInteract(view.activity, player.getUniqueId()) || clue.found
                || view.huntState.phase() != HuntPhase.TRACKING) return;
        if (clue.index != view.huntState.cluesFound()) return;
        clue.found = true;
        byClueHitbox.remove(clue.hitbox.getUniqueId());
        activityEntities.unregister(clue.marker);
        activityEntities.unregister(clue.hitbox);
        clue.marker.remove();
        clue.hitbox.remove();
        var update = view.hunt.inspectClue(view.huntState);
        view.huntState = update.state();
        syncDiagnostics(view);
        int nextIndex = clue.index + 1;
        if (nextIndex < view.clues.size()) {
            Clue next = view.clues.get(nextIndex);
            player.showEntity(plugin, next.marker);
            player.showEntity(plugin, next.hitbox);
        }
        if (update.transition() == HuntTransition.TRAIL_COMPLETED) {
            spawnCoverChoices(view, player);
            notify(player, Component.text("Trail complete. Choose a cover point for your snare.",
                    NamedTextColor.GREEN), 60);
        } else {
            notify(player, Component.text("Track found (" + view.huntState.cluesFound() + "/"
                    + view.huntRules().cluesRequired() + ").", NamedTextColor.YELLOW), 35);
        }
    }

    private void spawnCoverChoices(View view, Player player) {
        removeCovers(view);
        for (AiVector point : view.coverPoints) {
            Location location = new Location(player.getWorld(), point.x(), point.y() + .05, point.z());
            ItemDisplay marker = player.getWorld().spawn(location, ItemDisplay.class, value -> {
                value.setItemStack(ItemStack.of(Material.TRIPWIRE_HOOK));
                value.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.GROUND);
                value.setBillboard(Display.Billboard.FIXED);
                value.setPersistent(false);
                value.setVisibleByDefault(false);
            });
            Interaction hitbox = player.getWorld().spawn(location, Interaction.class, value -> {
                value.setInteractionWidth(1.2f);
                value.setInteractionHeight(.7f);
                value.setResponsive(true);
                value.setPersistent(false);
                value.setVisibleByDefault(false);
            });
            Cover cover = new Cover(view, location, marker, hitbox);
            view.covers.add(cover);
            byCoverHitbox.put(hitbox.getUniqueId(), cover);
            activityEntities.publishControl(marker, view.activity);
            activityEntities.publishControl(hitbox, view.activity);
        }
    }

    private void removeCovers(View view) {
        for (Cover cover : view.covers) {
            byCoverHitbox.remove(cover.hitbox.getUniqueId());
            activityEntities.unregister(cover.marker);
            activityEntities.unregister(cover.hitbox);
            cover.marker.remove();
            cover.hitbox.remove();
        }
        view.covers.clear();
        view.snare = null;
    }

    private void escape(View view, Player player, String reason) {
        if (!byEncounter.containsKey(view.encounterId)) return;
        notify(player, Component.text(reason, NamedTextColor.RED), 60);
        encounters.get().fail(view.encounterId, "CRITTER_ESCAPED");
    }

    private void notify(Player player, Component message, long duration) {
        messages.showTemporaryMessage(player, message, Math.max(duration, MINIMUM_GUIDANCE_TICKS));
    }

    private void syncDiagnostics(View view) {
        view.brain.memory().put("hunt-phase", view.huntState.phase().name());
        view.brain.memory().put("hunt-clues", view.huntState.cluesFound());
        view.brain.memory().put("hunt-alertness", view.huntState.alertness());
        view.brain.memory().put("hunt-relocations", view.huntState.relocations());
        if (view.snare == null) view.brain.memory().remove("hunt-snare");
        else view.brain.memory().put("hunt-snare", new AiVector(view.snare.location.getX(),
                view.snare.location.getY(), view.snare.location.getZ()));
    }

    private void updateHud(View view, Player player, long tick) {
        HuntState state = view.huntState;
        String instruction = switch (state.phase()) {
            case TRACKING -> "FOLLOW THE TRAIL";
            case TRAP_PLACEMENT -> "CHOOSE SNARE COVER";
            case APPROACH -> "PRESSURE TOWARD SNARE";
            case CAPTURE_WINDOW -> tick < state.captureReadyAtTick() ? "HOLD STEADY" : "INTERACT NOW";
            case COMPLETED -> "HUNT COMPLETE";
            case ESCAPED -> "CRITTER ESCAPED";
        };
        long remaining = state.phase() == HuntPhase.CAPTURE_WINDOW
                ? Math.max(0, state.captureExpiresAtTick() - tick) : 0;
        huntingHud.show(player.getUniqueId(), new HuntingHudModel(
                definitions.require(view.definitionId).name(), instruction,
                state.cluesFound(), view.huntRules.cluesRequired(), state.alertness(),
                view.huntRules.maximumAlertness(), remaining,
                HuntGrade.fromRelocations(state.relocations()).name()));
    }

    private Location spawnLocation(Player player, double distance) {
        var direction = player.getLocation().getDirection().setY(0);
        if (direction.lengthSquared() < .0001) direction.setX(1);
        direction.normalize().multiply(distance);
        Location candidate = player.getLocation().clone().add(direction);
        return ground(player.getWorld(), candidate.getX(), candidate.getZ(), player.getLocation().getBlockY())
                .orElse(player.getLocation().clone());
    }

    private List<AiVector> coverPoints(EncounterRuntimeContext context, Location home) {
        String encoded = context.definition().parameters().getOrDefault("cover-offsets",
                "-7,5;7,5;-7,-5;7,-5");
        List<AiVector> points = new ArrayList<>();
        for (String value : encoded.split(";")) {
            String[] coordinates = value.trim().split(",");
            if (coordinates.length != 2) continue;
            try {
                double x = home.getX() + Double.parseDouble(coordinates[0].trim());
                double z = home.getZ() + Double.parseDouble(coordinates[1].trim());
                ground(home.getWorld(), x, z, home.getBlockY()).ifPresent(point ->
                        points.add(new AiVector(point.getX(), point.getY(), point.getZ())));
            } catch (NumberFormatException ignored) {
                // Invalid authored offsets are skipped; an empty result is visible in AI diagnostics.
            }
        }
        return List.copyOf(points);
    }

    private java.util.Optional<Location> ground(World world, double x, double z, int originY) {
        int blockX = (int) Math.floor(x), blockZ = (int) Math.floor(z);
        int top = Math.min(world.getMaxHeight() - 2, originY + 7);
        int bottom = Math.max(world.getMinHeight() + 1, originY - 10);
        for (int y = top; y >= bottom; y--) {
            if (!world.getBlockAt(blockX, y - 1, blockZ).isPassable()
                    && world.getBlockAt(blockX, y, blockZ).isPassable()
                    && world.getBlockAt(blockX, y + 1, blockZ).isPassable()) {
                return java.util.Optional.of(new Location(world, blockX + .5, y, blockZ + .5));
            }
        }
        return java.util.Optional.empty();
    }

    private double parseDouble(String value, double fallback) {
        if (value == null) return fallback;
        try { return Double.parseDouble(value); }
        catch (NumberFormatException ignored) { return fallback; }
    }

    private int parseInt(String value, int fallback) {
        if (value == null) return fallback;
        try { return Integer.parseInt(value); }
        catch (NumberFormatException ignored) { return fallback; }
    }

    private long parseLong(String value, long fallback) {
        if (value == null) return fallback;
        try { return Long.parseLong(value); }
        catch (NumberFormatException ignored) { return fallback; }
    }

    private EntityType carrierType(String configured) {
        String name = configured == null ? "SILVERFISH" : configured.trim().toUpperCase(Locale.ROOT);
        try { return EntityType.valueOf(name); }
        catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Unknown ground critter carrier " + name, exception);
        }
    }

    private void remove(UUID encounterId) {
        View view = byEncounter.remove(encounterId);
        if (view == null) return;
        huntingHud.hide(view.activity.ownerId());
        removeCovers(view);
        for (Clue clue : view.clues) {
            byClueHitbox.remove(clue.hitbox.getUniqueId());
            activityEntities.unregister(clue.marker);
            activityEntities.unregister(clue.hitbox);
            clue.marker.remove();
            clue.hitbox.remove();
        }
        ai.remove(view.carrier.getUniqueId());
        critters.find(view.critterId).ifPresent(value -> critters.escape(view.critterId));
        activityEntities.unregister(view.display);
        activityEntities.unregister(view.hitbox);
        view.display.remove();
        view.hitbox.remove();
        view.carrier.remove();
    }

    @Override public void close() {
        presentationTask.cancel();
        for (UUID id : new ArrayList<>(byEncounter.keySet())) remove(id);
        HandlerList.unregisterAll(this);
    }

    private String name(View view) { return definitions.require(view.definitionId).name(); }

    private static final class View {
        private final UUID encounterId;
        private final ActivityInstance activity;
        private final UUID critterId;
        private final String definitionId;
        private final Mob carrier;
        private final ItemDisplay display;
        private final Interaction hitbox;
        private final AiBrain brain;
        private final double modelYOffset;
        private final double modelYawOffset;
        private final HuntRules huntRules;
        private final HuntEngine hunt;
        private final List<AiVector> coverPoints;
        private final boolean assisted;
        private final List<Clue> clues = new ArrayList<>();
        private final List<Cover> covers = new ArrayList<>();
        private Cover snare;
        private boolean flushActive;
        private HuntState huntState = HuntState.start();
        private long lastAwarenessTick;
        private long lastHudTick;

        private View(UUID encounterId, ActivityInstance activity, UUID critterId, String definitionId, Mob carrier,
                ItemDisplay display, Interaction hitbox, AiBrain brain,
                double modelYOffset, double modelYawOffset, HuntRules huntRules, HuntEngine hunt,
                List<AiVector> coverPoints, boolean assisted) {
            this.encounterId = encounterId;
            this.activity = activity;
            this.critterId = critterId;
            this.definitionId = definitionId;
            this.carrier = carrier;
            this.display = display;
            this.hitbox = hitbox;
            this.brain = brain;
            this.modelYOffset = modelYOffset;
            this.modelYawOffset = modelYawOffset;
            this.huntRules = huntRules;
            this.hunt = hunt;
            this.coverPoints = List.copyOf(coverPoints);
            this.assisted = assisted;
        }

        private HuntRules huntRules() { return huntRules; }
    }

    private static final class Clue {
        private final View view;
        private final int index;
        private final ItemDisplay marker;
        private final Interaction hitbox;
        private boolean found;
        private int focusTicks;
        private Clue(View view, int index, ItemDisplay marker, Interaction hitbox) {
            this.view = view;
            this.index = index;
            this.marker = marker;
            this.hitbox = hitbox;
        }
    }

    private static final class Cover {
        private final View view;
        private final Location location;
        private final ItemDisplay marker;
        private final Interaction hitbox;
        private Cover(View view, Location location, ItemDisplay marker, Interaction hitbox) {
            this.view = view;
            this.location = location;
            this.marker = marker;
            this.hitbox = hitbox;
        }
    }
}
