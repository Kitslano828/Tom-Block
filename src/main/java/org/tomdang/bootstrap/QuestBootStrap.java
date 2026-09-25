package org.tomdang.bootstrap;

import org.tomdang.TomBlock;
import org.tomdang.quest.configuration.QuestConfigurationLoader;
import org.tomdang.quest.configuration.QuestResourceDiscovery;
import org.tomdang.quest.definition.QuestRegistry;
import org.tomdang.quest.progress.InMemoryQuestProgressRepository;
import org.tomdang.quest.progress.PostgresQuestProgressRepository;
import org.tomdang.quest.progress.QuestProgressRepository;
import org.tomdang.quest.progress.QuestProgressService;
import org.tomdang.gameplay.event.GameplayEventBus;
import org.tomdang.quest.integration.QuestGameplayEventAdapter;
import org.tomdang.quest.gate.QuestGateConfigurationLoader;
import org.tomdang.quest.gate.QuestGateRegistry;
import org.tomdang.quest.orchestration.*;

import javax.sql.DataSource;

public final class QuestBootStrap implements AutoCloseable {
	private final QuestRegistry registry = new QuestRegistry();
	private final QuestProgressService progressService;
	private final QuestGameplayEventAdapter gameplayEvents;
	private final QuestGateRegistry gates;
	private final QuestHandlerRegistry<QuestAction> actions = new QuestHandlerRegistry<>("quest action");
	private final QuestHandlerRegistry<QuestCondition> conditions = new QuestHandlerRegistry<>("quest condition");
	private final QuestHandlerRegistry<QuestReward> rewards = new QuestHandlerRegistry<>("quest reward");
	private final QuestLifecycleBus lifecycle = new QuestLifecycleBus();
	private final QuestOrchestrationService orchestration;

	public QuestBootStrap(TomBlock plugin, DataSource dataSource, GameplayEventBus events) {
		if (plugin == null) throw new IllegalArgumentException("Plugin cannot be null");
		if (events == null) throw new IllegalArgumentException("events cannot be null");
		var loader = new QuestConfigurationLoader();
		var resources = new QuestResourceDiscovery().discover(plugin.getClass());
		if (resources.isEmpty()) throw new IllegalStateException("No quest definitions found under quests/");
		int definitionCount = 0;
		for (String resource : resources) {
			try (var input = plugin.getResource(resource)) {
				if (input == null) throw new IllegalStateException("Missing discovered quest resource " + resource);
				var definitions = loader.load(input);
				if (definitions.size() != 1) throw new IllegalArgumentException(
						"Quest resource must contain exactly one quest: " + resource);
				registry.register(definitions.getFirst());
				definitionCount++;
			} catch (java.io.IOException exception) {
				throw new IllegalStateException("Could not close quest resource " + resource, exception);
			} catch (RuntimeException exception) {
				throw new IllegalStateException("Invalid quest resource " + resource + ": " + exception.getMessage(), exception);
			}
		}
		registry.validateAndSeal();
		try (var input = plugin.getResource("quest-gates.yml")) {
			gates = new QuestGateConfigurationLoader().load(input);
		} catch (java.io.IOException exception) {
			throw new IllegalStateException("Could not close quest-gates.yml", exception);
		}
		for (var gate : gates.all()) registry.require(gate.questId());
		QuestProgressRepository repository = dataSource == null
				? new InMemoryQuestProgressRepository()
				: new PostgresQuestProgressRepository(dataSource);
		QuestRewardLedger rewardLedger = dataSource == null ? new InMemoryQuestRewardLedger()
				: new PostgresQuestRewardLedger(dataSource);
		orchestration = new QuestOrchestrationService(actions, conditions, rewards, rewardLedger, lifecycle);
		progressService = new QuestProgressService(registry, repository, orchestration);
		gameplayEvents = new QuestGameplayEventAdapter(events, progressService);
		plugin.getLogger().info("Loaded " + definitionCount + " quest definitions from " + resources.size()
				+ " files using "
				+ (dataSource == null ? "in-memory" : "PostgreSQL") + " progress storage.");
	}

	public QuestRegistry registry() { return registry; }
	public QuestProgressService progressService() { return progressService; }
	public QuestGateRegistry gates() { return gates; }
	public QuestHandlerRegistry<QuestAction> actions() { return actions; }
	public QuestHandlerRegistry<QuestCondition> conditions() { return conditions; }
	public QuestHandlerRegistry<QuestReward> rewards() { return rewards; }
	public QuestLifecycleBus lifecycle() { return lifecycle; }
	public void sealOrchestration() { orchestration.validate(registry); }
	@Override public void close() { gameplayEvents.close(); }
}
