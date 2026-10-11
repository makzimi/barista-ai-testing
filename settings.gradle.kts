pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "barista"

include(":app")
include(":core:common")
include(":core:mvi")
include(":core:designsystem")
include(":core:network")
include(":core:testing")
include(":feature:menu:api")
include(":feature:menu:impl")
include(":feature:drink-details:api")
include(":feature:drink-details:impl")
include(":konsist-test")
