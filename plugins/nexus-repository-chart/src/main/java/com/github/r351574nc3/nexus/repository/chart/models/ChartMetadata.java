package com.github.r351574nc3.nexus.repository.chart.models;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ChartMetadata {
    protected String apiVersion;
    protected String created;
    protected String description;
    protected String digest;
    protected List<Map<String, String>> maintainers;
    protected String name;
    protected Iterable<String> keywords;
    protected Iterable<String> sources;
    protected Iterable<String> urls;
    protected String version;
    protected String icon;
    protected String home;
    protected String engine;

    public ChartMetadata() {
        setMaintainers(new ArrayList<Map<String, String>>());
        setUrls(new ArrayList<String>());
        setSources(new ArrayList<String>());
        setKeywords(new ArrayList<String>());
    }

    public void setApiVersion(final String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getApiVersion() {
        return this.apiVersion;
    }

    public void setCreated(final String created) {
        this.created = created;
    }

    public String getCreated() {
        return this.created;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }
    public void setDigest(final String digest) {
        this.digest = digest;
    }
    public String getDigest() {
        return this.digest;
    }

    public void setMaintainers(final List<Map<String, String>> maintainers) {
        this.maintainers = maintainers;
    }

    public List<Map<String, String>> getMaintainers() {
        return this.maintainers;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setSources(final Iterable<String> sources) {
        this.sources = sources;
    }

    public Iterable<String> getSources() {
        return this.sources;
    }

    public void setUrls(final Iterable<String> urls) {
        this.urls = urls;
    }

    public Iterable<String> getUrls() {
        return this.urls;
    }

    public void setKeywords(final Iterable<String> keywords) {
        this.keywords = keywords;
    }

    public Iterable<String> getKeywords() {
        return this.keywords;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public String getVersion() {
        return this.version;
    }
    public void setIcon(final String icon) {
        this.icon = icon;
    }

    public String getIcon() {
        return this.icon;
    }

    public void setHome(final String home) {
        this.home = home;
    }

    public String getHome() {
        return this.home;
    }

    public void setEngine(final String engine) {
        this.engine = engine;
    }

    public String getEngine() {
        return this.engine;
    }
}