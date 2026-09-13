import io.papermc.paperweight.userdev.ReobfArtifactConfiguration
import java.security.MessageDigest

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
    description = "Copies the resource pack to the local server and updates its required-pack settings."
    dependsOn(packageResourcePack)

    doLast {
        val archive = packageResourcePack.get().archiveFile.get().asFile
        val serverPackDirectory = localServerDirectory.resolve("resource-pack")
        serverPackDirectory.mkdirs()
        archive.copyTo(serverPackDirectory.resolve(resourcePackArchiveName), overwrite = true)

        val digest = MessageDigest.getInstance("SHA-1")
        val hash = archive.inputStream().use { input ->
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

        val serverProperties = localServerDirectory.resolve("server.properties")
        val replacements = mapOf(
            "require-resource-pack" to "true",
            "resource-pack" to "http\\://127.0.0.1\\:8123/$resourcePackArchiveName",
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
        serverProperties.writeText(updatedLines.joinToString(System.lineSeparator(), postfix = System.lineSeparator()))

        logger.lifecycle("Deployed required resource pack: ${archive.name}")
        logger.lifecycle("Resource pack SHA-1: $hash")
    }
}

tasks.build {
    finalizedBy(copyPluginToServer, deployResourcePackToServer)
}
