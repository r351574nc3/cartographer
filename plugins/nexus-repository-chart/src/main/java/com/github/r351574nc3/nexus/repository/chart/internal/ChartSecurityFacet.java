package com.github.r351574nc3.nexus.repository.chart.internal;
import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.repository.security.ContentPermissionChecker;
import org.sonatype.nexus.repository.security.SecurityFacetSupport;
import org.sonatype.nexus.repository.security.VariableResolverAdapter;

/**
 * Chart security facet.
 *
 * @since 3.0
 */
@Named
public class ChartSecurityFacet extends SecurityFacetSupport {
  @Inject
  public ChartSecurityFacet(final ChartFormatSecurityContributor securityContributor,
                            @Named("simple") final VariableResolverAdapter variableResolverAdapter,
                            final ContentPermissionChecker contentPermissionChecker) {
    super(securityContributor, variableResolverAdapter, contentPermissionChecker);
  }
}
