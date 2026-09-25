package org.tomdang.entityai.navigation;
import org.tomdang.entityai.core.AiVector;
public record NavigationRequest(AiVector destination,double speed,double arrivalRadius,double maximumHomeDistance,double preferredHeight){
	public NavigationRequest{if(destination==null||!Double.isFinite(speed)||speed<=0||!Double.isFinite(arrivalRadius)||arrivalRadius<0||!Double.isFinite(maximumHomeDistance)||maximumHomeDistance<0||!Double.isFinite(preferredHeight)||preferredHeight<0)throw new IllegalArgumentException("Navigation request is invalid");}
}
