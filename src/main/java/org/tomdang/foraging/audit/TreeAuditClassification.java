package org.tomdang.foraging.audit;

public enum TreeAuditClassification {
	CONFIDENT_TREE,
	AMBIGUOUS,
	LANDMARK_TREE;

	public static TreeAuditClassification parse(String value) {
		return switch (value.trim().toLowerCase()) {
			case "confident_tree", "confident" -> CONFIDENT_TREE;
			case "ambiguous" -> AMBIGUOUS;
			case "landmark_tree", "landmark" -> LANDMARK_TREE;
			default -> throw new IllegalArgumentException("Unknown tree-audit classification: " + value);
		};
	}
}
