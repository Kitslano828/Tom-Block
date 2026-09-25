package org.tomdang.platform.validation;

import java.util.List;

public final class ContentValidationException extends IllegalStateException {
	private final List<ValidationIssue> issues;

	public ContentValidationException(String message, List<ValidationIssue> issues) {
		super(message);
		this.issues = List.copyOf(issues);
	}

	public List<ValidationIssue> issues() { return issues; }
}
