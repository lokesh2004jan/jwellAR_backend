plugins {
	java
	id("org.springframework.boot") version "3.5.6"
	id("io.spring.dependency-management") version "1.1.7"
	kotlin("jvm")
}

group = "com.JwellARDB"
version = "0.0.1-SNAPSHOT"
description = "Demo project for Spring Boot"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
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
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	implementation("com.squareup.retrofit2:retrofit:2.9.0")
	implementation("com.squareup.retrofit2:converter-gson:2.9.0")
	//implementation("com.mysql:mysql-connector-j:8.0.33")
	implementation("mysql:mysql-connector-java:8.0.33")
	implementation(kotlin("stdlib-jdk8"))

	implementation("com.google.firebase:firebase-admin:9.2.0")


}

tasks.withType<Test> {
	useJUnitPlatform()
}


//   ssh -i "springboot-imgdb.pem" ImagesDB-0.0.1-SNAPSHOT.jar ec2-user@ec2-100-53-229-181.compute-1.amazonaws.com :/home/ec2-user