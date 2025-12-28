plugins {
    java
    alias(libs.plugins.springboot)
    alias(libs.plugins.springboot.dependencymanagement)
    idea
    checkstyle
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
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation(libs.flyway.postgresql)
    implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    testCompileOnly(libs.lombok)
    testAnnotationProcessor(libs.lombok)
    runtimeOnly("org.postgresql:postgresql")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.springframework.boot:spring-boot-starter-flyway-test")
    testImplementation(libs.testcontainers.junit)
    testImplementation(libs.testcontainers.postgresql)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation(libs.springdoc.openapi)
    implementation(libs.jjwt.api)
    runtimeOnly(libs.jjwt.impl)
    runtimeOnly(libs.jjwt.jackson)
    testImplementation("org.springframework.boot:spring-boot-resttestclient")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    implementation(libs.mapstruct)
    annotationProcessor(libs.mapstruct.processor)
    implementation(libs.moneta)
    testCompileOnly("org.assertj:assertj-core:3.11.1")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<Test>().configureEach {
    val mockito = this.classpath.elements.map { files ->
        files.single { it.asFile.name.startsWith("mockito-core-") }
    }
    this.jvmArgumentProviders.add { listOf("-javaagent:${mockito.get().asFile}") }
}

