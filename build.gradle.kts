/*
 * This file was generated by the Gradle 'init' task.
 *
 * This is a general purpose Gradle build.
 * Learn more about Gradle by exploring our samples at https://docs.gradle.org/7.5.1/samples
 */

plugins {
    kotlin("jvm") version "1.7.10"
    `java-library`
    java
    `java-gradle-plugin`
}

sourceSets.main {
    java.srcDirs("src", "test")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-common:1.8.0-RC")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
    implementation("com.google.guava:guava:31.1-jre")
    api("org.apache.commons:commons-math3:3.6.1")
    compileOnly("com.google.android:android:4.1.1.4")
}
