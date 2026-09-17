package org.tomdang.custommobframework.presentation;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Color;
import org.bukkit.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.entity.Display;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.event.world.EntitiesUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.tomdang.TomBlock;
import org.tomdang.custommobframework.CustomMobRegistry;
import org.tomdang.custommobframework.JellyfishColor;
import org.tomdang.custommobframework.custommobhealth.CustomMobHitFeedback;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Visual-only companion for the test jellyfish mob; the living carrier owns gameplay and health. */
public final class JellyfishPresentationService implements Listener, CustomMobHitFeedback {
	private static final String MOB_ID = "JELLYFISH";
	private static final int HIT_FLASH_TICKS = 5;
	private final TomBlock plugin;
	private final NamespacedKey mobKey;
	private final NamespacedKey currentHealthKey;
	private final NamespacedKey visualVariantKey;
	private final CustomMobRegistry mobs;
	private final NamespacedKey modelKey = NamespacedKey.minecraft("jellyfish");
	private final Map<UUID, Presentation> presentations = new HashMap<>();
	private BukkitTask followTask;

	public JellyfishPresentationService(TomBlock plugin, NamespacedKey mobKey,
	                                    NamespacedKey currentHealthKey, NamespacedKey visualVariantKey,
	                                    CustomMobRegistry mobs) {
		if (plugin == null || mobKey == null || currentHealthKey == null || visualVariantKey == null || mobs == null)
			throw new IllegalArgumentException("plugin, keys and mobs are required");
		this.plugin = plugin;
		this.mobKey = mobKey;
		this.currentHealthKey = currentHealthKey;
		this.visualVariantKey = visualVariantKey;
		this.mobs = mobs;
	}

	public void start() {
		if (followTask != null) return;
		Bukkit.getPluginManager().registerEvents(this, plugin);
		for (var world : Bukkit.getWorlds()) {
			for (LivingEntity entity : world.getLivingEntities()) trackIfJellyfish(entity);
		}
		followTask = Bukkit.getScheduler().runTaskTimer(plugin, this::follow, 1, 5);
	}

	public void stop() {
		if (followTask != null) followTask.cancel();
		followTask = null;
		for (Presentation presentation : presentations.values()) removeDisplays(presentation);
		presentations.clear();
	}

