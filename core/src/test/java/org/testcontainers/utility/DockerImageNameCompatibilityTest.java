package org.testcontainers.utility;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.core.StringContains.containsString;
import static org.rnorth.visibleassertions.VisibleAssertions.assertFalse;
import static org.rnorth.visibleassertions.VisibleAssertions.assertTrue;


public class DockerImageNameCompatibilityTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testPlainImage() {
        DockerImageName subject = DockerImageName.parse("foo");

        assertFalse("image name foo != bar", subject.isCompatibleWith(DockerImageName.parse("bar")));
    }
    @Test
    public void testLatestTreatedAsWildcard() {
        final DockerImageName subject = DockerImageName.parse("foo:4.5.6");
        /*
        foo:1.2.3 != foo:4.5.6
        foo:1.2.3 ~= foo
        foo:1.2.3 ~= foo:latest

        The test is effectively making sure that no tag and `latest` tag are equivalent
         */
        assertFalse("foo:4.5.6 != foo:1.2.3", subject.isCompatibleWith(DockerImageName.parse("foo:1.2.3")));
        assertTrue("foo:4.5.6 ~= foo", subject.isCompatibleWith(DockerImageName.parse("foo")));
        assertTrue("foo:4.5.6 ~= foo:latest", subject.isCompatibleWith(DockerImageName.parse("foo:latest")));
        assertTrue("foo:4.5.6 ~= foo:latest", subject.isCompatibleWith(DockerImageName.parse("foo:1.2.3").withTag("latest")));
    }

    @Test
    public void testImageWithAutomaticCompatibilityForFullPath() {
        DockerImageName subject = DockerImageName.parse("repo/foo:1.2.3");

        assertTrue("repo/foo:1.2.3 ~= repo/foo", subject.isCompatibleWith(DockerImageName.parse("repo/foo")));
    }

    @Test
    public void testImageWithClaimedCompatibility() {
        DockerImageName subject = DockerImageName.parse("foo").asCompatibleSubstituteFor("bar");

        assertTrue("foo(bar) ~= bar", subject.isCompatibleWith(DockerImageName.parse("bar")));
        assertFalse("foo(bar) != fizz", subject.isCompatibleWith(DockerImageName.parse("fizz")));
    }

    @Test
    public void testImageWithClaimedCompatibilityAndVersion() {
        DockerImageName subject = DockerImageName.parse("foo:1.2.3").asCompatibleSubstituteFor("bar");

        assertTrue("foo:1.2.3(bar) ~= bar", subject.isCompatibleWith(DockerImageName.parse("bar")));
    }

    @Test
    public void testImageWithClaimedCompatibilityForFullPath() {
        DockerImageName subject = DockerImageName.parse("foo").asCompatibleSubstituteFor("registry/repo/bar");

        assertTrue("foo(registry/repo/bar) ~= registry/repo/bar", subject.isCompatibleWith(DockerImageName.parse("registry/repo/bar")));
        assertFalse("foo(registry/repo/bar) != repo/bar", subject.isCompatibleWith(DockerImageName.parse("repo/bar")));
        assertFalse("foo(registry/repo/bar) != bar", subject.isCompatibleWith(DockerImageName.parse("bar")));
    }

    @Test
    public void testImageWithClaimedCompatibilityForVersion() {
        DockerImageName subject = DockerImageName.parse("foo").asCompatibleSubstituteFor("bar:1.2.3");

        assertTrue("foo(bar:1.2.3) ~= bar", subject.isCompatibleWith(DockerImageName.parse("bar")));
        assertTrue("foo(bar:1.2.3) ~= bar:1.2.3", subject.isCompatibleWith(DockerImageName.parse("bar:1.2.3")));
        assertTrue("foo(bar:1.2.3) ~= bar:latest", subject.isCompatibleWith(DockerImageName.parse("bar:latest")));
        assertFalse("foo(bar:1.2.3) != bar:0.0.1", subject.isCompatibleWith(DockerImageName.parse("bar:0.0.1")));
        assertFalse("foo(bar:1.2.3) != bar:2.0.0", subject.isCompatibleWith(DockerImageName.parse("bar:2.0.0")));
        assertFalse("foo(bar:1.2.3) != bar:1.2.4", subject.isCompatibleWith(DockerImageName.parse("bar:1.2.4")));
    }

    @Test
    public void testCheckMethodAcceptsCompatible() {
        DockerImageName subject = DockerImageName.parse("foo").asCompatibleSubstituteFor("bar");
        subject.assertCompatibleWith(DockerImageName.parse("bar"));
    }

    @Test
    public void testCheckMethodRejectsIncompatible() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage(containsString("Failed to verify that image 'foo' is a compatible substitute for 'bar'"));

        DockerImageName subject = DockerImageName.parse("foo");
        subject.assertCompatibleWith(DockerImageName.parse("bar"));
    }
}
