package com.github.r351574nc3.nexus.repository.chart.internal;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.repository.Format;
import org.sonatype.nexus.repository.security.RepositoryFormatSecurityContributor;

/**
 * Chart format security contributor.
 *
 * @since 3.0
 */
@Named
@Singleton
public class ChartFormatSecurityContributor extends RepositoryFormatSecurityContributor {
  @Inject
  public ChartFormatSecurityContributor(@Named(ChartFormat.NAME) final Format format) {
    super(format);
  }
}
