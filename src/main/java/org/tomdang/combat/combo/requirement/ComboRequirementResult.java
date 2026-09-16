package org.tomdang.combat.combo.requirement;

public record ComboRequirementResult(ComboRequirementStatus status) {
	public ComboRequirementResult {
		if (status == null) throw new IllegalArgumentException("status cannot be null");
	}

	public boolean satisfied() {
		return status == ComboRequirementStatus.SATISFIED;
	}
}
