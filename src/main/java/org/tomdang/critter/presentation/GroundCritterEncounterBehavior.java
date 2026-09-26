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
import org.tomdang.critter.runtime.CritterRuntimeService;
import org.tomdang.critter.runtime.CritterState;
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

/** Private encounter presentation for data-defined critters using native ground navigation. */
public final class GroundCritterEncounterBehavior implements EncounterBehavior, Listener, AutoCloseable {
    private final Plugin plugin;
    private final CritterRegistry definitions;
    private final CritterRuntimeService critters;
    private final EntityAiRuntime ai;
    private final Supplier<EncounterRuntimeService> encounters;
    private final ConfiguredGoalFactory goals = new ConfiguredGoalFactory();
    private final ConfiguredNavigatorFactory navigators = new ConfiguredNavigatorFactory();
    private final Map<UUID, View> byHitbox = new HashMap<>();
    private final Map<UUID, View> byEncounter = new HashMap<>();
    private final BukkitTask presentationTask;

    public GroundCritterEncounterBehavior(Plugin plugin, CritterRegistry definitions,
            CritterRuntimeService critters, EntityAiRuntime ai,
            Supplier<EncounterRuntimeService> encounters) {
        if (plugin == null || definitions == null || critters == null || ai == null || encounters == null) {
            throw new IllegalArgumentException("Ground critter behavior dependencies are required");
        }
        this.plugin = plugin;
        this.definitions = definitions;
        this.critters = critters;
        this.ai = ai;
        this.encounters = encounters;
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
            metadata.setItemModel(new NamespacedKey("tomblock", modelId));
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
        player.showEntity(plugin, display);
        player.showEntity(plugin, hitbox);

        var critter = critters.spawn(critterId, player.getUniqueId());
        var agent = new BukkitMobGroundAgent(carrier, critterId, Set.of(AiCapability.INTERACTING));
        var brain = new AiBrain(agent,
                new BukkitPlayerSensor(player.getUniqueId(), Math.max(16, definition.hunting().awarenessRadius() * 2)),
                navigators.create(definition.ai()), goals.create(definition.ai()));
        List<AiVector> coverPoints = coverPoints(context, location);
        brain.memory().put("cover-points", coverPoints);
        brain.memory().put("cover-count", coverPoints.size());
        ai.register(brain);

        View view = new View(context.session().instanceId(), player.getUniqueId(), critter.instanceId(),
                carrier, display, hitbox, brain,
                parseDouble(context.definition().parameters().get("model-y-offset"), .15),
                parseDouble(context.definition().parameters().get("model-yaw-offset"), 0));
        byEncounter.put(view.encounterId, view);
        byHitbox.put(hitbox.getUniqueId(), view);
        context.resources().own(() -> remove(view.encounterId));
        player.sendMessage(Component.text(definition.name()
                + " tracks through the undergrowth. Approach carefully and interact to observe it.",
                NamedTextColor.GOLD));
    }

    @EventHandler(ignoreCancelled = true)
    public void interact(PlayerInteractEntityEvent event) {
        View view = byHitbox.get(event.getRightClicked().getUniqueId());
        if (view == null || !view.owner.equals(event.getPlayer().getUniqueId())) return;
        event.setCancelled(true);
        var instance = critters.find(view.critterId).orElse(null);
        if (instance == null) return;
        String activeGoal = view.brain.activeGoal().orElse("");
        if (!view.brain.memory().flag("settled")
                && (activeGoal.equals("FLEE") || activeGoal.equals("SEEK_COVER"))) {
            event.getPlayer().sendMessage(Component.text(
                    "It is too alert to study. Let it reach cover and settle.", NamedTextColor.RED));
            return;
        }
        if (instance.state() != CritterState.SETTLED) {
            critters.approach(view.critterId, false);
            critters.observe(view.critterId);
            view.brain.memory().flag("settled", true);
            event.getPlayer().sendMessage(Component.text(
                    "You document its behavior. Interact once more to complete the field test.",
                    NamedTextColor.YELLOW));
            return;
        }
        critters.capture(view.critterId);
        encounters.get().complete(view.encounterId);
        event.getPlayer().sendMessage(Component.text("Ground critter documented and released.",
                NamedTextColor.GREEN));
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
        }
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
        byHitbox.remove(view.hitbox.getUniqueId());
        ai.remove(view.carrier.getUniqueId());
        critters.find(view.critterId).ifPresent(value -> critters.escape(view.critterId));
        view.display.remove();
        view.hitbox.remove();
        view.carrier.remove();
    }

    @Override public void close() {
        presentationTask.cancel();
        for (UUID id : new ArrayList<>(byEncounter.keySet())) remove(id);
        HandlerList.unregisterAll(this);
    }

    private record View(UUID encounterId, UUID owner, UUID critterId, Mob carrier,
                        ItemDisplay display, Interaction hitbox, AiBrain brain,
                        double modelYOffset, double modelYawOffset) {}
}
