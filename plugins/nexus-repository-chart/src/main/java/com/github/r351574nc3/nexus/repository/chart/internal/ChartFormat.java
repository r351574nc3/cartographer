package com.github.r351574nc3.nexus.repository.chart.internal;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.repository.Format;

/**
 * Chart repository format.
 *
 * @since 3.0
 */
@Named(ChartFormat.NAME)
@Singleton
public class ChartFormat extends Format {
  public static final String NAME = "chart";

  public ChartFormat() {
    super(NAME);
  }
}
