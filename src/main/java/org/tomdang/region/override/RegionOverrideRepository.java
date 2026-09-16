package org.tomdang.region.override;

import java.io.IOException;
import java.util.Map;

public interface RegionOverrideRepository {
	Map<String, RegionOverrides> load() throws IOException;

	void save(Map<String, RegionOverrides> overrides) throws IOException;
}
