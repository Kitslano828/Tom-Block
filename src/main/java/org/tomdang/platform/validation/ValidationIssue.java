package org.tomdang.platform.validation;

public record ValidationIssue(Severity severity, String source, String message) {
	public ValidationIssue {
		if (severity == null) throw new IllegalArgumentException("severity cannot be null");
		if (source == null || source.isBlank()) throw new IllegalArgumentException("source cannot be blank");
		if (message == null || message.isBlank()) throw new IllegalArgumentException("message cannot be blank");
	}

	public enum Severity { WARNING, ERROR }
}
