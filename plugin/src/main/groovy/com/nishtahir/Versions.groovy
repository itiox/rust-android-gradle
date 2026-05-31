package com.nishtahir

import com.google.common.collect.ImmutableMultimap
import com.google.common.collect.ImmutableSortedSet
import com.google.common.collect.Multimap
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.apache.maven.artifact.versioning.ComparableVersion
import org.gradle.util.GradleVersion

@CompileStatic(TypeCheckingMode.SKIP)
class Versions {
    static final ComparableVersion PLUGIN_VERSION;
    static final Set<GradleVersion> SUPPORTED_GRADLE_VERSIONS
    static final Set<ComparableVersion> SUPPORTED_ANDROID_VERSIONS
    static final Multimap<ComparableVersion, GradleVersion> SUPPORTED_VERSIONS_MATRIX

    static {
        def versions = new JsonSlurper().parse(Versions.classLoader.getResource("versions.json"))
        PLUGIN_VERSION = new ComparableVersion(versions.version)

        def builder = ImmutableMultimap.<ComparableVersion, GradleVersion>builder()
        versions.supportedVersions.each { String androidVersion, List<String> gradleVersions ->
            builder.putAll(android(androidVersion), gradleVersions.collect { gradle(it) })
        }
        def matrix = builder.build()

        SUPPORTED_VERSIONS_MATRIX = matrix
        SUPPORTED_ANDROID_VERSIONS = ImmutableSortedSet.copyOf(matrix.keySet())
        SUPPORTED_GRADLE_VERSIONS = ImmutableSortedSet.copyOf(matrix.values())
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
