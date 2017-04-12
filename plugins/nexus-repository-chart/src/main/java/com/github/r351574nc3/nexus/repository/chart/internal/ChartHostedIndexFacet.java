package com.github.r351574nc3.nexus.repository.chart.internal;

import java.io.IOException;

import javax.inject.Named;

import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.transaction.UnitOfWork;

import com.github.r351574nc3.nexus.repository.chart.ChartIndexFacet;
import com.github.r351574nc3.nexus.repository.chart.internal.ChartIndexFacetSupport;
import com.github.r351574nc3.nexus.repository.chart.internal.ChartIndexPublisher;

/**
 * Hosted implementation of {@link ChartIndexFacet}.
 *
 * @since 3.0
 */
@Named
public class ChartHostedIndexFacet extends ChartIndexFacetSupport {
    @Override
    public void publishIndex() throws Exception {
        log.info("Publishing new index");
        UnitOfWork.begin(getRepository().facet(StorageFacet.class).txSupplier());
        try {
            ChartIndexPublisher.publishHostedIndex(getRepository());
        }
        finally {
            UnitOfWork.end();
        }
    }
}