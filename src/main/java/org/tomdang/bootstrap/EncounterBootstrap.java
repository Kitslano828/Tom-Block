package org.tomdang.bootstrap;
import org.bukkit.scheduler.BukkitTask;
import org.tomdang.TomBlock;
import org.tomdang.encounter.bukkit.EncounterPlayerListener;
import org.tomdang.encounter.configuration.*;
import org.tomdang.encounter.definition.*;
import org.tomdang.encounter.integration.QuestEncounterIntegration;
import org.tomdang.encounter.runtime.*;
import org.tomdang.gameplay.event.GameplayEventBus;
import javax.sql.DataSource;

public final class EncounterBootstrap implements AutoCloseable {
	private final EncounterRegistry definitions=new EncounterRegistry();
	private final EncounterBehaviorRegistry behaviors=new EncounterBehaviorRegistry();
	private final EncounterRuntimeService runtime; private final BukkitTask tickTask;
	public EncounterBootstrap(TomBlock plugin,DataSource dataSource,GameplayEventBus events,QuestBootStrap quests){
		var loader=new EncounterConfigurationLoader();var resources=new EncounterResourceDiscovery().discover(plugin.getClass());
		for(String resource:resources)try(var input=plugin.getResource(resource)){var values=loader.load(input);if(values.size()!=1)throw new IllegalArgumentException("One encounter per file required");definitions.register(values.getFirst());}catch(java.io.IOException exception){throw new IllegalStateException("Could not load "+resource,exception);}
		behaviors.register("NOOP",new EncounterBehavior(){});
		EncounterRepository repository=dataSource==null?new InMemoryEncounterRepository():new PostgresEncounterRepository(dataSource);
		runtime=new EncounterRuntimeService(definitions,behaviors,repository,events);runtime.recover();
		new QuestEncounterIntegration(runtime,quests.actions(),quests.conditions());
		plugin.getServer().getPluginManager().registerEvents(new EncounterPlayerListener(runtime),plugin);
		tickTask=plugin.getServer().getScheduler().runTaskTimer(plugin,runtime::tick,20L,20L);
		plugin.getLogger().info("Loaded "+definitions.all().size()+" encounter definitions using "+(dataSource==null?"in-memory":"PostgreSQL")+" runtime storage.");
	}
	public EncounterRuntimeService runtime(){return runtime;}
	public EncounterRegistry definitions(){return definitions;}
	public EncounterBehaviorRegistry behaviors(){return behaviors;}
	public void sealFramework(){for(EncounterDefinition definition:definitions.all())behaviors.require(definition.behavior());definitions.seal();behaviors.seal();}
	@Override public void close(){tickTask.cancel();runtime.close();}
}
