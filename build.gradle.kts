plugins {
    kotlin("js") version "1.3.70-eap-184"
    kotlin("plugin.serialization") version "1.3.61"
}

group = "es.lustr"
version = "0.1.0"

repositories {
    maven("https://dl.bintray.com/kotlin/kotlin-eap")
    maven("https://kotlin.bintray.com/kotlin-js-wrappers/")
    jcenter()
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-js"))
    implementation("org.jetbrains:kotlin-css:1.0.0-pre.91-kotlin-1.3.61")
    implementation("org.jetbrains:kotlin-css-js:1.0.0-pre.91-kotlin-1.3.61")
    implementation("org.jetbrains:kotlin-extensions:1.0.1-pre.91-kotlin-1.3.61")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:1.3.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:0.14.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-js:0.14.0")
    implementation(npm("@mdi/js"))
    implementation(npm("fast-png"))
}

tasks.register<Copy>("copyStatic") {
    from("src/static")
    into("$buildDir/distributions")
    into("$buildDir/processedResources/Js/main")
}

tasks["processResources"].dependsOn(tasks["copyStatic"])

kotlin.sourceSets.main.configure {
    kotlin.srcDir("src")
}

kotlin.target {
    useCommonJs()
    browser {}
}