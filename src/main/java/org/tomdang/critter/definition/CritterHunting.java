package org.tomdang.critter.definition;
import java.time.Duration;
import java.util.Set;
public record CritterHunting(Set<HuntingArchetype> archetypes,double awarenessRadius,Duration captureWindow,int maximumRelocations,CaptureOutcome outcome){
	public CritterHunting{archetypes=archetypes==null?Set.of():Set.copyOf(archetypes);if(archetypes.isEmpty()||awarenessRadius<=0||captureWindow==null||captureWindow.isNegative()||captureWindow.isZero()||maximumRelocations<0||outcome==null)throw new IllegalArgumentException("Invalid hunting rules");}
}
