package org.tomdang.critter.definition;
import java.util.Map;
public record CritterRewards(long huntingXp,Map<String,Drop> materials){
	public record Drop(int minimum,int maximum,double chance){public Drop{if(minimum<0||maximum<minimum||chance<0||chance>1)throw new IllegalArgumentException("Invalid critter drop");}}
	public CritterRewards{if(huntingXp<0)throw new IllegalArgumentException("Hunting XP cannot be negative");materials=materials==null?Map.of():Map.copyOf(materials);}
}
