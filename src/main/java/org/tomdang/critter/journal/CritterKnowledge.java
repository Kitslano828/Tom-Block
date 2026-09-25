package org.tomdang.critter.journal;
public enum CritterKnowledge{DISCOVERED,OBSERVED,HUNTED,STUDIED,MASTERED;public CritterKnowledge max(CritterKnowledge other){return ordinal()>=other.ordinal()?this:other;}}
