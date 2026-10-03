package com.xyz.platform.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

public class DomainArchitectureTest {

    private static JavaClasses importedClasses;

    @BeforeAll
    static void setUp() {
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.xyz.platform");
    }

    @Test
    void noArbitrarySearchPathExecution() {
        noClasses()
                .that().resideOutsideOfPackages("..tenancy..", "..controlplane.provisioning..")
                .should().dependOnClassesThat().haveFullyQualifiedName("com.xyz.platform.tenancy.provider.MultiTenantConnectionProviderImpl")
                .because("Only tenancy connection provider and provisioning infrastructure may switch or manage PostgreSQL search_path")
                .check(importedClasses);
    }

    @Test
    void sharedDomainAccess() {
        classes()
                .that().resideInAPackage("..shared..")
                .should().onlyBeAccessed().byAnyPackage(
                        "..shared..",
                        "..controlplane..",
                        "..organization..",
                        "..company..",
                        "..business..",
                        "..brand..",
                        "..store..",
                        "..channel..",
                        "..website..",
                        "..warehouse..",
                        "..pos..",
                        "..identity..",
                        "..audit..",
                        "..tenancy..",
                        "..security..",
                        "com.xyz.platform"
                )
                .check(importedClasses);
    }

    @Test
    void controllersShouldBeAnnotatedWithRestController() {
        classes()
                .that().haveSimpleNameEndingWith("Controller")
                .should().beAnnotatedWith(org.springframework.web.bind.annotation.RestController.class)
                .check(importedClasses);
    }

    @Test
    void repositoriesShouldBeInterfaces() {
        classes()
                .that().haveSimpleNameEndingWith("Repository")
                .should().beInterfaces()
                .check(importedClasses);
    }
}
