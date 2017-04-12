package com.github.r351574nc3.nexus.repository.chart.internal;

import java.io.IOException;

import javax.annotation.Nullable;

import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.transaction.UnitOfWork;

import com.github.r351574nc3.nexus.repository.chart.ChartIndexFacet;

import org.joda.time.DateTime;

/**
 * {@link ChartIndexFacet} support.
 *
 * @since 3.0
 */
public abstract class ChartIndexFacetSupport extends FacetSupport implements ChartIndexFacet {
  @Nullable
  public DateTime lastPublished() throws Exception {
    UnitOfWork.begin(getRepository().facet(StorageFacet.class).txSupplier());
    try {
      return ChartIndexPublisher.lastPublished(getRepository());
    }
    finally {
      UnitOfWork.end();
    }
  }

  @Override
  public void unpublishIndex() throws Exception {
    UnitOfWork.begin(getRepository().facet(StorageFacet.class).txSupplier());
    try {
      ChartIndexPublisher.unpublishIndexFiles(getRepository());
    }
    finally {
      UnitOfWork.end();
    }
  }
}