import io.papermc.paperweight.userdev.ReobfArtifactConfiguration
import java.security.MessageDigest
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.net.InetSocketAddress
import java.net.Socket
import java.util.zip.ZipFile

plugins {
    java
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
}

group = "org.TomDang"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    paperweight.paperDevBundle("26.2.build.87-stable")

    implementation("com.zaxxer:HikariCP:7.0.2")
    implementation("org.flywaydb:flyway-core:13.7.0")
    implementation("org.flywaydb:flyway-database-postgresql:13.7.0")
    runtimeOnly("org.postgresql:postgresql:42.7.8")

    compileOnly("org.projectlombok:lombok:1.18.46")
    annotationProcessor("org.projectlombok:lombok:1.18.46")
    testImplementation(platform("org.junit:junit-bom:6.0.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.mockito:mockito-core:5.23.0")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

paperweight.reobfArtifactConfiguration = ReobfArtifactConfiguration.MOJANG_PRODUCTION

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release = 25
}

tasks.jar {
    archiveFileName = "TomBlock.jar"
    exclude("maps/world.png")
}

val islandEdgeJar by tasks.registering(Jar::class) {
    group = "build"
    description = "Builds the isolated southwest-island boundary and void-generator plugin."
    dependsOn(tasks.classes)
    archiveFileName = "TomBlock-Island-Edge.jar"
    destinationDirectory.set(layout.buildDirectory.dir("island-edge"))
    from(sourceSets.main.get().output) {
        include("org/tomdang/islandedge/**")
    }
    from("src/island-edge/resources")
}

tasks.test {
    useJUnitPlatform()
}

val copyPluginToServer by tasks.registering(Copy::class) {
    group = "build"
    description = "Copies TomBlock.jar into the local Paper server plugins directory."
    from(tasks.jar.flatMap { it.archiveFile })
    into("C:/Users/tomda/Desktop/26.2/plugins")
}

val resourcePackArchiveName = "TomBlock-Resource-Pack.zip"
val localServerDirectory = file("C:/Users/tomda/Desktop/26.2")

val generateVillageMapHudAssets by tasks.registering(Exec::class) {
    group = "build"
    description = "Regenerates the village map HUD textures from the calibrated world PNG."
    val java25 = javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
    commandLine(
        java25.get().executablePath.asFile.absolutePath,
        "scripts/GenerateVillageMapHudAssets.java",
        "src/main/resources/maps/world.png",
        "resource-pack/assets/tomblock/textures/font"
    )
    inputs.file("src/main/resources/maps/world.png")
    inputs.file("scripts/GenerateVillageMapHudAssets.java")
    outputs.files(
        "resource-pack/assets/tomblock/textures/font/village_map.png",
        "resource-pack/assets/tomblock/textures/font/village_markers.png",
        "resource-pack/assets/tomblock/font/village_map.json",
        "resource-pack/assets/tomblock/font/village_markers.json",
        "resource-pack/assets/tomblock/textures/font/village_follow_map.png",
        "resource-pack/assets/tomblock/textures/font/village_follow_markers.png",
        "resource-pack/assets/tomblock/font/village_follow_map.json",
        "resource-pack/assets/tomblock/font/village_follow_markers.json",
        "resource-pack/assets/tomblock/font/village_follow_map_small.json",
        "resource-pack/assets/tomblock/font/village_follow_markers_small.json",
        "resource-pack/assets/tomblock/textures/font/hud_follow_map.png",
        "resource-pack/assets/tomblock/textures/font/hud_follow_markers.png",
        "resource-pack/assets/tomblock/font/hud_follow_map.json",
        "resource-pack/assets/tomblock/font/hud_follow_markers.json",
        "resource-pack/assets/tomblock/textures/font/hud_world_map.png",
        "resource-pack/assets/tomblock/font/hud_world_map.json",
        "resource-pack/assets/tomblock/textures/font/hud_centered_map.png",
        "resource-pack/assets/tomblock/font/hud_centered_map.json",
        "resource-pack/assets/tomblock/textures/font/hud_centered_markers.png",
        "resource-pack/assets/tomblock/font/hud_centered_markers.json"
    )
}

