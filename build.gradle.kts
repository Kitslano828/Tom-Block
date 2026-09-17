import io.papermc.paperweight.userdev.ReobfArtifactConfiguration
import java.security.MessageDigest
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.nio.file.Files
import java.nio.file.StandardCopyOption

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

val packageResourcePack by tasks.registering(Zip::class) {
    group = "build"
    description = "Packages the TomBlock resource pack for client download."
    from(layout.projectDirectory.dir("resource-pack"))
    archiveFileName.set(resourcePackArchiveName)
    destinationDirectory.set(layout.buildDirectory.dir("resource-pack"))
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
