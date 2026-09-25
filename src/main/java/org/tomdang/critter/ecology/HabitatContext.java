package org.tomdang.critter.ecology;
import java.util.Set;
public record HabitatContext(Set<String> habitats,double wildFortune){public HabitatContext{habitats=habitats==null?Set.of():Set.copyOf(habitats);if(wildFortune<0)throw new IllegalArgumentException("Wild Fortune cannot be negative");}}
