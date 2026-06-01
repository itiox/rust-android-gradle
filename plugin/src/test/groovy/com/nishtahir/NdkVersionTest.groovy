package com.nishtahir

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Unroll

class NdkVersionTest extends AbstractTest {
    private static final String DEFAULT_NDK_VERSION = System.getProperty("org.gradle.android.ndkVersion")

    @Unroll
    def "cargoBuild works with Android NDK version #ndkVersion"() {
        given:
        def androidVersion = TestVersions.latestAndroidVersionForCurrentJDK()
        def target = "x86_64"
        def location = "android/x86_64/librust.so"

        SimpleAndroidApp.builder(temporaryFolder.root, cacheDir)
                .withAndroidVersion(androidVersion)
                .withNdkVersion(ndkVersion)
                .withKotlinDisabled()
        // TODO: .withCargo(...)
                .build()
                .writeProject()

        SimpleCargoProject.builder(temporaryFolder.root)
                .withTargets([target])
                .build()
                .writeProject()

        // To ease debugging.
        temporaryFolder.root.eachFileRecurse {
            System.err.println("before> ${it}")
            if (it.path.endsWith(".gradle")) {
                System.err.println(it.text)
            }
        }

        when:
        BuildResult buildResult = withGradleVersion(TestVersions.latestSupportedGradleVersionFor(androidVersion).version)
                .withProjectDir(temporaryFolder.root)
                .withArguments('cargoBuild', '--info', '--stacktrace')
        // .withDebug(true)
                .build()

        // To ease debugging.
        temporaryFolder.root.eachFileRecurse {
            System.err.println("after> ${it}")
        }

        then:
        buildResult.task(':app:cargoBuild').outcome == TaskOutcome.SUCCESS
        buildResult.task(':library:cargoBuild').outcome == TaskOutcome.SUCCESS
        new File(temporaryFolder.root, "app/build/rustJniLibs/${location}").exists()
        new File(temporaryFolder.root, "library/build/rustJniLibs/${location}").exists()

        where:
        ndkVersion << [
            // Partial list of NDK versions supported by Github Actions, per
            // https://github.com/actions/runner-images/blob/main/images/ubuntu/Ubuntu2204-Readme.md
            DEFAULT_NDK_VERSION,
        ]
    }
}
