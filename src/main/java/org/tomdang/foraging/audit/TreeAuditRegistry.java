package org.tomdang.foraging.audit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class TreeAuditRegistry {
	private final List<TreeAuditComponent> components;

	private TreeAuditRegistry(List<TreeAuditComponent> components) {
		this.components = List.copyOf(components);
	}

	public static TreeAuditRegistry load(InputStream input) throws IOException {
		if (input == null) throw new IOException("Missing southwest tree audit resource");
		List<TreeAuditComponent> components = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
			String line = reader.readLine();
			if (line == null || !line.startsWith("id,classification,")) throw new IOException("Invalid tree audit header");
			int lineNumber = 1;
			while ((line = reader.readLine()) != null) {
				lineNumber++;
				if (line.isBlank()) continue;
				String[] values = line.split(",", -1);
				if (values.length != 8) throw new IOException("Invalid tree audit row at line " + lineNumber);
				try {
					components.add(new TreeAuditComponent(Integer.parseInt(values[0]),
							TreeAuditClassification.parse(values[1]), Integer.parseInt(values[2]),
							Integer.parseInt(values[3]), Integer.parseInt(values[4]), Integer.parseInt(values[5]),
							Integer.parseInt(values[6]), Integer.parseInt(values[7])));
				} catch (IllegalArgumentException exception) {
					throw new IOException("Invalid tree audit row at line " + lineNumber, exception);
				}
			}
		}
		return new TreeAuditRegistry(components);
	}

	public List<TreeAuditComponent> components() {
		return components;
	}
}
