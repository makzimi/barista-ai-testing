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
include(":data:api")
include(":data:impl")
include(":domain:api")
include(":domain:impl")
include(":feature:menu")
include(":feature:drink-details")
include(":konsist-test")
