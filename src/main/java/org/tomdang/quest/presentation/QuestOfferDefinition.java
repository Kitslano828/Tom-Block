package org.tomdang.quest.presentation;
public record QuestOfferDefinition(String actorId,String questId){public QuestOfferDefinition{if(actorId==null||actorId.isBlank()||questId==null||questId.isBlank())throw new IllegalArgumentException("Quest offer actor and quest are required");}}
