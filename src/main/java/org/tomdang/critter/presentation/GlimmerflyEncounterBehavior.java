package org.tomdang.critter.presentation;

import java.util.ArrayList;
import java.util.HashMap;
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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.tomdang.critter.definition.CritterRegistry;
import org.tomdang.critter.runtime.CritterRuntimeService;
import org.tomdang.critter.runtime.CritterState;
import org.tomdang.encounter.runtime.EncounterBehavior;
import org.tomdang.encounter.runtime.EncounterRuntimeContext;
import org.tomdang.encounter.runtime.EncounterRuntimeService;
import org.tomdang.entityai.behavior.ConfiguredGoalFactory;
import org.tomdang.entityai.bukkit.BukkitPlayerSensor;
import org.tomdang.entityai.bukkit.DisplayHitboxAiAgent;
import org.tomdang.entityai.configuration.ConfiguredNavigatorFactory;
import org.tomdang.entityai.core.AiBrain;
import org.tomdang.entityai.core.AiCapability;
import org.tomdang.entityai.runtime.EntityAiRuntime;
import org.tomdang.activity.ActivityAccessService;
import org.tomdang.activity.ActivityInstance;
import org.tomdang.activity.bukkit.BukkitActivityEntityController;

/** Private Glimmerfly presentation backed by the shared entity AI runtime. */
public final class GlimmerflyEncounterBehavior implements EncounterBehavior, Listener, AutoCloseable {
    private final CritterRegistry definitions;
    private final CritterRuntimeService critters;
    private final EntityAiRuntime ai;
    private final Supplier<EncounterRuntimeService> encounters;
    private final ActivityAccessService activityAccess;
    private final BukkitActivityEntityController activityEntities;
    private final ConfiguredGoalFactory goals = new ConfiguredGoalFactory();
    private final ConfiguredNavigatorFactory navigators = new ConfiguredNavigatorFactory();
    private final Map<UUID, View> byHitbox = new HashMap<>();
    private final Map<UUID, View> byEncounter = new HashMap<>();

    public GlimmerflyEncounterBehavior(org.bukkit.plugin.Plugin plugin, CritterRegistry definitions,
            CritterRuntimeService critters, EntityAiRuntime ai,
            Supplier<EncounterRuntimeService> encounters, ActivityAccessService activityAccess,
            BukkitActivityEntityController activityEntities) {
        if (plugin == null || definitions == null || critters == null || ai == null || encounters == null
                || activityAccess == null || activityEntities == null) {
            throw new IllegalArgumentException("Glimmerfly behavior dependencies are required");
        }
        this.definitions = definitions;
        this.critters = critters;
        this.ai = ai;
        this.encounters = encounters;
        this.activityAccess = activityAccess;
        this.activityEntities = activityEntities;
        Bukkit.getPluginManager().registerEvents(this, plugin);
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
        String critterId = context.definition().parameters().getOrDefault("critter", "GLIMMERFLY");
        var definition = definitions.require(critterId);
        var direction = player.getLocation().getDirection().setY(0);
        if (direction.lengthSquared() < 0.0001) direction.setX(1);
        direction.normalize();
        Location location = player.getLocation().clone().add(direction.multiply(4))
                .add(0, definition.movement().preferredHeight(), 0);
        World world = player.getWorld();
        ItemDisplay display = world.spawn(location, ItemDisplay.class, value -> {
            ItemStack model = ItemStack.of(Material.PAPER);
            var metadata = model.getItemMeta();
            metadata.setItemModel(new NamespacedKey("tomblock", "glimmerfly"));
            model.setItemMeta(metadata);
            value.setItemStack(model);
            value.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);
            value.setBillboard(Display.Billboard.CENTER);
            value.setGlowing(true);
            value.setPersistent(false);
            value.setVisibleByDefault(false);
        });
        Interaction hitbox = world.spawn(location, Interaction.class, value -> {
            value.setInteractionWidth(0.8f);
            value.setInteractionHeight(0.8f);
            value.setResponsive(true);
            value.setPersistent(false);
        });
        ActivityInstance activity = context.activity();
        activityEntities.publishVisual(display, activity);
        activityEntities.publishControl(hitbox, activity);

        var critter = critters.spawn(critterId, player.getUniqueId());
        var agent = new DisplayHitboxAiAgent(critter.instanceId(), critterId, display, hitbox,
                Set.of(AiCapability.FLYING, AiCapability.HOVERING, AiCapability.INTERACTING));
        var brain = new AiBrain(agent,
                new BukkitPlayerSensor(player.getUniqueId(), Math.max(12, definition.hunting().awarenessRadius() * 2)),
                navigators.create(definition.ai()), goals.create(definition.ai()));
        ai.register(brain);

        View view = new View(context.session().instanceId(), activity, critter.instanceId(), display, hitbox, brain);
        byEncounter.put(view.encounterId, view);
        byHitbox.put(hitbox.getUniqueId(), view);
        context.resources().own(() -> remove(view.encounterId));
        player.sendMessage(Component.text("A Glimmerfly hovers nearby. Approach and interact to observe it.",
                NamedTextColor.GOLD));
    }

    @EventHandler(ignoreCancelled = true)
    public void interact(PlayerInteractEntityEvent event) {
        View view = byHitbox.get(event.getRightClicked().getUniqueId());
        if (view == null) return;
        event.setCancelled(true);
        if (!activityAccess.canInteract(view.activity, event.getPlayer().getUniqueId())) return;
        var instance = critters.find(view.critterId).orElse(null);
        if (instance == null) return;
        if (instance.state() != CritterState.SETTLED) {
            critters.approach(view.critterId, false);
            critters.observe(view.critterId);
            view.brain.memory().flag("settled", true);
            event.getPlayer().sendMessage(Component.text(
                    "You study its rhythm. It settles—interact once more to finish the hunt.",
                    NamedTextColor.YELLOW));
            return;
        }
        critters.capture(view.critterId);
        encounters.get().complete(view.encounterId);
        event.getPlayer().sendMessage(Component.text("Glimmerfly documented and released.", NamedTextColor.GREEN));
    }

    private void remove(UUID encounterId) {
        View view = byEncounter.remove(encounterId);
        if (view == null) return;
        byHitbox.remove(view.hitbox.getUniqueId());
        ai.remove(view.critterId);
        critters.find(view.critterId).ifPresent(value -> critters.escape(view.critterId));
        activityEntities.unregister(view.display);
        activityEntities.unregister(view.hitbox);
        view.display.remove();
        view.hitbox.remove();
    }

    @Override public void close() {
        for (UUID id : new ArrayList<>(byEncounter.keySet())) remove(id);
        HandlerList.unregisterAll(this);
    }

    private record View(UUID encounterId, ActivityInstance activity, UUID critterId, ItemDisplay display,
                        Interaction hitbox, AiBrain brain) {}
}
