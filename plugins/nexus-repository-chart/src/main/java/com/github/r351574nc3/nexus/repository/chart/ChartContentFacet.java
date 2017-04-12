package com.github.r351574nc3.nexus.repository.chart;
import java.io.IOException;

import javax.annotation.Nullable;

import org.sonatype.nexus.repository.Facet;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.cache.CacheInfo;
import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.Payload;

/**
 * Provides persistent storage for {@link Content}.
 *
 * @since 3.0
 */
@Facet.Exposed
public interface ChartContentFacet extends Facet {
  @Nullable
  Content get(String path) throws Exception;

  Content put(String path, Payload content) throws Exception;

  boolean delete(String path) throws Exception;

  /**
   * Raw proxy facet specific method: invoked when cached content (returned by {@link #get(String)} method of this
   * same facet instance) is found to be up to date after remote checks. This method applies the passed in {@link
   * CacheInfo} to the {@link Content}'s underlying asset.
   */
  void setCacheInfo(String path, Content content, CacheInfo cacheInfo) throws Exception;

  Asset getOrCreateAsset(Repository repository, String componentName, String componentGroup, String assetName);
}
