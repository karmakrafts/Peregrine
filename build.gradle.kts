/*
 * Copyright 2024 Karma Krafts & associates
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import com.github.jengelman.gradle.plugins.shadow.internal.DependencyFilter
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.jvm.tasks.Jar
import org.jetbrains.dokka.gradle.DokkaTask
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import kotlin.io.path.div
import kotlin.io.path.inputStream
import kotlin.io.path.pathString

plugins {
    eclipse
    idea
    `maven-publish`
    alias(libs.plugins.forgeGradle)
    alias(libs.plugins.mixinGradle)
    alias(libs.plugins.librarian)
    alias(libs.plugins.shadow)
    alias(libs.plugins.dokka)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

val projectPath: Path = project.projectDir.toPath()
val buildConfig: Properties = Properties().apply {
    (projectPath / "build.properties").inputStream(StandardOpenOption.READ).use(::load)
}
val modId: String = buildConfig["mod_id"] as String
val license: String = buildConfig["license"] as String
val mcVersion: String = libs.versions.minecraft.get()
val buildNumber: Int = System.getenv("CI_PIPELINE_IID")?.toIntOrNull() ?: 0
val buildTime: Instant = Instant.now()
val platforms: List<String> = (buildConfig["platforms"] as String).split(',')

version = "${libs.versions.peregrine.get()}.$buildNumber"
group = buildConfig["group"] as String
base.archivesName = "$modId-$mcVersion"

// Source sets
sourceSets.main {
    java.srcDirs(projectPath / "src" / "main" / "java", projectPath / "src" / "main" / "kotlin")
    resources.srcDirs(projectPath / "src" / "generated" / "resources", projectPath / "src" / "main" / "resources")
}
val mainSourceSet by sourceSets.main

fun SourceSetContainer.createDefault(name: String): SourceSet = create(name) {
    java.srcDirs(projectPath / "src" / name, projectPath / "src" / name)
    resources.srcDirs(projectPath / "src" / name / "resources")
}

val apiSourceSet = sourceSets.createDefault("api")

// Configs
val coreLibraryConfig = configurations.create("coreLibrary")
val libraryConfig = configurations.create("library") {
    extendsFrom(coreLibraryConfig)
}

val minecraftConfig = configurations.getByName("minecraft")

configurations {
    annotationProcessor {
        extendsFrom(minecraftConfig)
    }
    val implementation by getting {
        extendsFrom(coreLibraryConfig, libraryConfig)
    }
    val compileClasspath by getting {
        extendsFrom(coreLibraryConfig, libraryConfig)
    }
    val apiCompileOnly by getting {
        extendsFrom(coreLibraryConfig, minecraftConfig)
    }
}

repositories {
    mavenCentral()
    google()
    maven("https://maven.covers1624.net")
    maven("https://thedarkcolour.github.io/KotlinForForge")
    maven("https://maven.blamejared.com")
    maven("https://cursemaven.com")
}

fun DependencyHandlerScope.localLwjglModule(name: String) {
    libraryConfig(files(projectPath / "libs" / "lwjgl-$name.jar"))
    compileOnly(files(projectPath / "libs" / "lwjgl-$name-javadoc.jar"))
    compileOnly(files(projectPath / "libs" / "lwjgl-$name-sources.jar"))
    platforms.forEach { platform ->
        libraryConfig(files(projectPath / "libs" / "lwjgl-$name-natives-$platform.jar"))
    }
}

dependencies {
    minecraft(libs.minecraftForge)

    //implementation(fg.deobf(libs.embeddium.get().toString()))
    //implementation(fg.deobf(libs.oculus.get().toString()))

    coreLibraryConfig(libs.annotations)
    coreLibraryConfig(libs.jackson.core)
    coreLibraryConfig(libs.jackson.annotationns)
    coreLibraryConfig(libs.jackson.databind)

    localLwjglModule("freetype")
    localLwjglModule("msdfgen")

    implementation(apiSourceSet.output)

    testImplementation(libs.junit.api)
    testRuntimeOnly(libs.junit.engine)
    testImplementation(apiSourceSet.output)
}

minecraft {
    mappings(buildConfig["mappings_channel"] as String, "${libs.versions.mappings.get()}-$mcVersion")
    accessTransformer(projectPath / "src" / "main" / "resources" / "META-INF" / "accesstransformer.cfg")
    copyIdeResources = true
    runs {
        val client by creating {
            property("forge.enabledGameTestNamespaces", modId)
        }
        val server by creating {
            property("forge.enabledGameTestNamespaces", modId)
            args("--nogui")
        }
        val gameTestServer by creating {
            property("forge.enabledGameTestNamespaces", modId)
        }
        val data by creating
        val clientAlt by creating {
            parent(client)
            args("--username", "Dev2")
        }
        configureEach {
            workingDirectory(project.file("run"))
            properties(mapOf("forge.logging.markers" to "LOADING,CORE",
                "forge.logging.console.level" to "debug",
                "mixin.debug" to "true",
                "mixin.debug.dumpTargetOnFailure" to "true",
                "mixin.debug.verbose" to "true",
                "mixin.env.remapRefFile" to "true",
                "mixin.env.refMapRemappingFile" to (projectPath / "build" / "createSrgToMcp" / "output.srg").pathString))
            jvmArgs("-Xms512M", "-Xmx4096M")
            mods {
                create(modId) {
                    sources(mainSourceSet, apiSourceSet)
                }
            }
            lazyToken("minecraft_classpath") {
                libraryConfig.copyRecursive().resolve().joinToString(File.pathSeparator) { it.absolutePath }
            }
        }
    }
}

mixin {
    add(mainSourceSet, "mixins.$modId.refmap.json")
    config("mixins.$modId.client.json")
}

fun Manifest.applyCommonManifest() {
    attributes.apply {
        this["MixinConfigs"] = "mixins.$modId.client.json"
        this["Specification-Title"] = modId
        this["Specification-Vendor"] = "Karma Krafts"
        this["Specification-Version"] = version
        this["Implementation-Title"] = modId
        this["Implementation-Vendor"] = "Karma Krafts"
        this["Implementation-Version"] = version
        this["Implementation-Timestamp"] = SimpleDateFormat.getDateTimeInstance().format(Date.from(buildTime))
    }
}

fun Provider<MinimalExternalModuleDependency>.toShadowInclude(): String {
    return get().module.let { "${it.group}:${it.name}" }
}

tasks {
    processResources {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        val forgeVersion = libs.versions.forge.get()
        val properties = mapOf("minecraft_version" to mcVersion,
            "minecraft_version_range" to "[$mcVersion]",
            "forge_version" to forgeVersion,
            "forge_version_range" to "[$forgeVersion,)",
            "loader_version_range" to forgeVersion.substringBefore("."),
            "mod_id" to modId,
            "mod_name" to buildConfig["mod_name"] as String,
            "mod_license" to license,
            "mod_version" to version,
            "mod_authors" to "Karma Krafts",
            "mod_description" to "A Blaze3D compatible rendering library for Minecraft Forge.")
        inputs.properties(properties)
        filesMatching(listOf("META-INF/mods.toml", "pack.mcmeta")) {
            expand(properties)
        }
    }
    dokkaHtml {
        dokkaSourceSets {
            create("main") {
                sourceRoots.setFrom("src/api/java")
                reportUndocumented = false
                jdkVersion = java.toolchain.languageVersion.get().asInt()
                noAndroidSdkLink = true
                externalDocumentationLink("https://docs.karmakrafts.dev/peregrine")
            }
        }
    }
}

val dokkaHtmlTask = tasks.getByName<DokkaTask>("dokkaHtml")
val classesTask = tasks.getByName("classes")

val jarTask = tasks.getByName<Jar>("jar") {
    from(mainSourceSet.output, apiSourceSet.output)
    archiveClassifier = "slim"
    manifest.applyCommonManifest()
    finalizedBy("reobfJar") // Lazy forward dependency
}

fun DependencyFilter.includeCoreLibs() {
    include(dependency(libs.annotations.toShadowInclude()))
    include(dependency(libs.jackson.core.toShadowInclude()))
    include(dependency(libs.jackson.annotationns.toShadowInclude()))
    include(dependency(libs.jackson.databind.toShadowInclude()))
}

fun DependencyFilter.includeLocalLwjglModule(name: String) {
    include(dependency(files(projectPath / "libs" / "lwjgl-$name.jar")))
    platforms.forEach { platform ->
        include(dependency(files(projectPath / "libs" / "lwjgl-$name-natives-$platform.jar")))
    }
}

val shadowJarTask = tasks.getByName<ShadowJar>("shadowJar") {
    from(mainSourceSet.output, apiSourceSet.output)
    archiveClassifier = ""
    manifest.applyCommonManifest()
    mergeServiceFiles()
    finalizedBy("reobfShadowJar") // Lazy forward dependency
    dependencies {
        includeCoreLibs()
        includeLocalLwjglModule("freetype")
        includeLocalLwjglModule("msdfgen")
        includeLocalLwjglModule("yoga")
    }
}
val reobfShadowJarTask = reobf.create("shadowJar")

val sourcesJarTask = tasks.create<Jar>("sourcesJar") {
    from(mainSourceSet.allSource, apiSourceSet.allSource)
    dependsOn(classesTask)
    archiveClassifier = "sources"
}

val apiJarTask = tasks.create<ShadowJar>("apiJar") {
    from(apiSourceSet.output)
    archiveClassifier = "api"
    finalizedBy("reobfApiJar") // Lazy forward dependency
    mergeServiceFiles()
    dependencies {
        includeCoreLibs()
    }
}
val reobfApiJarTask = reobf.create("apiJar")

val apiSourcesJarTask = tasks.create<Jar>("apiSourcesJar") {
    from(apiSourceSet.allSource)
    archiveClassifier = "api-sources"
}

val apiJavadocJarTask = tasks.create<Jar>("apiJavadocJar") {
    from(dokkaHtmlTask.outputs)
    dependsOn(dokkaHtmlTask)
    mustRunAfter(dokkaHtmlTask)
    archiveClassifier = "api-javadoc"
}

artifacts {
    archives(jarTask)
    archives(shadowJarTask)
    archives(sourcesJarTask)
    archives(apiJarTask)
    archives(apiSourcesJarTask)
    archives(apiJavadocJarTask)
}

tasks {
    withType<Jar> {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }

    val archiveName = project.base.archivesName.get()

    System.getProperty("publishDocs.root")?.let { docsDir ->
        create<Copy>("publishDocs") {
            dependsOn(apiJavadocJarTask)
            mustRunAfter(apiJavadocJarTask)
            from(zipTree(projectPath / "build" / "libs" / "$archiveName-$version-api-javadoc.jar"))
            into(docsDir)
        }
    }

    System.getenv("CI_API_V4_URL")?.let { apiUrl ->

        publishing {
            repositories {
                maven {
                    url = uri("${apiUrl.replace("http://", "https://")}/projects/${System.getenv("CI_PROJECT_ID")}/packages/maven")
                    name = "GitLab"
                    credentials(HttpHeaderCredentials::class) {
                        name = "Job-Token"
                        value = System.getenv("CI_JOB_TOKEN")
                    }
                    authentication {
                        create("header", HttpHeaderAuthentication::class)
                    }
                }
            }

            publications {
                create<MavenPublication>(modId) {
                    groupId = project.group as String
                    artifactId = archiveName
                    version = project.version as String

                    artifact(jarTask)
                    artifact(shadowJarTask)
                    artifact(sourcesJarTask)
                    artifact(apiJarTask)
                    artifact(apiSourcesJarTask)
                    artifact(apiJavadocJarTask)

                    pom {
                        name = artifactId
                        url = "https://git.karmakrafts.dev/kk/mc-projects/$modId"
                        scm {
                            url = this@pom.url
                        }
                        issueManagement {
                            system = "gitlab"
                            url = "https://git.karmakrafts.dev/kk/mc-projects/$modId/issues"
                        }
                        licenses {
                            license {
                                name = license
                                distribution = "repo"
                            }
                        }
                        developers {
                            developer {
                                id = "kitsunealex"
                                name = "KitsuneAlex"
                                url = "https://git.karmakrafts.dev/KitsuneAlex"
                            }
                        }
                    }
                }
            }
        }
    }
}