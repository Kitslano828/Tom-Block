package org.tomdang.entityai.navigation;
import org.tomdang.entityai.core.*;
public interface MovableAiAgent extends AiAgent { boolean canOccupy(AiVector position); void move(AiVector position,AiVector direction); }
