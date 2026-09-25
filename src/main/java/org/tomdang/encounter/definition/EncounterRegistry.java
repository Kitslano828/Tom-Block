package org.tomdang.encounter.definition;
import org.tomdang.platform.identity.ContentKey;
import org.tomdang.platform.registry.SealableRegistry;
import java.util.Collection;
import java.util.Locale;
public final class EncounterRegistry {
	private final SealableRegistry<EncounterDefinition> definitions = new SealableRegistry<>("encounter");
	public void register(EncounterDefinition value) { definitions.register(key(value.id()), value); }
	public EncounterDefinition require(String id) { return definitions.require(key(id)); }
	public Collection<EncounterDefinition> all() { return definitions.all(); }
	public void seal() { definitions.seal(); }
	public boolean isSealed() { return definitions.isSealed(); }
	private ContentKey<EncounterDefinition> key(String id) {
		if (id == null || id.isBlank()) throw new IllegalArgumentException("Encounter id cannot be blank");
		return ContentKey.of("tomblock", id.toLowerCase(Locale.ROOT));
	}
}
