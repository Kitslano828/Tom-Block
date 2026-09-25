package org.tomdang.platform.validation;

import java.util.ArrayList;
import java.util.List;

public final class ValidationReport {
	private final List<ValidationIssue> issues = new ArrayList<>();

	public void warning(String source, String message) {
		issues.add(new ValidationIssue(ValidationIssue.Severity.WARNING, source, message));
	}

	public void error(String source, String message) {
		issues.add(new ValidationIssue(ValidationIssue.Severity.ERROR, source, message));
	}

	public List<ValidationIssue> issues() { return List.copyOf(issues); }
	public boolean hasErrors() { return issues.stream().anyMatch(issue -> issue.severity() == ValidationIssue.Severity.ERROR); }

	public void throwIfInvalid() {
		if (!hasErrors()) return;
		String message = issues.stream()
				.filter(issue -> issue.severity() == ValidationIssue.Severity.ERROR)
				.map(issue -> issue.source() + ": " + issue.message())
				.reduce((left, right) -> left + System.lineSeparator() + right)
				.orElse("Validation failed");
		throw new ContentValidationException(message, issues());
	}
}
