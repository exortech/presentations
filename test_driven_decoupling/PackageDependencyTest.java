package com.pulseenergy.test;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import jdepend.framework.JDepend;
import jdepend.framework.JavaPackage;

import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;

/**
 * These tests are designed to enforce our package structure. Tests asserting that internal dependencies are empty are finished. Tests that have
 * 'invalid' are still in progress. Please help by breaking/eliminating these dependencies. Please try not to introduce new invalid
 * dependencies. Sometimes it is necessary to support restructuring, but we should have a plan for breaking the dependency when this happens. When adding valid
 * dependencies, please be sure to avoid circular references.
 */
public class PackageDependencyTest {
	public static final Set<String> EMPTY = Collections.emptySet();

	@Test
	public void utilPackageShouldHaveNoInternalDependencies() throws Exception {
		Set<String> valid = ImmutableSortedSet.of();
		assertThat(findInternalDependencies("com.pulseenergy.util", valid), is(EMPTY));
	}

	@Test
	public void corePackageShouldOnlyDependOn_util() throws Exception {
		Set<String> valid = ImmutableSortedSet.of("com.pulseenergy.util");
		assertThat(findInternalDependencies("com.pulseenergy.core", valid), is(EMPTY));
	}

	@Test
	public void systemPackageShouldOnlyDependOn_core_util() throws Exception {
		Set<String> valid = ImmutableSortedSet.of("com.pulseenergy.core", "com.pulseenergy.util");
		assertThat(findInternalDependencies("com.pulseenergy.system", valid), is(EMPTY));
	}

	@Test
	public void springPackageShouldOnlyDependOn_util() throws Exception {
		Set<String> valid = ImmutableSortedSet.of("com.pulseenergy.util");
		assertThat(findInternalDependencies("com.pulseenergy.spring", valid), is(EMPTY));
	}

	@Test
	public void securityPackageShouldOnlyDependOn_core_springSecurity_system_util() throws Exception {
		Set<String> valid = ImmutableSortedSet.of("com.pulseenergy.core", "com.pulseenergy.spring.security", "com.pulseenergy.system", "com.pulseenergy.util");
		Set<String> invalid = ImmutableSortedSet.of("com.pulseenergy.web.filter"); // this dependency doesn't exist in the source, but AspectJ weaving adds it to the .class (legitimately)
		assertThat(findInternalDependencies("com.pulseenergy.security", valid), is(invalid));
	}

	@Test
	public void webPackageShouldOnlyDependOn_core_security_spring_system_util() throws Exception {
		Set<String> valid = ImmutableSortedSet.of("com.pulseenergy.core", "com.pulseenergy.security", "com.pulseenergy.spring",
				"com.pulseenergy.system", "com.pulseenergy.util");
		Set<String> invalid = ImmutableSortedSet.of(
				"com.pulseenergy.point", // JavascriptGenerator
				"com.pulseenergy.point.model" // JavascriptGenerator
		);
		assertThat(findInternalDependencies("com.pulseenergy.web", valid), is(invalid));
	}

	@Test
	public void apiPackageShouldNotHaveExternalDependencies() throws Exception {
		Set<String> valid = ImmutableSortedSet.of("com.pulseenergy.core", "com.pulseenergy.security", "com.pulseenergy.system", "com.pulseenergy.util", "com.pulseenergy.web");
		assertThat(findInternalDependencies("com.pulseenergy.api", valid), is(EMPTY));
	}

	@Test // TODO-OR - rename package?
	public void localizationPackageShouldOnlyDependOn_core_system_util() throws Exception {
		Set<String> valid = ImmutableSortedSet.of("com.pulseenergy.core", "com.pulseenergy.system", "com.pulseenergy.util");
		Set<String> invalid = ImmutableSortedSet.of("com.pulseenergy.web.filter"); // this dependency doesn't exist in the source, but AspectJ weaving adds it to the .class (legitimately)
		assertThat(findInternalDependencies("com.pulseenergy.localization", valid), is(invalid));
	}

	@Test
	public void userPackageShouldOnlyDependOn_core_security_system_util_web() throws Exception {
		Set<String> valid = ImmutableSortedSet.of("com.pulseenergy.core", "com.pulseenergy.security", "com.pulseenergy.system", "com.pulseenergy.util", "com.pulseenergy.web");
		assertThat(findInternalDependencies("com.pulseenergy.user", valid), is(EMPTY));
	}

	@Test
	// has circular dependency with com.pulseenergy.point
	// To remove the dependency on Point, we'd need a way to ask a WeatherStation for its earliest record
	public void weatherPackageShouldOnlyDependOn_core_system_util() throws Exception {
		Set<String> valid = ImmutableSortedSet.of("com.pulseenergy.core", "com.pulseenergy.system", "com.pulseenergy.util", "com.pulseenergy.web");
		Set<String> invalid = ImmutableSortedSet.of(
				"com.pulseenergy.point",
				"com.pulseenergy.point.data", // DataQueryRepository
				"com.pulseenergy.point.weather"
		);
		assertThat(findInternalDependencies("com.pulseenergy.weather", valid), is(invalid));
	}

