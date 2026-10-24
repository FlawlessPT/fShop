plugins { `java-library` }

group = "pt.flawless"
version = "1.0"

repositories { mavenLocal(); mavenCentral(); maven("https://repo.papermc.io/repository/maven-public/") }

dependencies {
    compileOnly("pt.flawless:fapi:1.0")
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("org.xerial:sqlite-jdbc:3.49.1.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    testImplementation("pt.flawless:fapi:1.0")
    testImplementation("com.mysql:mysql-connector-j:8.4.0")
}

tasks.withType<JavaCompile> { options.release = 21; options.encoding = "UTF-8" }

tasks.test {
    useJUnitPlatform()
    environment("FLAWLESS_DB_NAME", "flawless_test")
}

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
