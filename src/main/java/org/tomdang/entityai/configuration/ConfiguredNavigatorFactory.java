package org.tomdang.entityai.configuration;

import org.tomdang.entityai.navigation.DirectFlightNavigator;
import org.tomdang.entityai.navigation.NativeGroundNavigator;
import org.tomdang.entityai.navigation.Navigator;

/** Resolves validated profile navigator ids to their runtime implementation. */
public final class ConfiguredNavigatorFactory {
    public Navigator create(AiProfile profile) {
        return switch (profile.navigator()) {
            case "DIRECT_FLIGHT" -> new DirectFlightNavigator();
            case "NATIVE_GROUND" -> new NativeGroundNavigator();
            default -> throw new IllegalArgumentException("Unsupported AI navigator " + profile.navigator());
        };
    }
}
