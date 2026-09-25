package org.tomdang.entityai.core;
import java.util.*;
public final class AiMemory{
	private final Map<String,Object> values=new HashMap<>();
	public void put(String key,Object value){if(key==null||key.isBlank()||value==null)throw new IllegalArgumentException("AI memory entry is invalid");values.put(key,value);}
	public <T> Optional<T> get(String key,Class<T> type){Object value=values.get(key);return type.isInstance(value)?Optional.of(type.cast(value)):Optional.empty();}
	public boolean flag(String key){return get(key,Boolean.class).orElse(false);} public void flag(String key,boolean value){put(key,value);}
	public void remove(String key){values.remove(key);}
	public Map<String,Object> snapshot(){return Map.copyOf(values);}
}
