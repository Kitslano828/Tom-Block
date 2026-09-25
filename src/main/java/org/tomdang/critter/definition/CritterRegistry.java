package org.tomdang.critter.definition;
import org.tomdang.platform.identity.ContentKey;
import org.tomdang.platform.registry.SealableRegistry;
import java.util.*;
public final class CritterRegistry{
	private final SealableRegistry<CritterDefinition> values=new SealableRegistry<>("critter");
	public void register(CritterDefinition value){values.register(key(value.id()),value);}public CritterDefinition require(String id){return values.require(key(id));}public Collection<CritterDefinition> all(){return values.all();}public void seal(){for(CritterDefinition value:all())for(String relation:value.relations())require(relation);values.seal();}public boolean isSealed(){return values.isSealed();}
	private ContentKey<CritterDefinition> key(String id){if(id==null||id.isBlank())throw new IllegalArgumentException("Critter id cannot be blank");return ContentKey.of("tomblock",id.toLowerCase(Locale.ROOT));}
}
