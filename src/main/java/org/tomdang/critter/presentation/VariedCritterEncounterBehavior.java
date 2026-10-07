package org.tomdang.critter.presentation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.tomdang.activity.ActivityAccessService;
import org.tomdang.activity.ActivityInstance;
import org.tomdang.activity.bukkit.BukkitActivityEntityController;
import org.tomdang.critter.definition.CritterRegistry;
import org.tomdang.critter.runtime.CritterRuntimeService;
import org.tomdang.encounter.runtime.EncounterBehavior;
import org.tomdang.encounter.runtime.EncounterRuntimeContext;
import org.tomdang.encounter.runtime.EncounterRuntimeService;

/** Distinct, lightweight field mechanics for critters that are not snare hunts. */
public final class VariedCritterEncounterBehavior implements EncounterBehavior, Listener, AutoCloseable {
    private final Plugin plugin;
    private final CritterRegistry definitions;
    private final CritterRuntimeService critters;
    private final Supplier<EncounterRuntimeService> encounters;
    private final ActivityAccessService access;
    private final BukkitActivityEntityController entities;
    private final Map<UUID, View> byEncounter = new HashMap<>();
    private final Map<UUID, Node> byHitbox = new HashMap<>();
    private final BukkitTask task;

    public VariedCritterEncounterBehavior(Plugin plugin, CritterRegistry definitions,
            CritterRuntimeService critters, Supplier<EncounterRuntimeService> encounters,
            ActivityAccessService access, BukkitActivityEntityController entities) {
        if (plugin == null || definitions == null || critters == null || encounters == null
                || access == null || entities == null) throw new IllegalArgumentException("Varied hunt dependencies are required");
        this.plugin = plugin;
        this.definitions = definitions;
        this.critters = critters;
        this.encounters = encounters;
        this.access = access;
        this.entities = entities;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 1L, 1L);
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
        String critterId = context.definition().parameters().get("critter");
        Mechanic mechanic = Mechanic.valueOf(context.definition().parameters().get("mechanic"));
        definitions.require(critterId);
        Location center = center(player, 6.0);
        var instance = critters.spawn(critterId, player.getUniqueId());
        View view = new View(context.session().instanceId(), context.activity(), instance.instanceId(), critterId,
                mechanic, center, Bukkit.getCurrentTick());
        byEncounter.put(view.encounterId, view);
        switch (mechanic) {
            case GLIMMER_TIMING -> spawnGlimmer(view);
            case BRAMBLE_BAIT -> spawnBramble(view);
            case DEW_INTERCEPT -> spawnDewhopper(view);
            case BURROW_EXITS -> spawnBurrows(view);
            case CANOPY_ECHO -> spawnCanopy(view);
            case SPORE_SEQUENCE -> spawnSporeling(view);
        }
        context.resources().own(() -> remove(view.encounterId));
        tell(player, instruction(mechanic), NamedTextColor.GOLD);
    }

    private void spawnGlimmer(View view) {
        for (int i = 0; i < 3; i++) addNode(view, point(view.center, i, 2.8, 1.3), Material.AMETHYST_SHARD, i);
        showOnly(view, 0);
    }

    private void spawnBramble(View view) {
        addNode(view, surfaceAt(view.center.clone()), Material.SWEET_BERRIES, 0);
        addNode(view, surfaceAt(view.center.clone().add(1.8, 0, 0)), Material.BROWN_DYE, 1);
    }

    private void spawnDewhopper(View view) {
        for (int i = 0; i < 3; i++) addNode(view, point(view.center, i, 3.2, .25), Material.LILY_PAD, i);
        view.target = 1;
    }

    private void spawnBurrows(View view) {
        for (int i = 0; i < 3; i++) addNode(view, point(view.center, i, 2.7, .15), Material.ROOTED_DIRT, i);
        view.target = Math.floorMod(view.encounterId.hashCode(), 3);
    }

    private void spawnCanopy(View view) {
        for (int i = 0; i < 3; i++) addNode(view, point(view.center, i, 4.0, 3.0), Material.FEATHER, i);
        view.target = Math.floorMod(view.encounterId.hashCode(), 3);
    }

    private void spawnSporeling(View view) {
        addNode(view, surfaceAt(view.center.clone().add(-2, 0, 0)), Material.RED_MUSHROOM, 0);
        addNode(view, surfaceAt(view.center.clone().add(0, 0, 2)), Material.BROWN_MUSHROOM, 1);
        addNode(view, surfaceAt(view.center.clone().add(2, 0, 0)), Material.RED_MUSHROOM, 2);
        addNode(view, surfaceAt(view.center.clone()), Material.MUSHROOM_STEM, 3);
        setActive(view.nodes.get(3), false);
    }

    @EventHandler(ignoreCancelled = true)
    public void interact(PlayerInteractEntityEvent event) {
        Node node = byHitbox.get(event.getRightClicked().getUniqueId());
        if (node == null) return;
        event.setCancelled(true);
        View view = node.view;
        Player player = event.getPlayer();
        if (!access.canInteract(view.activity, player.getUniqueId())) return;
        switch (view.mechanic) {
            case GLIMMER_TIMING -> glimmerClick(view, node, player);
            case BRAMBLE_BAIT -> brambleClick(view, node, player);
            case DEW_INTERCEPT -> dewClick(view, node, player);
            case BURROW_EXITS -> burrowClick(view, node, player);
            case CANOPY_ECHO -> canopyClick(view, node, player);
            case SPORE_SEQUENCE -> sporeClick(view, node, player);
        }
    }

    private void glimmerClick(View view, Node node, Player player) {
        if (node.index != view.target || !view.windowOpen) {
            tell(player, "It flashes away. Wait for the next landing.", NamedTextColor.YELLOW);
            advanceTarget(view);
            return;
        }
        finish(view, player, "You net the Glimmerfly during its brief landing and release it.");
    }

    private void brambleClick(View view, Node node, Player player) {
        if (view.stage == 0 && node.index == 0) {
            view.stage = 1;
            view.startedTick = Bukkit.getCurrentTick();
            tell(player, "Bait placed. Back away at least 5 blocks, crouch, and remain still.", NamedTextColor.YELLOW);
        } else if (view.stage == 2 && node.index == 1) {
            finish(view, player, "The distracted Bramblehog yields a bristle before wandering off.");
        } else if (node.index == 1) tell(player, "It curls into its thorny shell. Give the bait space.", NamedTextColor.RED);
    }

    private void dewClick(View view, Node node, Player player) {
        if (view.windowOpen && node.index == view.target) {
            finish(view, player, "You intercept the Dewhopper at its predicted landing point.");
        } else {
            tell(player, "Missed—the ripples reveal where it will land next.", NamedTextColor.YELLOW);
            advanceTarget(view);
        }
    }

    private void burrowClick(View view, Node node, Player player) {
        if (view.stage < 2) {
            if (node.index == view.target) {
                tell(player, "That exit is active; blocking it would make the Burrowtail retreat. Try another.", NamedTextColor.RED);
                return;
            }
            if (view.selected.contains(node.index)) return;
            view.selected.add(node.index);
            view.stage++;
            node.display.setItemStack(ItemStack.of(Material.COBBLESTONE));
            tell(player, view.stage == 2 ? "Two exits sealed. The Burrowtail is surfacing!" : "Exit sealed. Find the other false tunnel.", NamedTextColor.YELLOW);
            if (view.stage == 2) view.nodes.get(view.target).display.setItemStack(ItemStack.of(Material.FLINT));
            return;
        }
        if (node.index == view.target) finish(view, player, "You safely flush and document the Burrowtail.");
    }

    private void canopyClick(View view, Node node, Player player) {
        if (view.stage < 3) {
            tell(player, "Listen for three calls before choosing the source.", NamedTextColor.YELLOW);
        } else if (node.index == view.target) {
            finish(view, player, "You locate the Screecher by its echo and net it between perches.");
        } else {
            view.stage = 0;
            tell(player, "Only an echo. Listen again from the clearing.", NamedTextColor.RED);
        }
    }

    private void sporeClick(View view, Node node, Player player) {
        int[] sequence = {0, 1, 0, 2};
        if (view.stage >= sequence.length) {
            if (node.index == 3) finish(view, player, "The mushroom pattern awakens a Sporeling. You document and release it.");
            return;
        }
        if (node.index != sequence[view.stage]) {
            view.stage = 0;
            tell(player, "The spores dim. The pattern restarts with the left red cap.", NamedTextColor.RED);
            return;
        }
        view.stage++;
        player.getWorld().spawnParticle(Particle.SPORE_BLOSSOM_AIR, node.display.getLocation(), 8, .2, .2, .2, 0);
        if (view.stage == sequence.length) {
            setActive(view.nodes.get(3), true);
            tell(player, "The roots stir—the Sporeling has emerged in the center!", NamedTextColor.GREEN);
        } else tell(player, "The cap answers. Continue the pattern.", NamedTextColor.YELLOW);
    }

    private void tick() {
        long tick = Bukkit.getCurrentTick();
        for (View view : new ArrayList<>(byEncounter.values())) {
            Player player = Bukkit.getPlayer(view.activity.ownerId());
            if (player == null) continue;
            switch (view.mechanic) {
                case GLIMMER_TIMING -> tickGlimmer(view, player, tick);
                case BRAMBLE_BAIT -> tickBramble(view, player);
                case DEW_INTERCEPT -> tickDew(view, player, tick);
                case CANOPY_ECHO -> tickCanopy(view, player, tick);
                case SPORE_SEQUENCE -> tickSpore(view, tick);
                case BURROW_EXITS -> { }
            }
        }
    }

    private void tickGlimmer(View view, Player player, long tick) {
        long phase = (tick - view.startedTick) % 70;
        view.windowOpen = phase >= 42 && phase < 62;
        if (phase == 0) advanceTarget(view);
        if (view.windowOpen) player.getWorld().spawnParticle(Particle.END_ROD,
                view.nodes.get(view.target).display.getLocation(), 1, .12, .12, .12, 0);
    }

    private void tickBramble(View view, Player player) {
        if (view.stage != 1) return;
        double distance = player.getLocation().distance(view.center);
        if (distance >= 5 && player.isSneaking() && player.getVelocity().lengthSquared() < .01) view.progress++;
        else view.progress = 0;
        if (view.progress >= 60) {
            view.stage = 2;
            tell(player, "The Bramblehog lowers its crest to eat. Approach it now.", NamedTextColor.GREEN);
        }
    }

    private void tickDew(View view, Player player, long tick) {
        long phase = (tick - view.startedTick) % 70;
        if (phase == 0) { advanceTarget(view); view.windowOpen = false; }
        if (phase < 38) player.getWorld().spawnParticle(Particle.SPLASH,
                view.nodes.get(view.target).display.getLocation().clone().add(0, .3, 0), 2, .3, .05, .3, 0);
        view.windowOpen = phase >= 38 && phase < 58;
        if (phase == 38) tell(player, "Now—intercept the marked landing!", NamedTextColor.AQUA);
    }

    private void tickCanopy(View view, Player player, long tick) {
        long elapsed = tick - view.startedTick;
        if (view.stage >= 3 || elapsed == 0 || elapsed % 45 != 0) return;
        Node source = view.nodes.get(view.target);
        player.playSound(source.display.getLocation(), "minecraft:entity.parrot.ambient", .8f, 1.3f);
        player.getWorld().spawnParticle(Particle.NOTE, source.display.getLocation(), 3, .2, .2, .2, 0);
        view.stage++;
        tell(player, "Canopy call " + view.stage + "/3—turn toward the sound.", NamedTextColor.YELLOW);
    }

    private void tickSpore(View view, long tick) {
        if (view.stage >= 4 || tick % 30 != 0) return;
        int[] sequence = {0, 1, 0, 2};
        Node next = view.nodes.get(sequence[view.stage]);
        next.display.getWorld().spawnParticle(Particle.SPORE_BLOSSOM_AIR,
                next.display.getLocation().clone().add(0, .4, 0), 3, .15, .2, .15, 0);
    }

    private void finish(View view, Player player, String message) {
        if (!byEncounter.containsKey(view.encounterId)) return;
        critters.observe(view.critterId);
        critters.capture(view.critterId);
        encounters.get().complete(view.encounterId);
        tell(player, message, NamedTextColor.GREEN);
    }

    private Node addNode(View view, Location location, Material material, int index) {
        location = ensureVisible(location);
        World world = location.getWorld();
        ItemDisplay display = world.spawn(location, ItemDisplay.class, value -> {
            value.setItemStack(ItemStack.of(material));
            value.setBillboard(Display.Billboard.CENTER);
            value.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);
            value.setPersistent(false);
            value.setVisibleByDefault(false);
        });
        Interaction hitbox = world.spawn(location, Interaction.class, value -> {
            value.setInteractionWidth(1.1f);
            value.setInteractionHeight(1.2f);
            value.setResponsive(true);
            value.setPersistent(false);
            value.setVisibleByDefault(false);
        });
        Node node = new Node(view, index, display, hitbox);
        view.nodes.add(node);
        byHitbox.put(hitbox.getUniqueId(), node);
        entities.publishVisual(display, view.activity);
        entities.publishControl(hitbox, view.activity);
        return node;
    }

    private void advanceTarget(View view) {
        view.target = (view.target + 1) % Math.min(3, view.nodes.size());
        if (view.mechanic == Mechanic.GLIMMER_TIMING) showOnly(view, view.target);
    }

    private void showOnly(View view, int index) {
        for (Node node : view.nodes) setActive(node, node.index == index);
    }

    private void setActive(Node node, boolean active) {
        node.display.setGlowing(active);
        node.display.setViewRange(active ? 1.0f : 0.0f);
        node.hitbox.setInteractionWidth(active ? 1.1f : .01f);
    }

    private void remove(UUID encounterId) {
        View view = byEncounter.remove(encounterId);
        if (view == null) return;
        for (Node node : view.nodes) {
            byHitbox.remove(node.hitbox.getUniqueId());
            entities.unregister(node.display);
            entities.unregister(node.hitbox);
            node.display.remove();
            node.hitbox.remove();
        }
        critters.find(view.critterId).ifPresent(value -> critters.escape(view.critterId));
    }

    private static Location center(Player player, double distance) {
        var direction = player.getLocation().getDirection().setY(0);
        if (direction.lengthSquared() < .001) direction.setX(1);
        return surfaceAt(player.getLocation().clone().add(direction.normalize().multiply(distance)));
    }

    private static Location point(Location center, int index, double radius, double y) {
        double angle = Math.PI * 2 * index / 3.0;
        Location surface = surfaceAt(center.clone().add(Math.cos(angle) * radius, 0, Math.sin(angle) * radius));
        return surface.add(0, y, 0);
    }

    /** Finds nearby solid footing with two open blocks above it. */
    private static Location surfaceAt(Location desired) {
        World world = desired.getWorld();
        int x = desired.getBlockX();
        int z = desired.getBlockZ();
        int start = Math.min(world.getMaxHeight() - 3, desired.getBlockY());
        int end = Math.max(world.getMinHeight(), desired.getBlockY() - 10);
        for (int y = start; y >= end; y--) {
            if (world.getBlockAt(x, y, z).getType().isSolid()
                    && world.getBlockAt(x, y + 1, z).isPassable()
                    && world.getBlockAt(x, y + 2, z).isPassable()) {
                return new Location(world, x + .5, y + 1.15, z + .5, desired.getYaw(), desired.getPitch());
            }
        }
        return ensureVisible(desired.clone().add(.5 - desired.getX() + x, .35, .5 - desired.getZ() + z));
    }

    /** Last-resort collision guard for elevated markers such as canopy perches. */
    private static Location ensureVisible(Location desired) {
        Location result = desired.clone();
        for (int attempts = 0; attempts < 8; attempts++) {
            if (result.getBlock().isPassable()
                    && result.clone().add(0, 1, 0).getBlock().isPassable()) return result;
            result.add(0, 1, 0);
        }
        return result;
    }

    private static String instruction(Mechanic mechanic) {
        return switch (mechanic) {
            case GLIMMER_TIMING -> "Track the Glimmerfly's flashes and interact during a landing.";
            case BRAMBLE_BAIT -> "Place the berry bait, back away, and crouch until the Bramblehog feeds.";
            case DEW_INTERCEPT -> "Watch the ripples, predict the Dewhopper's next pad, and intercept its landing.";
            case BURROW_EXITS -> "Read the burrows, seal the two false exits, then catch what surfaces.";
            case CANOPY_ECHO -> "Use three directional calls to identify the Screecher's true perch.";
            case SPORE_SEQUENCE -> "Follow the pulsing red-and-brown mushroom pattern to awaken the Sporeling.";
        };
    }

    private static void tell(Player player, String text, NamedTextColor color) {
        player.sendMessage(Component.text(text, color));
    }

    @Override public void close() {
        task.cancel();
        for (UUID id : new ArrayList<>(byEncounter.keySet())) remove(id);
        HandlerList.unregisterAll(this);
    }

    private enum Mechanic { GLIMMER_TIMING, BRAMBLE_BAIT, DEW_INTERCEPT, BURROW_EXITS, CANOPY_ECHO, SPORE_SEQUENCE }

    private static final class View {
        private final UUID encounterId;
        private final ActivityInstance activity;
        private final UUID critterId;
        private final String definitionId;
        private final Mechanic mechanic;
        private final Location center;
        private final List<Node> nodes = new ArrayList<>();
        private final List<Integer> selected = new ArrayList<>();
        private long startedTick;
        private int stage;
        private int target;
        private int progress;
        private boolean windowOpen;

        private View(UUID encounterId, ActivityInstance activity, UUID critterId, String definitionId,
                Mechanic mechanic, Location center, long startedTick) {
            this.encounterId = encounterId;
            this.activity = activity;
            this.critterId = critterId;
            this.definitionId = definitionId;
            this.mechanic = mechanic;
            this.center = center;
            this.startedTick = startedTick;
        }
    }

    private record Node(View view, int index, ItemDisplay display, Interaction hitbox) { }
}
