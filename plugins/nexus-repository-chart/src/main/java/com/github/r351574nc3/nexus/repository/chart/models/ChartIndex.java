package com.github.r351574nc3.nexus.repository.chart.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * 
 */
public class ChartIndex {
    protected Map<String, List<ChartMetadata>> entries;
    protected String apiVersion;
    protected String generated;

    public ChartIndex() {
        setEntries(new HashMap<String, List<ChartMetadata>>());
    }

    public void setEntries(final Map<String, List<ChartMetadata>> entries) {
        this.entries = entries;
    }

    public Map<String, List<ChartMetadata>> getEntries() {
        return this.entries;
    }

    public void setApiVersion(final String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getApiVersion() {
        return this.apiVersion;
    }
    public void setGenerated(final String generated) {
        this.generated = generated;
    }

    public String getGenerated() {
        return this.generated;
    }

    public void updateMetadata(final ChartMetadata metadata) {
        getEntries().put(metadata.getName(), new ArrayList<ChartMetadata>() {{
            add(metadata);
        }});
    }

    public String toYaml() throws Exception {
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.writeValueAsString(this);
    }
}