val generateMonocraftHudAssets by tasks.registering(Exec::class) {
    group = "build"
    description = "Generates vanilla-font dialogue positioning and HUD assets."
    val java25 = javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
    commandLine(
        java25.get().executablePath.asFile.absolutePath,
        "scripts/GenerateMonocraftHudAssets.java",
        "resource-pack/assets/tomblock/textures/font/vanilla_ascii.png",
        "resource-pack/assets/tomblock/textures/font"
    )
    inputs.files(
        "scripts/GenerateMonocraftHudAssets.java",
        "resource-pack/assets/tomblock/textures/font/vanilla_ascii.png",
        "resource-pack/assets/tomblock/textures/font/status_health_cell.png",
        "resource-pack/assets/tomblock/textures/font/status_energy_cell.png"
    )
    outputs.files(
        "resource-pack/assets/tomblock/textures/font/dialogue_ascii.png",
        "resource-pack/assets/tomblock/textures/font/dialogue_lines_ascii.png",
        "resource-pack/assets/tomblock/textures/font/dialogue_speaker_ascii.png",
        "resource-pack/assets/tomblock/textures/font/status_health_cell_empty.png",
        "resource-pack/assets/tomblock/textures/font/status_energy_cell_empty.png"
    )
    outputs.dirs(
        "resource-pack/assets/minecraft/textures/gui/sprites/hud/heart"
    )
}

val validateResourcePackFonts by tasks.registering {
    group = "verification"
    description = "Rejects malformed HUD font atlases before a resource pack can be packaged."
    dependsOn(generateMonocraftHudAssets)

    doLast {
        val specifications = mapOf(
            "dialogue_line_1.json" to "dialogue_lines_ascii.png",
            "dialogue_line_2.json" to "dialogue_lines_ascii.png",
            "dialogue_line_3.json" to "dialogue_lines_ascii.png",
            "dialogue_speaker.json" to "dialogue_speaker_ascii.png",
            "dialogue_indicator.json" to "dialogue_speaker_ascii.png",
            "status_text.json" to "vanilla_ascii.png"
        )
        val fontDirectory = file("resource-pack/assets/tomblock/font")
        val textureDirectory = file("resource-pack/assets/tomblock/textures/font")
        val stringPattern = Regex("\\\"(?:\\\\.|[^\\\"\\\\])*\\\"")

        specifications.forEach { (fontName, textureName) ->
            val definition = fontDirectory.resolve(fontName).readText()
            val charsStart = definition.indexOf("\"chars\":[")
            val charsEnd = definition.indexOf("]},{\"type\"", startIndex = charsStart + 9)
            if (charsStart < 0 || charsEnd < 0) throw GradleException("$fontName has no readable chars array")
            val chars = definition.substring(charsStart + 9, charsEnd)
            val rows = stringPattern.findAll(chars).count()
            if (rows != 16) {
                throw GradleException("$fontName defines $rows rows; its atlas requires exactly 16")
            }
            if (!definition.contains("\"id\":\"minecraft:default\"")) {
                throw GradleException("$fontName must use the complete minecraft:default fallback")
            }
            val image = javax.imageio.ImageIO.read(textureDirectory.resolve(textureName))
                ?: throw GradleException("Cannot read $textureName")
            if (image.height % rows != 0 || image.width % 16 != 0) {
                throw GradleException("$textureName dimensions ${image.width}x${image.height} do not match a 16x$rows atlas")
            }
        }
        if (file("resource-pack/assets/minecraft/font/default.json").exists()) {
            throw GradleException("TomBlock must not globally replace Minecraft's default font")
        }
    }
}

val packageResourcePack by tasks.registering(Zip::class) {
    group = "build"
    description = "Packages the TomBlock resource pack for client download."
    dependsOn(generateVillageMapHudAssets, generateMonocraftHudAssets, validateResourcePackFonts)
    from(layout.projectDirectory.dir("resource-pack")) {
        include("pack.mcmeta", "assets/**", "licenses/**")
    }
    archiveFileName.set(resourcePackArchiveName)
    destinationDirectory.set(layout.buildDirectory.dir("resource-pack"))
}

val packageMapHudExperiment by tasks.registering(Zip::class) {
    group = "build"
    description = "Packages an opt-in 26.2 map HUD pack that filters dark GUI rectangles."
    dependsOn(generateVillageMapHudAssets, generateMonocraftHudAssets)
    from(layout.projectDirectory.dir("resource-pack")) {
        include("pack.mcmeta", "assets/**", "licenses/**")
    }
    from(layout.projectDirectory.dir("resource-pack-experiments/transparent-map-sidebar")) {
        include("assets/**")
    }
    archiveFileName.set("TomBlock-MapHud-Experiment.zip")
    destinationDirectory.set(layout.buildDirectory.dir("resource-pack"))
}

