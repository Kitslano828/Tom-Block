package org.tomdang.entityai.navigation;
import org.tomdang.entityai.core.AiAgent;
import java.util.Optional;
public interface Navigator { NavigationStatus navigate(AiAgent agent,NavigationRequest request,long tick); void cancel(AiAgent agent); default Optional<NavigationDiagnostics> diagnostics(AiAgent agent){return Optional.empty();} }
