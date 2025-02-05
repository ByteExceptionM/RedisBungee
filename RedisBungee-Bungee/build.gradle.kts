plugins {
    `java-library`
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("xyz.jpenilla.run-waterfall") version "2.0.0"
}


repositories {
    mavenCentral()
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") } // bungeecord
}
val bungeecordApiVersion = "1.19-R0.1-SNAPSHOT"
dependencies {
    api(project(":RedisBungee-API")) {
        exclude("com.google.guava", "guava")
        exclude("com.google.code.gson", "gson")
    }
    compileOnly("net.md-5:bungeecord-api:$bungeecordApiVersion")
}

description = "RedisBungee Bungeecord implementation"

java {
    withJavadocJar()
    withSourcesJar()
}


tasks {
    withType<Javadoc> {
        dependsOn(project(":RedisBungee-API").getTasksByName("javadoc", false))
        val options = options as StandardJavadocDocletOptions
        options.use()
        options.isDocFilesSubDirs = true
        options.links(
            "https://ci.md-5.net/job/BungeeCord/ws/api/target/apidocs/", // bungeecord api
        )
        val apiDocs = File(rootProject.projectDir, "RedisBungee-API/build/docs/javadoc")
        options.linksOffline("https://ci.limework.net/RedisBungee/RedisBungee-API/build/docs/javadoc",  apiDocs.path)
    }
    runWaterfall {
        waterfallVersion("1.19")
    }
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(8)
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name()
    }
    processResources {
        filteringCharset = Charsets.UTF_8.name()
        filesMatching("plugin.yml") {
            filter {
                it.replace("*{redisbungee.version}*", "$version", false)
            }
        }

    }
    shadowJar {
        relocate("redis.clients.jedis", "com.imaginarycode.minecraft.redisbungee.internal.jedis")
        relocate("redis.clients.util", "com.imaginarycode.minecraft.redisbungee.internal.jedisutil")
        relocate("org.apache.commons.pool", "com.imaginarycode.minecraft.redisbungee.internal.commonspool")
        relocate("com.squareup.okhttp", "com.imaginarycode.minecraft.redisbungee.internal.okhttp")
        relocate("okio", "com.imaginarycode.minecraft.redisbungee.internal.okio")
        relocate("org.json", "com.imaginarycode.minecraft.redisbungee.internal.json")

        // configurate shade
        relocate("ninja.leaping.configurate", "com.imaginarycode.minecraft.redisbungee.internal.configurate")
        relocate("org.yaml", "com.imaginarycode.minecraft.redisbungee.internal.yml")
    }

}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}