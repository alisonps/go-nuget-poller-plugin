package com.tw.go.plugin.nuget;

import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
import com.tw.go.plugin.nuget.apimpl.NuGetPoller;
import com.tw.go.plugin.util.RepoUrl;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import static com.tw.go.plugin.nuget.config.NuGetPackageConfig.PACKAGE_LOCATION;
import static com.tw.go.plugin.nuget.config.NuGetPackageConfig.PACKAGE_VERSION;
import static junit.framework.Assert.assertNull;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class NuGetTest {
    @Test
    public void shouldReportLocationCorrectly() {
        PackageRevision result = new NuGetPoller().poll(new NuGetParams(RepoUrl.create("http://www.nuget.org/api/v2", null, null), "RouteMagic.Mvc", null, null, null, true));
        assertThat(result.getDataFor(PACKAGE_LOCATION), is("http://www.nuget.org/api/v2/package/RouteMagic.Mvc/1.2.0"));
    }

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void shouldFailIfNoPackagesFound() {
        expectedEx.expect(NuGetException.class);
        expectedEx.expectMessage("No such package found");
        new NuGetPoller().poll(new NuGetParams(RepoUrl.create("http://nuget.org/api/v2/", null, null), "Rou", null, null, null, true));
    }

    @Test
    public void shouldGetUpdateWhenLastVersionKnown() throws ParseException {
        PackageRevision lastKnownVersion = new PackageRevision("1Password-1.0.9.288", new SimpleDateFormat("yyyy-MM-dd").parse("2013-03-21"), "xyz");
        lastKnownVersion.addData(PACKAGE_VERSION, "1.0.9.288");
        PackageRevision result = new NuGetPoller().poll(new NuGetParams(RepoUrl.create("http://chocolatey.org/api/v2", null, null), "1Password", null, null, lastKnownVersion, true));
        assertThat(result.getDataFor(PACKAGE_VERSION), is("4.2.0.548"));
    }

    @Test
    public void shouldReturnNullIfNoNewerRevision() throws ParseException {
        PackageRevision lastKnownVersion = new PackageRevision("1Password-10.0.9.332", new SimpleDateFormat("yyyy-MM-dd").parse("2013-03-21"), "xyz");
        lastKnownVersion.addData(PACKAGE_VERSION, "10.0.9.332");
        NuGetParams params = new NuGetParams(RepoUrl.create("http://chocolatey.org/api/v2", null, null), "1Password", null, null, lastKnownVersion, true);
        assertNull(new NuGetPoller().poll(params));

    }
}
