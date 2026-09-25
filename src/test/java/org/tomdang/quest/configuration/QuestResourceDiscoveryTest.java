package org.tomdang.quest.configuration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QuestResourceDiscoveryTest {
	@TempDir Path temporaryDirectory;

	@Test void recursivelyDiscoversSortedYamlFromDirectory() throws IOException {
		Files.createDirectories(temporaryDirectory.resolve("quests/island/side"));
		Files.writeString(temporaryDirectory.resolve("quests/z.yml"), "quest: {}");
		Files.writeString(temporaryDirectory.resolve("quests/island/side/a.yaml"), "quest: {}");
		Files.writeString(temporaryDirectory.resolve("quests/readme.txt"), "ignored");

		assertEquals(List.of("quests/island/side/a.yaml", "quests/z.yml"),
				new QuestResourceDiscovery().discover(temporaryDirectory));
	}

	@Test void recursivelyDiscoversSortedYamlFromPackagedJar() throws IOException {
		Path jar = temporaryDirectory.resolve("plugin.jar");
		try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(jar))) {
			entry(output, "quests/z.yml");
			entry(output, "quests/island/main/a.yml");
			entry(output, "plugin.yml");
		}
		assertEquals(List.of("quests/island/main/a.yml", "quests/z.yml"),
				new QuestResourceDiscovery().discover(jar));
	}

	private void entry(JarOutputStream output, String name) throws IOException {
		output.putNextEntry(new JarEntry(name));
		output.write("test".getBytes(java.nio.charset.StandardCharsets.UTF_8));
		output.closeEntry();
	}
}
