package com.miki.footballpitch.player;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

class PlayerArchitectureTest
{
    private JavaClasses importedClasses;

    @BeforeEach
    void setUp()
    {
        importedClasses = new ClassFileImporter()
                .importPackages("com.miki.footballpitch.player");
    }

    @Test
    @DisplayName("All classes that reside under 'model' package MUST be public")
    void allClassesInModelPackageMustBePublic()
    {
        ArchRule rule = classes()
                .that().resideInAPackage("com.miki.footballpitch.player.model..")
                .should().bePublic()
                .as("Classes in model package need to be accessible as domain API");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("All classes that DO NOT reside under 'model' package MUST NOT be public")
    void classesOutsideModelPackageMustNotBePublic()
    {
        ArchRule rule = classes()
                .that().resideInAPackage("com.miki.footballpitch.player..")
                .and().resideOutsideOfPackage("com.miki.footballpitch.player.model..")
                .should().notBePublic()
                .as("Implementation classes should be package-private to enforce encapsulation");

        rule.check(importedClasses);
    }

}