	@Test
	public void schedulePackageShouldOnlyDependOn_core_security_system_util_web() throws Exception {
		Set<String> valid = ImmutableSortedSet.of("com.pulseenergy.core", "com.pulseenergy.security", "com.pulseenergy.system", "com.pulseenergy.util", "com.pulseenergy.web");
		assertThat(findInternalDependencies("com.pulseenergy.schedule", valid), is(EMPTY));
	}

	@Test
	public void pointPackageShouldOnlyDependOn_api_core_security_schedule_spring_system_util_web() throws Exception {
		Set<String> valid = ImmutableSortedSet.of("com.pulseenergy.api", "com.pulseenergy.core", "com.pulseenergy.schedule", "com.pulseenergy.security",
				"com.pulseenergy.spring", "com.pulseenergy.system", "com.pulseenergy.util", "com.pulseenergy.web");
		Set<String> invalid = ImmutableSortedSet.of("com.pulseenergy.chart", "com.pulseenergy.connector", "com.pulseenergy.messages",
				"com.pulseenergy.threshold", "com.pulseenergy.reporting.billing", // BillingAccount, BillingAccountRepository referenced in PointController and PointForm for importing invoice data into LinearRegressionModel - break this
				"com.pulseenergy.space", "com.pulseenergy.space.corepoint", "com.pulseenergy.weather");
		assertThat(findInternalDependencies("com.pulseenergy.point", valid), is(invalid));
	}

	@Test
	public void benchmarkingPackageShouldOnlyDependOn_core_system_util() throws Exception {
		Set<String> valid = ImmutableSortedSet.of("com.pulseenergy.core", "com.pulseenergy.system", "com.pulseenergy.util");
		assertThat(findInternalDependencies("com.pulseenergy.benchmarking", valid), is(EMPTY));
	}

	@Test
	public void thresholdPackageShouldOnlyDependOn_core_point_security_spring_system_util_web() throws Exception {
		Set<String> valid = ImmutableSortedSet.of("com.pulseenergy.core", "com.pulseenergy.point", "com.pulseenergy.security", "com.pulseenergy.spring", "com.pulseenergy.system", "com.pulseenergy.util", "com.pulseenergy.web");
		Set<String> invalid = ImmutableSortedSet.of("com.pulseenergy.messages", "com.pulseenergy.space", "com.pulseenergy.subscription",
				"com.pulseenergy.subscription.threshold");
		assertThat(findInternalDependencies("com.pulseenergy.threshold", valid), is(invalid));
	}

	@Test
	public void spacePackageShouldOnlyDependOn_api_benchmarking_core_localization_point_security_spring_system_util_web_weather() throws Exception {
		Set<String> valid = ImmutableSortedSet.of("com.pulseenergy.api", "com.pulseenergy.benchmarking", "com.pulseenergy.core", "com.pulseenergy.ecm", "com.pulseenergy.localization", "com.pulseenergy.point",
				"com.pulseenergy.security", "com.pulseenergy.spring", "com.pulseenergy.system", "com.pulseenergy.util", "com.pulseenergy.weather", "com.pulseenergy.web");
		Set<String> invalid = ImmutableSortedSet.of("com.pulseenergy.homepage");
		assertThat(findInternalDependencies("com.pulseenergy.space", valid), is(invalid));
	}

	@Test
	public void chartPackageShouldOnlyDependOn_core_point_security_spring_system_util_web() throws Exception {
		Set<String> valid = ImmutableSortedSet.of("com.pulseenergy.core", "com.pulseenergy.point", "com.pulseenergy.security", "com.pulseenergy.spring", "com.pulseenergy.system",
				"com.pulseenergy.util", "com.pulseenergy.web");
		Set<String> invalid = ImmutableSortedSet.of("com.pulseenergy.space");
		assertThat(findInternalDependencies("com.pulseenergy.chart", valid), is(invalid));
	}

	@Test
	public void connectorPackageShouldOnlyDependOn_core_security_spring_system_util_web() throws Exception {
		Set<String> valid = ImmutableSortedSet.of("com.pulseenergy.core", "com.pulseenergy.security", "com.pulseenergy.spring", "com.pulseenergy.system",
				"com.pulseenergy.util", "com.pulseenergy.web");
		Set<String> invalid = ImmutableSortedSet.of("com.pulseenergy.messages", "com.pulseenergy.point", "com.pulseenergy.point.data",
				"com.pulseenergy.point.data.sql", "com.pulseenergy.point.reference", "com.pulseenergy.point.validation.skew", "com.pulseenergy.seeding");
		assertThat(findInternalDependencies("com.pulseenergy.connector", valid), is(invalid));
	}

