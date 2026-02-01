plugins {
    id("application")
    id("java")
    kotlin("jvm") version "2.0.20"
}

application {
    mainClass.set("tools.important.tankslua.TanksLuaKt")
}

repositories {
    mavenCentral()
}

dependencies {
    // This is where I store my tanks jar in particular.
    // If the project won't build because your computer's files
    // are not structured exactly the same as mine,
    // then you should change it to wherever you put your tanks jar.
    implementation(files("/home/f6/Programming/Java/TanksJars/Tanks1.6.0.jar"))

    // https://mvnrepository.com/artifact/party.iroiro.luajava/luajit
    implementation("party.iroiro.luajava:luajit:4.0.2")
    runtimeOnly("party.iroiro.luajava:luajit-platform:4.0.2:natives-desktop")

    implementation(kotlin("stdlib-jdk8"))
}

kotlin {
    jvmToolchain(8)
}

sourceSets {
    main {
        kotlin {
            srcDirs("src")
        }
        resources {
            srcDirs("resources")
        }
    }
}