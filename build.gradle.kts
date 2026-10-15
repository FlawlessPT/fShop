plugins { `java-library` }

group = "pt.flawless"
version = "1.0"

repositories { mavenLocal(); mavenCentral() }

dependencies {
    compileOnly("pt.flawless:fapi:1.0")
    compileOnly("org.spigotmc:spigot:1.8-R0.1-SNAPSHOT")
    compileOnly("org.xerial:sqlite-jdbc:3.49.1.0")
}

tasks.withType<JavaCompile> { options.release = 17; options.encoding = "UTF-8" }

tasks.jar { archiveFileName = "${project.name}.jar" }

val pluginsDirFile = providers.gradleProperty("pluginsDir").map { layout.projectDirectory.dir(it).asFile }

// O Gradle cria a pasta de destino de um Copy antes do doFirst, por isso a verificação
// tem de ser uma tarefa à parte de que o deploy depende.
val checkPluginsDir = tasks.register("checkPluginsDir") {
    doLast {
        val dir = pluginsDirFile.get()
        if (!dir.isDirectory) throw GradleException("pluginsDir não existe: $dir")
    }
}

tasks.register<Copy>("deploy") {
    dependsOn(tasks.jar, checkPluginsDir)
    from(tasks.jar)
    into(pluginsDirFile)
}
