package org.tomdang.critter.runtime;
public enum CritterState{UNAWARE,CURIOUS,ALERT,FLEEING,SETTLED,CAPTURED,ESCAPED;public boolean terminal(){return this==CAPTURED||this==ESCAPED;}}
