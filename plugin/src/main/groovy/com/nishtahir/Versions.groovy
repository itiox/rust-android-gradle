package com.nishtahir

import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.apache.maven.artifact.versioning.ComparableVersion
import org.gradle.util.GradleVersion

@CompileStatic(TypeCheckingMode.SKIP)
class Versions {
    static final ComparableVersion PLUGIN_VERSION
    static final SortedSet<GradleVersion> SUPPORTED_GRADLE_VERSIONS
    static final SortedSet<ComparableVersion> SUPPORTED_ANDROID_VERSIONS
    static final Map<ComparableVersion, SortedSet<GradleVersion>> SUPPORTED_VERSIONS_MATRIX

    static {
        def versions = new JsonSlurper().parse(Versions.classLoader.getResource("versions.json"))
        PLUGIN_VERSION = new ComparableVersion(versions.version)

        def matrix = new TreeMap<ComparableVersion, SortedSet<GradleVersion>>()
        versions.supportedVersions.each { String androidVersion, List<String> gradleVersions ->
            def androidVersionKey = android(androidVersion)
            def gradleVersionSet = matrix.computeIfAbsent(androidVersionKey) { new TreeSet<GradleVersion>() }
            gradleVersionSet.addAll(gradleVersions.collect { gradle(it) })
        }
        def immutableMatrix = Collections.unmodifiableMap(
                matrix.collectEntries { version, gradleVersions ->
                    [(version): Collections.unmodifiableSortedSet(new TreeSet<GradleVersion>(gradleVersions))]
                } as Map<ComparableVersion, SortedSet<GradleVersion>>)

        SUPPORTED_VERSIONS_MATRIX = immutableMatrix
        SUPPORTED_ANDROID_VERSIONS = Collections.unmodifiableSortedSet(new TreeSet<ComparableVersion>(immutableMatrix.keySet()))

        def allGradleVersions = new TreeSet<GradleVersion>()
        immutableMatrix.values().each { allGradleVersions.addAll(it) }
        SUPPORTED_GRADLE_VERSIONS = Collections.unmodifiableSortedSet(allGradleVersions)
    }

    static ComparableVersion android(String version) {
        new ComparableVersion(version)
    }

    static GradleVersion gradle(String version) {
        GradleVersion.version(version)
    }

    static ComparableVersion latestAndroidVersion() {
        return SUPPORTED_ANDROID_VERSIONS.max()
    }

    static String majorMinor(ComparableVersion version) {
        def parts = version.toString().split(/[.-]/)
        def major = parts.length > 0 ? parts[0] : "0"
        def minor = parts.length > 1 ? parts[1] : "0"
        return "${major}.${minor}"
    }
}
