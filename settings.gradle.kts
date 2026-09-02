// Workaround for AGP 9.3.1 environment variable conflict in Flatpak
try {
    val env = System.getenv()
    val clazz = env.javaClass
    val field = clazz.getDeclaredField("m")
    field.isAccessible = true
    @Suppress("UNCHECKED_CAST")
    val writableEnv = field.get(env) as MutableMap<String, String>
    writableEnv.remove("ANDROID_PREFS_ROOT")
} catch (e: Exception) {
    // Fallback: if reflection fails, try setting a system property that might take precedence
    System.setProperty("android.prefs.root", "/home/mcsilk/.var/app/com.google.AndroidStudio/config")
}

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Sonus"
include(":app")
include(":shared")
include(":desktop")
 