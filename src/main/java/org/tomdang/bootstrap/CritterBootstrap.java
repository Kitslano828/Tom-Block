package org.tomdang.bootstrap;

import javax.sql.DataSource;
import org.tomdang.TomBlock;
import org.tomdang.critter.configuration.CritterConfigurationLoader;
import org.tomdang.critter.configuration.CritterResourceDiscovery;
import org.tomdang.critter.command.CritterTestCommand;
import org.tomdang.critter.definition.CritterRegistry;
import org.tomdang.critter.ecology.CritterEcologyService;
import org.tomdang.critter.journal.CritterJournalService;
import org.tomdang.critter.journal.CritterJournalRepository;
import org.tomdang.critter.journal.InMemoryCritterJournalRepository;
import org.tomdang.critter.journal.PostgresCritterJournalRepository;
import org.tomdang.critter.presentation.GlimmerflyEncounterBehavior;
import org.tomdang.critter.presentation.GroundCritterEncounterBehavior;
import org.tomdang.critter.progression.CritterProgressionService;
import org.tomdang.critter.presentation.critterdex.CritterdexCommand;
import org.tomdang.critter.presentation.critterdex.CritterdexDetailScreen;
import org.tomdang.critter.presentation.critterdex.CritterdexIndexScreen;
import org.tomdang.critter.presentation.critterdex.CritterdexItemListener;
import org.tomdang.critter.runtime.CritterRuntimeService;
import org.tomdang.customitemframework.CustomItemRegistry;
import org.tomdang.customitemframework.CustomItemStackFactory;
import org.tomdang.customitemframework.CustomItemResolver;
import org.tomdang.entityai.runtime.EntityAiRuntime;
import org.tomdang.entityai.diagnostics.AiDiagnosticsCommand;
import org.tomdang.gameplay.event.GameplayEventBus;
import org.tomdang.guiframework.GuiLayoutLoader;
import org.tomdang.guiframework.GuiRegistry;
import org.tomdang.guiframework.GuiService;
import org.tomdang.player.PlayerProfileService;
import org.tomdang.player.playerdata.PlayerProfileRepository;
import org.tomdang.player.skill.SkillProgressPresenter;
import org.tomdang.player.skill.SkillProgressionService;
import org.tomdang.player.skill.SkillProgressNotificationSink;
import org.tomdang.player.playeractionbar.PlayerActionBarService;
import org.tomdang.hud.hunting.HuntingHudService;
import org.tomdang.activity.ActivityAccessService;
import org.tomdang.activity.bukkit.BukkitActivityEntityController;

public final class CritterBootstrap implements AutoCloseable {
    private final CritterRegistry definitions = new CritterRegistry();
    private final CritterRuntimeService runtime;
    private final CritterJournalService journal;
    private final CritterProgressionService progression;
    private final EntityAiRuntime ai;
    private final AiDiagnosticsCommand aiDiagnostics;
    private final GlimmerflyEncounterBehavior glimmerfly;
    private final GroundCritterEncounterBehavior groundCritter;
    private final CritterEcologyService ecology = new CritterEcologyService();
    private final BukkitActivityEntityController activityEntities;

    public CritterBootstrap(TomBlock plugin, DataSource dataSource, GameplayEventBus events,
            EncounterBootstrap encounters, PlayerProfileService profiles,
            PlayerProfileRepository profileRepository, SkillProgressNotificationSink progressionNotifications,
            PlayerActionBarService actionBar, HuntingHudService huntingHud,
            CustomItemRegistry items, CustomItemStackFactory stacks, CustomItemResolver itemResolver,
            GuiRegistry guiRegistry, GuiService guiService) {
        var loader = new CritterConfigurationLoader();
        for (String resource : new CritterResourceDiscovery().discover(plugin.getClass())) {
            try (var input = plugin.getResource(resource)) {
                definitions.register(loader.load(input));
            } catch (java.io.IOException exception) {
                throw new IllegalStateException("Could not load " + resource, exception);
            }
        }
        definitions.seal();
        CritterJournalRepository journalRepository = dataSource == null
                ? new InMemoryCritterJournalRepository() : new PostgresCritterJournalRepository(dataSource);
        journal = new CritterJournalService(journalRepository);
        guiRegistry.register(new CritterdexIndexScreen(definitions, journal,
                loadLayout(plugin, "gui/critterdex-index.yml")));
        guiRegistry.register(new CritterdexDetailScreen(definitions, journal,
                loadLayout(plugin, "gui/critterdex-detail.yml")));
        var critterdexCommand = java.util.Objects.requireNonNull(plugin.getCommand("critterdex"),
                "Missing critterdex command");
        critterdexCommand.setExecutor(new CritterdexCommand(guiService));
        plugin.getServer().getPluginManager().registerEvents(new CritterdexItemListener(itemResolver, guiService), plugin);
        runtime = new CritterRuntimeService(definitions, events);
        progression = new CritterProgressionService(events, definitions, journal, profiles, profileRepository,
                new SkillProgressionService(), new SkillProgressPresenter(progressionNotifications), items, stacks);
        ai = new EntityAiRuntime(plugin);
        ai.start();
        aiDiagnostics = new AiDiagnosticsCommand(plugin, ai);
        var aiCommand = java.util.Objects.requireNonNull(plugin.getCommand("aidiag"), "Missing aidiag command");
        aiCommand.setExecutor(aiDiagnostics);
        aiCommand.setTabCompleter(aiDiagnostics);
        var activityAccess = new ActivityAccessService();
        activityEntities = new BukkitActivityEntityController(plugin, activityAccess);
        glimmerfly = new GlimmerflyEncounterBehavior(plugin, definitions, runtime, ai, encounters::runtime,
                activityAccess, activityEntities);
        encounters.behaviors().register("GLIMMERFLY_HUNT", glimmerfly);
        groundCritter = new GroundCritterEncounterBehavior(plugin, definitions, runtime, ai,
                encounters::runtime, actionBar, huntingHud, activityAccess, activityEntities);
        encounters.behaviors().register("GROUND_CRITTER_HUNT", groundCritter);
        var critterTest = new CritterTestCommand(encounters.runtime());
        var critterCommand = java.util.Objects.requireNonNull(plugin.getCommand("crittertest"),
                "Missing crittertest command");
        critterCommand.setExecutor(critterTest);
        critterCommand.setTabCompleter(critterTest);
        plugin.getLogger().info("Loaded " + definitions.all().size() + " critter definitions.");
    }

    public CritterRegistry definitions() { return definitions; }
    public CritterRuntimeService runtime() { return runtime; }
    public CritterJournalService journal() { return journal; }
    public CritterEcologyService ecology() { return ecology; }
    public EntityAiRuntime ai() { return ai; }

    private static org.tomdang.guiframework.GuiLayout loadLayout(TomBlock plugin, String path) {
        try (java.io.InputStream resource = plugin.getResource(path)) {
            if (resource == null) throw new IllegalStateException("TomBlock.jar does not contain " + path);
            return new GuiLayoutLoader().load(resource);
        } catch (java.io.IOException exception) {
            throw new IllegalStateException("Could not close " + path, exception);
        }
    }

    @Override public void close() {
        groundCritter.close();
        glimmerfly.close();
        activityEntities.close();
        aiDiagnostics.close();
        ai.close();
        progression.close();
    }
}
