package org.tomdang.foraging.audit;

public enum TreeAuditClassification {
	CONFIDENT_TREE,
	AMBIGUOUS;

	public static TreeAuditClassification parse(String value) {
		return switch (value.trim().toLowerCase()) {
			case "confident_tree", "confident" -> CONFIDENT_TREE;
			case "ambiguous" -> AMBIGUOUS;
			default -> throw new IllegalArgumentException("Unknown tree-audit classification: " + value);
		};
	}
}