	public void trackIfJellyfish(Entity entity) {
		if (!(entity instanceof LivingEntity carrier) || !carrier.isValid() || carrier.isDead()) return;
		if (!MOB_ID.equals(carrier.getPersistentDataContainer().get(mobKey, PersistentDataType.STRING))) return;
		Presentation existing = presentations.get(carrier.getUniqueId());
		if (existing != null && existing.display().isValid() && existing.nameplate().isValid()) return;
		if (existing != null) removeDisplays(existing);
		carrier.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY,
				Integer.MAX_VALUE, 0, false, false, false));
		carrier.setSilent(true);
		carrier.setCustomNameVisible(false);
		ItemStack visual = ItemStack.of(Material.PAPER);
		ItemMeta meta = visual.getItemMeta();
		meta.setItemModel(modelFor(carrier));
		visual.setItemMeta(meta);
		Location location = visualLocation(carrier);
		ItemDisplay display = carrier.getWorld().spawn(location, ItemDisplay.class, spawned -> {
			spawned.setItemStack(visual);
			spawned.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);
			spawned.setTeleportDuration(5);
			spawned.setPersistent(false);
		});
		TextDisplay nameplate = carrier.getWorld().spawn(nameplateLocation(carrier), TextDisplay.class, spawned -> {
			spawned.setBillboard(Display.Billboard.CENTER);
			spawned.setShadowed(true);
			spawned.setDefaultBackground(false);
			spawned.setSeeThrough(false);
			spawned.setPersistent(false);
			spawned.text(healthText(initialHealth(carrier), maxHealth()));
		});
		presentations.put(carrier.getUniqueId(), new Presentation(carrier, display, nameplate));
	}

	@Override
	public void onAcceptedHit(LivingEntity target, double currentHealth, double maxHealth) {
		if (!isJellyfish(target)) return;
		Presentation presentation = presentations.get(target.getUniqueId());
		if (presentation == null) return;
		presentation.nameplate().text(healthText(currentHealth, maxHealth));
		presentation.display().setGlowColorOverride(Color.RED);
		presentation.display().setGlowing(true);
		presentation.flashUntilTick = Bukkit.getCurrentTick() + HIT_FLASH_TICKS;
		target.getWorld().playSound(target.getLocation(), Sound.BLOCK_BUBBLE_COLUMN_BUBBLE_POP, 0.9f, 0.8f);
	}

	@Override
	public void onRejectedHit(Player attacker, LivingEntity target) {
		if (attacker == null || !isJellyfish(target)) return;
		attacker.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.4f, 0.65f);
	}

	@EventHandler
	public void onEntitiesLoad(EntitiesLoadEvent event) {
		for (Entity entity : event.getEntities()) trackIfJellyfish(entity);
	}

	@EventHandler
	public void onEntitiesUnload(EntitiesUnloadEvent event) {
		for (Entity entity : event.getEntities()) remove(entity.getUniqueId());
	}

	@EventHandler
	public void onDeath(EntityDeathEvent event) {
		remove(event.getEntity().getUniqueId());
	}

	private void follow() {
		var iterator = presentations.entrySet().iterator();
		while (iterator.hasNext()) {
			Presentation presentation = iterator.next().getValue();
			if (!presentation.carrier().isValid() || presentation.carrier().isDead()
					|| !presentation.display().isValid() || !presentation.nameplate().isValid()) {
				removeDisplays(presentation);
				iterator.remove();
				continue;
			}
			presentation.display().teleport(visualLocation(presentation.carrier()));
			presentation.nameplate().teleport(nameplateLocation(presentation.carrier()));
			if (presentation.flashUntilTick <= Bukkit.getCurrentTick()) presentation.display().setGlowing(false);
		}
	}

	private void remove(UUID carrierId) {
		Presentation presentation = presentations.remove(carrierId);
		if (presentation != null) removeDisplays(presentation);
	}

	private void removeDisplays(Presentation presentation) {
		presentation.display().remove();
		presentation.nameplate().remove();
	}

	private boolean isJellyfish(LivingEntity entity) {
		return MOB_ID.equals(entity.getPersistentDataContainer().get(mobKey, PersistentDataType.STRING));
	}

	private NamespacedKey modelFor(LivingEntity carrier) {
		String saved = carrier.getPersistentDataContainer().get(visualVariantKey, PersistentDataType.STRING);
		return JellyfishColor.parse(saved)
				.map(color -> NamespacedKey.minecraft(color.modelId()))
				.orElse(modelKey);
	}

	private double maxHealth() {
		return mobs.getCustomMob(MOB_ID).getMaxHealth();
	}

	private double initialHealth(LivingEntity entity) {
		Double saved = entity.getPersistentDataContainer().get(currentHealthKey, PersistentDataType.DOUBLE);
		return saved == null ? maxHealth() : saved;
	}

	private Component healthText(double current, double max) {
		return Component.text("Jellyfish ", NamedTextColor.AQUA)
				.append(Component.text("❤ " + (int) Math.ceil(current) + "/" + (int) Math.ceil(max), NamedTextColor.RED));
	}

	private Location visualLocation(LivingEntity carrier) {
		return carrier.getLocation().add(0, 0.5, 0);
	}

	private Location nameplateLocation(LivingEntity carrier) {
		return carrier.getLocation().add(0, 1.35, 0);
	}

	private static final class Presentation {
		private final LivingEntity carrier;
		private final ItemDisplay display;
		private final TextDisplay nameplate;
		private int flashUntilTick;

		private Presentation(LivingEntity carrier, ItemDisplay display, TextDisplay nameplate) {
			this.carrier = carrier;
			this.display = display;
			this.nameplate = nameplate;
		}

		private LivingEntity carrier() { return carrier; }
		private ItemDisplay display() { return display; }
		private TextDisplay nameplate() { return nameplate; }
	}
}
