import dev.monosoul.jooq.RecommendedVersions.JOOQ_VERSION

plugins {
    java
    alias(libs.plugins.springboot)
    alias(libs.plugins.springboot.dependencymanagement)
    idea
    checkstyle
    alias(libs.plugins.jooq)
    alias(libs.plugins.lombok)
    alias(libs.plugins.integrationtest)
}

group = "com.familymoney"
version = "1.0.0"
description = "Backend of the \"Family Money\" application"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

checkstyle {
    toolVersion = "12.3.0"
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.spring.starter.actuator)
    implementation(libs.spring.starter.data.jdbc)
    implementation(libs.spring.starter.mail)
    implementation(libs.spring.starter.security)
    implementation(libs.spring.starter.thymeleaf)
    implementation(libs.spring.starter.validation)
    implementation(libs.spring.starter.web)
    implementation(libs.spring.starter.flyway)
    implementation(libs.spring.starter.jooq)
    implementation(libs.flyway.postgresql)
    implementation(libs.thymeleafextras.springsecurity6)
    runtimeOnly("org.postgresql:postgresql")
    testImplementation(libs.spring.starter.test)
    testImplementation(libs.spring.security.test)
    testImplementation(libs.spring.testcontainers)
    testImplementation(libs.spring.starter.flyway.test)
    testImplementation(libs.spring.starter.jooq.test)
    testImplementation(libs.testcontainers)
    testImplementation(libs.testcontainers.junit)
    testImplementation(libs.testcontainers.postgresql)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation(libs.springdoc.openapi)
    implementation(libs.jjwt.api)
    runtimeOnly(libs.jjwt.impl)
    runtimeOnly(libs.jjwt.jackson)
    testImplementation(libs.spring.resttestclient)
    testImplementation(libs.spring.starter.webmvc.test)
    implementation(libs.mapstruct)
    annotationProcessor(libs.mapstruct.processor)
    implementation(libs.moneta)
    implementation("org.jooq:jooq:${JOOQ_VERSION}")
    testCompileOnly(libs.assertj)
    jooqCodegen("org.postgresql:postgresql")
    integrationImplementation(libs.spring.starter.test)
    integrationImplementation(libs.spring.testcontainers)
    integrationImplementation(libs.spring.starter.flyway.test)
    integrationImplementation(libs.spring.starter.jooq.test)
    integrationImplementation(libs.testcontainers)
    integrationImplementation(libs.testcontainers.junit)
    integrationImplementation(libs.testcontainers.postgresql)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<Test>().configureEach {
    val mockito =
        this.classpath.elements.map { files ->
            files.single { it.asFile.name.startsWith("mockito-core-") }
        }
    this.jvmArgumentProviders.add { listOf("-javaagent:${mockito.get().asFile}") }
}

tasks {
    generateJooqClasses {
        withContainer {
            image {
                name = "postgres:18.1-alpine"
            }
        }
        schemas.set(listOf("public"))
        basePackageName.set("com.familymoney.familymoney.generated")
        migrationLocations.setFromFilesystem(
            project.files("$projectDir/src/main/resources/db/migration"),
        )
    }
}
