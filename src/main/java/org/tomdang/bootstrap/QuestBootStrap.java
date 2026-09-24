package org.tomdang.bootstrap;

import org.tomdang.TomBlock;
import org.tomdang.quest.configuration.QuestConfigurationLoader;
import org.tomdang.quest.definition.QuestRegistry;
import org.tomdang.quest.progress.InMemoryQuestProgressRepository;
import org.tomdang.quest.progress.PostgresQuestProgressRepository;
import org.tomdang.quest.progress.QuestProgressRepository;
import org.tomdang.quest.progress.QuestProgressService;

import javax.sql.DataSource;

public final class QuestBootStrap {
	private final QuestRegistry registry = new QuestRegistry();
	private final QuestProgressService progressService;

	public QuestBootStrap(TomBlock plugin, DataSource dataSource) {
		if (plugin == null) throw new IllegalArgumentException("Plugin cannot be null");
		var definitions = new QuestConfigurationLoader().load(plugin.getResource("quests/quests.yml"));
		definitions.forEach(registry::register);
		registry.validatePrerequisites();
		QuestProgressRepository repository = dataSource == null
				? new InMemoryQuestProgressRepository()
				: new PostgresQuestProgressRepository(dataSource);
		progressService = new QuestProgressService(registry, repository);
		plugin.getLogger().info("Loaded " + definitions.size() + " quest definitions using "
				+ (dataSource == null ? "in-memory" : "PostgreSQL") + " progress storage.");
	}

	public QuestRegistry registry() { return registry; }
	public QuestProgressService progressService() { return progressService; }
}
