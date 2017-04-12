package com.github.r351574nc3.nexus.repository.chart;

import java.io.IOException;

import javax.annotation.Nullable;

import org.sonatype.nexus.repository.Facet;

import org.joda.time.DateTime;

/**
 * Chart specific index facet responsible to generate index (for hosted and group repositories).
 *
 * @since 3.0
 */
@Facet.Exposed
public interface ChartIndexFacet extends Facet {
    /**
     * Returns time when index was last published on this repository, or {@code null} if index is not published for
     * whatever reason.
     */
    @Nullable
    DateTime lastPublished() throws Exception;

    /**
     * Publishes Chart Indexer indexes repository for downstream consumption.
     */
    void publishIndex() throws Exception;

    /**
     * Removes published Chart Indexer indexes from repository (or cache, if proxy).
     */
    void unpublishIndex() throws Exception;
}
