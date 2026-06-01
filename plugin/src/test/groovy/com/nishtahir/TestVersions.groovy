package com.nishtahir

import org.apache.maven.artifact.versioning.ComparableVersion
import org.gradle.util.GradleVersion


class TestVersions {
    static Map<ComparableVersion, SortedSet<GradleVersion>> getAllCandidateTestVersions() {
        def testedVersion = System.getProperty('org.gradle.android.testVersion')
        if (testedVersion) {
            def filtered = Versions.SUPPORTED_VERSIONS_MATRIX.findAll { version, _ ->
                version == new ComparableVersion(testedVersion)
            }
            return filtered.collectEntries { version, gradleVersions ->
                [(version): new TreeSet<GradleVersion>(gradleVersions)]
            } as Map<ComparableVersion, SortedSet<GradleVersion>>
        } else {
            return Versions.SUPPORTED_VERSIONS_MATRIX
        }
    }

    static ComparableVersion latestAndroidVersionForCurrentJDK() {
        String currentJDKVersion = System.getProperty("java.version");
        if (currentJDKVersion.startsWith("1.")) {
            return allCandidateTestVersions.keySet().findAll {it < new ComparableVersion("7.0.0-alpha01")}.max()
        }
        return allCandidateTestVersions.keySet().max()
    }

    static GradleVersion latestGradleVersion() {
        return allCandidateTestVersions.values().flatten().max() as GradleVersion
    }

    static GradleVersion latestSupportedGradleVersionFor(String androidVersion) {
        return latestSupportedGradleVersionFor(new ComparableVersion(androidVersion))
    }

    static GradleVersion latestSupportedGradleVersionFor(ComparableVersion androidVersion) {
        return allCandidateTestVersions.find { Versions.majorMinor(it.key) == Versions.majorMinor(androidVersion) }?.value?.max()
    }

    static ComparableVersion getLatestVersionForAndroid(String version) {
        ComparableVersion versionNumber = new ComparableVersion(version)
        return allCandidateTestVersions.keySet().findAll { Versions.majorMinor(it) == Versions.majorMinor(versionNumber) }?.max()
    }

    static List<ComparableVersion> getLatestAndroidVersions() {
        def minorVersions = allCandidateTestVersions.keySet().collect { Versions.majorMinor(it) }
        return minorVersions.collect { getLatestVersionForAndroid(it) }
    }
}
