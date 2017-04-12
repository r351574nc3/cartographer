package com.github.r351574nc3.nexus.repository.chart.tasks;

import java.io.IOException;

import javax.inject.Named;

import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.RepositoryTaskSupport;
import com.github.r351574nc3.nexus.repository.chart.ChartIndexFacet;
import com.github.r351574nc3.nexus.repository.chart.internal.ChartFormat;

/**
 * Unpublish Chart Index
 *
 * @since 3.0
 */
@Named
public class UnpublishChartIndexTask extends RepositoryTaskSupport {
  @Override
  protected void execute(final Repository repository) {
    final ChartIndexFacet chartIndexFacet = repository.facet(ChartIndexFacet.class);
    try {
      chartIndexFacet.unpublishIndex();
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected boolean appliesTo(final Repository repository) {
    return repository.getFormat().getValue().equals(ChartFormat.NAME);
  }

  @Override
  public String getMessage() {
    return "Unpublish Chart indexes of " + getRepositoryField();
  }
}