	@Test
	public void messagePackageShouldOnlyDependOn_core_security_spring_system_util_web() throws Exception {
		Set<String> valid = ImmutableSortedSet.of("com.pulseenergy.core", "com.pulseenergy.security", "com.pulseenergy.spring", "com.pulseenergy.system", "com.pulseenergy.util", "com.pulseenergy.web");
		Set<String> invalid = ImmutableSortedSet.of("com.pulseenergy.chart", "com.pulseenergy.connector", "com.pulseenergy.threshold", "com.pulseenergy.point", "com.pulseenergy.point.data", "com.pulseenergy.point.rollover", "com.pulseenergy.subscription");
		assertThat(findInternalDependencies("com.pulseenergy.messages", valid), is(invalid));
	}

	@Test
	public void dashboardPackageShouldOnlyDependOn_core_security_spring_system_util_web() throws Exception {
		Set<String> valid = ImmutableSortedSet.of("com.pulseenergy.core", "com.pulseenergy.security", "com.pulseenergy.spring", "com.pulseenergy.system", "com.pulseenergy.util", "com.pulseenergy.web");
		Set<String> invalid = ImmutableSortedSet.of("com.pulseenergy.chart", "com.pulseenergy.point", "com.pulseenergy.point.data", // PointRepository
				"com.pulseenergy.point.series", // SeriesFetcher, SeriesDataRepository
				"com.pulseenergy.space", // legitimate?
				"com.pulseenergy.user" // UserMailer
		);
		assertThat(findInternalDependencies("com.pulseenergy.dashboard", valid), is(invalid));
	}

	@Test
	public void reportingPackageShouldOnlyDependOn_core_security_spring_system_util_web() throws Exception {
		Set<String> valid = ImmutableSortedSet.of("com.pulseenergy.api", "com.pulseenergy.core", "com.pulseenergy.security", "com.pulseenergy.spring", "com.pulseenergy.system",
				"com.pulseenergy.util", "com.pulseenergy.web", "com.pulseenergy.visualization.export.chart", "com.pulseenergy.point.conversion", "com.pulseenergy.facilitymanager");
		Set<String> invalid = ImmutableSortedSet.of(
				"com.pulseenergy.localization", // CountryRepository
				"com.pulseenergy.point", "com.pulseenergy.point.billing", "com.pulseenergy.point.data", "com.pulseenergy.point.model",
				"com.pulseenergy.point.reference", "com.pulseenergy.point.series", "com.pulseenergy.space", "com.pulseenergy.space.corepoint", "com.pulseenergy.user",
				"com.pulseenergy.dashboard" // performance reports use dashboard images pending further configurability
		);
		assertThat(findInternalDependencies("com.pulseenergy.reporting", valid), is(invalid));
	}

	private Set<String> findInternalDependencies(String targetPackage, Collection<String> validDependencies) throws IOException {
		final Collection<JavaPackage> packages = analyzePackage(targetPackage);
		final Set<String> allPackageViolations = Sets.newTreeSet();
		for (JavaPackage javaPackage : packages) {
			final Set<String> packageViolations = findInvalidDependencies(javaPackage, Iterables.concat(Collections.singleton(targetPackage), validDependencies));
			// System.out.println(javaPackage.getName() + " has " + packageViolations.size() + " invalid dependencies: " + packageViolations);
			allPackageViolations.addAll(packageViolations);
		}
		return allPackageViolations;
	}

	private Collection<JavaPackage> analyzePackage(String targetPackage) throws IOException {
		final JDepend jDepend = new JDepend();
		jDepend.addDirectory(IntrospectionHelper.findClassesDirectory().getAbsolutePath() + "/" + convertPackageToPath(targetPackage));
		@SuppressWarnings("unchecked")
		final Collection<JavaPackage> allPackages = jDepend.analyze();
		final List<JavaPackage> filteredPackages = Lists.newArrayList();
		for (JavaPackage aPackage : allPackages) {
			if (aPackage.getName().startsWith(targetPackage)) {
				filteredPackages.add(aPackage);
			}
		}
		return filteredPackages;
	}

	private String convertPackageToPath(String targetPackage) {
		return targetPackage.replace(".", "/");
	}

	private Set<String> findInvalidDependencies(JavaPackage javaPackage, Iterable<String> validPackages) {
		final Set<String> packageViolations = Sets.newTreeSet();
		@SuppressWarnings("unchecked")
		Collection<JavaPackage> efferents = javaPackage.getEfferents();
		for (JavaPackage efferentPackage : efferents) {
			if (!isValid(efferentPackage, validPackages)) {
				packageViolations.add(efferentPackage.getName());
			}
		}
		return packageViolations;
	}

	private boolean isValid(JavaPackage efferentPackage, Iterable<String> validPackages) {
		if (!isInternalDependency(efferentPackage)) {
			return true;
		}
		for (String validPackage : validPackages) {
			if (efferentPackage.getName().startsWith(validPackage)) {
				return true;
			}
		}
		return false;
	}

	private boolean isInternalDependency(JavaPackage aPackage) {
		return aPackage.getName().startsWith("com.seg") || aPackage.getName().startsWith("com.pulseenergy");
	}
}
