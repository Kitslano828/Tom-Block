package org.tomdang.critter.definition;
import java.time.Duration;
import java.util.Set;
public record CritterMovement(Set<MovementType> modes,double wanderRadius,double preferredHeight,double speed,
		Duration restDuration,double relocationDistance){
	public CritterMovement{modes=modes==null?Set.of():Set.copyOf(modes);if(modes.isEmpty()||wanderRadius<0||preferredHeight<0||speed<=0||restDuration==null||restDuration.isNegative()||relocationDistance<0)throw new IllegalArgumentException("Invalid critter movement");}
}
