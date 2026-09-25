package org.tomdang.bootstrap;

import javax.sql.DataSource;
import org.tomdang.TomBlock;
import org.tomdang.critter.configuration.CritterConfigurationLoader;
import org.tomdang.critter.configuration.CritterResourceDiscovery;
import org.tomdang.critter.definition.CritterRegistry;
import org.tomdang.critter.ecology.CritterEcologyService;
import org.tomdang.critter.journal.CritterJournalService;
import org.tomdang.critter.journal.CritterJournalRepository;
import org.tomdang.critter.journal.InMemoryCritterJournalRepository;
import org.tomdang.critter.journal.PostgresCritterJournalRepository;
import org.tomdang.critter.presentation.GlimmerflyEncounterBehavior;
import org.tomdang.critter.progression.CritterProgressionService;
import org.tomdang.critter.runtime.CritterRuntimeService;
import org.tomdang.customitemframework.CustomItemRegistry;
import org.tomdang.customitemframework.CustomItemStackFactory;
import org.tomdang.entityai.runtime.EntityAiRuntime;
import org.tomdang.entityai.diagnostics.AiDiagnosticsCommand;
import org.tomdang.gameplay.event.GameplayEventBus;
import org.tomdang.player.PlayerProfileService;
import org.tomdang.player.playeractionbar.PlayerActionBarService;
import org.tomdang.player.playerdata.PlayerProfileRepository;
import org.tomdang.player.skill.SkillProgressPresenter;
import org.tomdang.player.skill.SkillProgressionService;

public final class CritterBootstrap implements AutoCloseable {
    private final CritterRegistry definitions = new CritterRegistry();
    private final CritterRuntimeService runtime;
    private final CritterJournalService journal;
    private final CritterProgressionService progression;
    private final EntityAiRuntime ai;
    private final AiDiagnosticsCommand aiDiagnostics;
    private final GlimmerflyEncounterBehavior glimmerfly;
    private final CritterEcologyService ecology = new CritterEcologyService();

    public CritterBootstrap(TomBlock plugin, DataSource dataSource, GameplayEventBus events,
            EncounterBootstrap encounters, PlayerProfileService profiles,
            PlayerProfileRepository profileRepository, PlayerActionBarService actionBar,
            CustomItemRegistry items, CustomItemStackFactory stacks) {
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
        runtime = new CritterRuntimeService(definitions, events);
        progression = new CritterProgressionService(events, definitions, journal, profiles, profileRepository,
                new SkillProgressionService(), new SkillProgressPresenter(actionBar), items, stacks);
        ai = new EntityAiRuntime(plugin);
        ai.start();
        aiDiagnostics = new AiDiagnosticsCommand(plugin, ai);
        var aiCommand = java.util.Objects.requireNonNull(plugin.getCommand("aidiag"), "Missing aidiag command");
        aiCommand.setExecutor(aiDiagnostics);
        aiCommand.setTabCompleter(aiDiagnostics);
        glimmerfly = new GlimmerflyEncounterBehavior(plugin, definitions, runtime, ai, encounters::runtime);
        encounters.behaviors().register("GLIMMERFLY_HUNT", glimmerfly);
        plugin.getLogger().info("Loaded " + definitions.all().size() + " critter definitions.");
    }

    public CritterRegistry definitions() { return definitions; }
    public CritterRuntimeService runtime() { return runtime; }
    public CritterJournalService journal() { return journal; }
    public CritterEcologyService ecology() { return ecology; }
    public EntityAiRuntime ai() { return ai; }

    @Override public void close() {
        glimmerfly.close();
        aiDiagnostics.close();
        ai.close();
        progression.close();
    }
}