val prepareLocalServer by tasks.registering {
    group = "development"
    description = "Builds TomBlock and prepares the Windows Paper server plugin and required resource pack."
    dependsOn(tasks.named("build"), islandEdgeJar, packageResourcePack, packageMapHudExperiment)

    doLast {
        val serverProperties = localServerDirectory.resolve("server.properties")
        val pluginsDirectory = localServerDirectory.resolve("plugins")
        if (!serverProperties.isFile || !pluginsDirectory.isDirectory) {
            throw GradleException("Local Paper server not found at $localServerDirectory")
        }
        val port = serverProperties.readLines().firstOrNull { it.startsWith("server-port=") }
            ?.substringAfter('=')?.toIntOrNull() ?: 25565
        val serverRunning = try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress("127.0.0.1", port), 250)
                true
            }
        } catch (_: java.io.IOException) {
            false
        }
        if (serverRunning) {
            throw GradleException("Stop the local Paper server on port $port before preparing its plugin JAR")
        }

        val jar = tasks.jar.get().archiveFile.get().asFile
        val islandEdge = islandEdgeJar.get().archiveFile.get().asFile
        val worldMap = file("src/main/resources/maps/world.png")
        val useMapHudExperiment = providers.gradleProperty("tomblockMapHudExperiment")
            .orNull?.toBoolean() == true
        val pack = if (useMapHudExperiment) packageMapHudExperiment.get().archiveFile.get().asFile
            else packageResourcePack.get().archiveFile.get().asFile
        val serverPackDirectory = localServerDirectory.resolve("resource-pack")
        val serverMapDirectory = pluginsDirectory.resolve("TomBlock/maps")
        serverPackDirectory.mkdirs()
        serverMapDirectory.mkdirs()
        jar.copyTo(pluginsDirectory.resolve("TomBlock.jar"), overwrite = true)
        islandEdge.copyTo(pluginsDirectory.resolve("TomBlock-Island-Edge.jar"), overwrite = true)
        worldMap.copyTo(serverMapDirectory.resolve("world.png"), overwrite = true)
        pack.copyTo(serverPackDirectory.resolve(resourcePackArchiveName), overwrite = true)

        val digest = MessageDigest.getInstance("SHA-1")
        val hash = pack.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var bytesRead = input.read(buffer)
            while (bytesRead != -1) {
                digest.update(buffer, 0, bytesRead)
                bytesRead = input.read(buffer)
            }
            digest.digest().joinToString("") { byte ->
                (byte.toInt() and 0xff).toString(16).padStart(2, '0')
            }
        }
        val replacements = mapOf(
            "require-resource-pack" to "true",
            "resource-pack" to "http\\://127.0.0.1\\:8123/$resourcePackArchiveName",
            "resource-pack-sha1" to hash
        )
        val updatedKeys = mutableSetOf<String>()
        val updatedLines = serverProperties.readLines().map { line ->
            val key = replacements.keys.firstOrNull { candidate -> line.startsWith("$candidate=") }
            if (key == null) line else {
                updatedKeys.add(key)
                "$key=${replacements.getValue(key)}"
            }
        }.toMutableList()
        replacements.forEach { (key, value) ->
            if (key !in updatedKeys) updatedLines.add("$key=$value")
        }
        serverProperties.copyTo(serverProperties.resolveSibling("server.properties.tomblock-backup"), overwrite = true)
        val temporaryProperties = Files.createTempFile(serverProperties.parentFile.toPath(), "server.properties.", ".tmp")
        try {
            Files.writeString(temporaryProperties,
                updatedLines.joinToString(System.lineSeparator(), postfix = System.lineSeparator()))
            Files.move(temporaryProperties, serverProperties.toPath(), StandardCopyOption.REPLACE_EXISTING)
        } finally {
            Files.deleteIfExists(temporaryProperties)
        }
        logger.lifecycle("Prepared local TomBlock.jar, TomBlock-Island-Edge.jar, and ${if (useMapHudExperiment) "experimental map HUD" else "normal"} resource pack (SHA-1 $hash)")
    }
}

