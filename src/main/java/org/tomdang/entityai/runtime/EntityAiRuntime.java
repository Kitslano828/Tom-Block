package org.tomdang.entityai.runtime;
import org.bukkit.Bukkit;import org.bukkit.plugin.Plugin;import org.bukkit.scheduler.BukkitTask;import org.tomdang.entityai.core.AiBrain;import java.util.*;
/** One budgeted server-thread scheduler for every TomBlock-controlled AI brain. */
public final class EntityAiRuntime implements AutoCloseable{
	private static final int DEFAULT_BUDGET=256;private final Plugin plugin;private final int budget;private final Map<UUID,AiBrain> brains=new LinkedHashMap<>();private BukkitTask task;private int cursor;
	public EntityAiRuntime(Plugin plugin){this(plugin,DEFAULT_BUDGET);}public EntityAiRuntime(Plugin plugin,int budget){if(plugin==null||budget<=0)throw new IllegalArgumentException("AI runtime configuration is invalid");this.plugin=plugin;this.budget=budget;}
	public void start(){if(task==null)task=Bukkit.getScheduler().runTaskTimer(plugin,this::tick,1L,1L);}public void register(AiBrain brain){if(brain==null)throw new IllegalArgumentException("AI brain is required");AiBrain old=brains.putIfAbsent(brain.agent().id(),brain);if(old!=null)throw new IllegalArgumentException("AI agent is already registered: "+brain.agent().id());}public boolean remove(UUID id){AiBrain brain=brains.remove(id);if(brain!=null)brain.stop();return brain!=null;}public Optional<AiBrain> find(UUID id){return Optional.ofNullable(brains.get(id));}public int size(){return brains.size();}
	public Collection<AiBrain> all(){return List.copyOf(brains.values());}public int budget(){return budget;}
	void tick(){if(brains.isEmpty())return;List<AiBrain> snapshot=List.copyOf(brains.values());int count=Math.min(budget,snapshot.size());for(int i=0;i<count;i++){AiBrain brain=snapshot.get((cursor+i)%snapshot.size());if(!brain.agent().valid())remove(brain.agent().id());else try{brain.tick(Bukkit.getCurrentTick());}catch(RuntimeException exception){plugin.getLogger().warning("AI agent "+brain.agent().id()+" failed: "+exception.getMessage());remove(brain.agent().id());}}cursor=snapshot.isEmpty()?0:(cursor+count)%snapshot.size();}
	@Override public void close(){if(task!=null)task.cancel();task=null;for(AiBrain brain:List.copyOf(brains.values()))brain.stop();brains.clear();}
}
