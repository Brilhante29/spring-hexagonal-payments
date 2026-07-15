import java.math.RoundingMode
import javax.xml.parsers.DocumentBuilderFactory

plugins {
    id("org.springframework.boot") version "4.1.0"
    kotlin("jvm") version "2.4.10"
    kotlin("plugin.spring") version "2.4.10"
    jacoco
}

group = "com.brilhante29"
version = "1.0.0"

java {
    toolchain { languageVersion = JavaLanguageVersion.of(25) }
}

kotlin {
    jvmToolchain(25)
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}

repositories { mavenCentral() }

dependencies {
    implementation(platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("tools.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.postgresql:postgresql")
    testImplementation(platform("org.testcontainers:testcontainers-bom:2.0.5"))
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-postgresql")
}

tasks.withType<Test> {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

jacoco { toolVersion = "0.8.14" }

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    classDirectories.setFrom(sourceSets.main.get().output.asFileTree.matching {
        exclude(
            "**/PaymentsApplication*",
            "**/adapters/**",
        )
    })
    reports {
        xml.required = true
        html.required = true
    }
}

tasks.register("writeCoverage") {
    dependsOn(tasks.jacocoTestReport)
    val output = layout.buildDirectory.file("coverage-percent.txt")
    outputs.file(output)
    doLast {
        val report = layout.buildDirectory.file("reports/jacoco/test/jacocoTestReport.xml").get().asFile
        val factory = DocumentBuilderFactory.newInstance().apply {
            setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
            setFeature("http://xml.org/sax/features/external-general-entities", false)
            setFeature("http://xml.org/sax/features/external-parameter-entities", false)
        }
        val document = factory.newDocumentBuilder().parse(report)
        val counters = document.documentElement.childNodes
        var missed = 0L
        var covered = 0L
        for (index in 0 until counters.length) {
            val node = counters.item(index)
            if (node.nodeName == "counter" && node.attributes.getNamedItem("type")?.nodeValue == "LINE") {
                missed = node.attributes.getNamedItem("missed").nodeValue.toLong()
                covered = node.attributes.getNamedItem("covered").nodeValue.toLong()
            }
        }
        require(missed + covered > 0) { "JaCoCo line counter not found" }
        val percent = covered.toBigDecimal().multiply(100.toBigDecimal())
            .divide((missed + covered).toBigDecimal(), 2, RoundingMode.HALF_UP)
        require(percent >= 75.toBigDecimal()) { "Core line coverage $percent% is below 75%" }
        output.get().asFile.writeText("$percent\n")
    }
}

tasks.bootJar { archiveFileName = "spring-hexagonal-payments.jar" }