tasks.register("verifyLocalServerPack") {
    group = "development"
    description = "Checks which TomBlock resource pack the local server actually serves."
    doLast {
        val archive = localServerDirectory.resolve("resource-pack/$resourcePackArchiveName")
        val properties = localServerDirectory.resolve("server.properties")
        if (!archive.isFile || !properties.isFile) {
            throw GradleException("Local server pack or server.properties is missing")
        }
        val hash = MessageDigest.getInstance("SHA-1").digest(Files.readAllBytes(archive.toPath()))
            .joinToString("") { byte -> (byte.toInt() and 0xff).toString(16).padStart(2, '0') }
        val configuredHash = properties.readLines().firstOrNull { it.startsWith("resource-pack-sha1=") }
            ?.substringAfter('=')
        val experimental = ZipFile(archive).use { zip ->
            zip.getEntry("assets/minecraft/shaders/core/gui.vsh") != null
        }
        logger.lifecycle("Local server pack: ${if (experimental) "EXPERIMENTAL" else "NORMAL"}")
        logger.lifecycle("File SHA-1: $hash")
        logger.lifecycle("Configured SHA-1: ${configuredHash ?: "missing"}")
        if (!hash.equals(configuredHash, ignoreCase = true)) {
            throw GradleException("server.properties hash does not match the served pack")
        }
    }
}

val deployResourcePackToServer by tasks.registering {
    group = "build"
    description = "Verifies a public resource-pack ZIP and configures a Paper server to require it."

    doLast {
        val packUrl = providers.gradleProperty("tomblockResourcePackUrl").orNull
            ?: throw GradleException("Supply -PtomblockResourcePackUrl=<public HTTPS ZIP URL>")
        val serverPath = providers.gradleProperty("tomblockServerDirectory").orNull
            ?: throw GradleException("Supply -PtomblockServerDirectory=<Paper server directory>")
        val uri = try { URI.create(packUrl) } catch (exception: IllegalArgumentException) {
            throw GradleException("Invalid resource-pack URL", exception)
        }
        if (uri.scheme != "https" || uri.host.isNullOrBlank()) {
            throw GradleException("The resource-pack URL must be a public HTTPS URL")
        }
        val serverProperties = file(serverPath).resolve("server.properties")
        if (!serverProperties.isFile) throw GradleException("No server.properties at $serverProperties")

        val client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(15))
            .build()
        val request = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(60)).GET().build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofInputStream())
        if (response.statusCode() != 200) {
            response.body().close()
            throw GradleException("Resource-pack URL returned HTTP ${response.statusCode()}")
        }
        val digest = MessageDigest.getInstance("SHA-1")
        val hash = response.body().use { input ->
            val signature = input.readNBytes(4)
            if (signature.size != 4 || signature[0] != 'P'.code.toByte() ||
                signature[1] != 'K'.code.toByte() ||
                !((signature[2] == 3.toByte() && signature[3] == 4.toByte()) ||
                  (signature[2] == 5.toByte() && signature[3] == 6.toByte()))) {
                throw GradleException("Resource-pack URL did not return a ZIP file")
            }
            digest.update(signature)
            val buffer = ByteArray(8192)
            var bytesRead = input.read(buffer)
            while (bytesRead != -1) {
                digest.update(buffer, 0, bytesRead)
                bytesRead = input.read(buffer)
            }
            digest.digest().joinToString("") { byte ->
                (byte.toInt() and 0xff).toString(16).padStart(2, '0')
            }
        }

        val replacements = mapOf(
            "require-resource-pack" to "true",
            "resource-pack" to packUrl.replace(":", "\\:"),
            "resource-pack-sha1" to hash
        )
        val updatedKeys = mutableSetOf<String>()
        val updatedLines = serverProperties.readLines().map { line ->
            val key = replacements.keys.firstOrNull { candidate -> line.startsWith("$candidate=") }
            if (key == null) {
                line
            } else {
                updatedKeys.add(key)
                "$key=${replacements.getValue(key)}"
            }
        }.toMutableList()
        replacements.forEach { (key, value) ->
            if (key !in updatedKeys) updatedLines.add("$key=$value")
        }
        val propertiesPath = serverProperties.toPath()
        val replacementPath = Files.createTempFile(propertiesPath.parent, "server.properties.", ".tmp")
        try {
            Files.writeString(replacementPath,
                updatedLines.joinToString(System.lineSeparator(), postfix = System.lineSeparator()))
            try {
                Files.setPosixFilePermissions(replacementPath, Files.getPosixFilePermissions(propertiesPath))
            } catch (_: UnsupportedOperationException) {
                // Windows filesystems do not expose POSIX permissions.
            }
            serverProperties.copyTo(serverProperties.resolveSibling("server.properties.tomblock-backup"), overwrite = true)
            Files.move(replacementPath, propertiesPath, StandardCopyOption.REPLACE_EXISTING)
        } finally {
            Files.deleteIfExists(replacementPath)
        }

        logger.lifecycle("Configured required resource pack: $packUrl")
        logger.lifecycle("Resource pack SHA-1: $hash")
    }
}
