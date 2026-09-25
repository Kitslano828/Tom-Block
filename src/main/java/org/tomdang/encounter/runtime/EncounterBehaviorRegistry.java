package org.tomdang.encounter.runtime;
import java.util.*;
public final class EncounterBehaviorRegistry {
	private final Map<String,EncounterBehavior> values=new LinkedHashMap<>(); private boolean sealed;
	public void register(String id,EncounterBehavior behavior){if(sealed)throw new IllegalStateException("Encounter behaviors are sealed");String key=key(id);if(values.putIfAbsent(key,behavior)!=null)throw new IllegalArgumentException("Duplicate encounter behavior "+id);}
	public EncounterBehavior require(String id){EncounterBehavior value=values.get(key(id));if(value==null)throw new IllegalArgumentException("Unknown encounter behavior "+id);return value;}
	public void seal(){sealed=true;}
	private String key(String id){if(id==null||id.isBlank())throw new IllegalArgumentException("Behavior id cannot be blank");return id.toUpperCase(Locale.ROOT);}
}
