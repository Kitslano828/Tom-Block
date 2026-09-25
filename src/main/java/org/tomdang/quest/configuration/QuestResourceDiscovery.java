package org.tomdang.quest.configuration;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.jar.JarFile;

/** Finds all bundled quest YAML files in both development class directories and plugin JARs. */
public final class QuestResourceDiscovery {
	public List<String> discover(Class<?> codeSourceOwner) {
		if (codeSourceOwner == null) throw new IllegalArgumentException("codeSourceOwner cannot be null");
		try {
			URI location = codeSourceOwner.getProtectionDomain().getCodeSource().getLocation().toURI();
			return discover(Path.of(location));
		} catch (URISyntaxException exception) {
			throw new IllegalStateException("Could not discover quest resources", exception);
		}
	}

	public List<String> discover(Path source) {
		if (source == null) throw new IllegalArgumentException("source cannot be null");
		try {
			return Files.isDirectory(source) ? fromDirectory(source) : fromJar(source);
		} catch (IOException exception) {
			throw new IllegalStateException("Could not discover quest resources in " + source, exception);
		}
	}

	private List<String> fromDirectory(Path root) throws IOException {
		Path quests = root.resolve("quests");
		if (!Files.isDirectory(quests)) return List.of();
		try (var paths = Files.walk(quests)) {
			return paths.filter(Files::isRegularFile)
					.map(root::relativize).map(path -> path.toString().replace('\\', '/'))
					.filter(QuestResourceDiscovery::isQuestYaml).sorted().toList();
		}
	}

	private List<String> fromJar(Path jarPath) throws IOException {
		try (JarFile jar = new JarFile(jarPath.toFile())) {
			return jar.stream().filter(entry -> !entry.isDirectory()).map(entry -> entry.getName())
					.filter(QuestResourceDiscovery::isQuestYaml).sorted(Comparator.naturalOrder()).toList();
		}
	}

	private static boolean isQuestYaml(String path) {
		String lower = path.toLowerCase(java.util.Locale.ROOT);
		return lower.startsWith("quests/") && (lower.endsWith(".yml") || lower.endsWith(".yaml"));
	}
}
