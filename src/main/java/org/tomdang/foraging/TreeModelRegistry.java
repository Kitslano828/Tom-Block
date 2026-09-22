package org.tomdang.foraging;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class TreeModelRegistry {
	private final Map<String, TreeModel> models = new LinkedHashMap<>();
	public void register(TreeModel model) {
		if (models.putIfAbsent(model.id(), model) != null) throw new IllegalArgumentException("Duplicate tree model: " + model.id());
	}
	public TreeModel require(String id) {
		TreeModel model = models.get(id);
		if (model == null) throw new IllegalArgumentException("Unknown tree model: " + id);
		return model;
	}
	public Collection<TreeModel> all() { return List.copyOf(models.values()); }
}
