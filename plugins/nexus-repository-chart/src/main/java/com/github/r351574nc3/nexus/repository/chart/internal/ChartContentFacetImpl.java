package com.github.r351574nc3.nexus.repository.chart.internal;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Named;

import org.sonatype.nexus.blobstore.api.Blob;
import org.sonatype.nexus.common.collect.AttributesMap;
import org.sonatype.nexus.common.hash.HashAlgorithm;
import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.cache.CacheInfo;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.repository.storage.AssetBlob;
import org.sonatype.nexus.repository.storage.Bucket;
import org.sonatype.nexus.repository.storage.Component;
import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.repository.storage.StorageTx;
import org.sonatype.nexus.repository.storage.TempBlob;
import org.sonatype.nexus.repository.transaction.TransactionalDeleteBlob;
import org.sonatype.nexus.repository.transaction.TransactionalStoreBlob;
import org.sonatype.nexus.repository.transaction.TransactionalStoreMetadata;
import org.sonatype.nexus.repository.transaction.TransactionalTouchBlob;
import org.sonatype.nexus.repository.transaction.TransactionalTouchMetadata;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.repository.view.payloads.BlobPayload;
import org.sonatype.nexus.transaction.UnitOfWork;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.utils.IOUtils;

import com.github.r351574nc3.nexus.repository.chart.ChartContentFacet;
import com.github.r351574nc3.nexus.repository.chart.ChartCoordinatesHelper;

import static org.sonatype.nexus.common.hash.HashAlgorithm.MD5;
import static org.sonatype.nexus.common.hash.HashAlgorithm.SHA1;
import static org.sonatype.nexus.repository.storage.MetadataNodeEntityAdapter.P_NAME;

/**
 * A {@link ChartContentFacet} that persists to a {@link StorageFacet}.
 *
 * @since 3.0
 */
@Named
public class ChartContentFacetImpl extends FacetSupport implements ChartContentFacet {
    private static final List<HashAlgorithm> hashAlgorithms = Arrays.asList(MD5, SHA1);
    protected static final String CHART_METADATA_FILENAME = "Chart.yaml";

    // TODO: raw does not have config, this method is here only to have this bundle do Import-Package org.sonatype.nexus.repository.config
    // TODO: as FacetSupport subclass depends on it. Actually, this facet does not need any kind of configuration
    // TODO: it's here only to circumvent this OSGi/maven-bundle-plugin issue.
    @Override
    protected void doValidate(final Configuration configuration) throws Exception {
      // empty
    }

    @Nullable
    @Override
    @TransactionalTouchBlob
    public Content get(final String path) {
        log.info("Fetching " + path);
        final StorageTx tx = UnitOfWork.currentTx();

        final Asset asset = findAsset(tx, path);
        if (asset == null) {
            return null;
        }
        if (asset.markAsAccessed()) {
            tx.saveAsset(asset);
        }

        final Blob blob = tx.requireBlob(asset.requireBlobRef());

        return toContent(asset, blob);
    }

    @Override
    public Content put(final String path, final Payload content) throws Exception {
        log.info("Putting {}", path);
        StorageFacet storageFacet = facet(StorageFacet.class);
        try (TempBlob tempBlob = storageFacet.createTempBlob(content, hashAlgorithms)) {
            return doPutContent(path, tempBlob, content);
        }
    }

      @TransactionalStoreBlob
      protected Content doPutContent(final String path, final TempBlob tempBlob, final Payload payload)
          throws Exception {
          final StorageTx tx = UnitOfWork.currentTx();

          final Asset asset = getOrCreateAsset(getRepository(), path, ChartCoordinatesHelper.getGroup(path), path);

          AttributesMap contentAttributes = null;
          if (payload instanceof Content) {
              contentAttributes = ((Content) payload).getAttributes();
          }
          Content.applyToAsset(asset, Content.maintainLastModified(asset, contentAttributes));
          AssetBlob assetBlob = tx.setBlob(
              asset,
              path,
              tempBlob,
              null,
              payload.getContentType(),
              false
          );

          try {
              tx.saveAsset(asset);

              if (!asset.name().equals("index.yaml")) { 
                log.info("Publishing new index");
                final InputStream indexStream = getMetadataFromStream(tempBlob.get());
                ChartIndexPublisher.updateHostedIndex(getRepository(), indexStream);
              }
          }
          finally {
          }

          return toContent(asset, assetBlob.getBlob());
      }

      protected File createTempChartFile(final InputStream stream) throws Exception {
          final File tempTar = File.createTempFile("tmp", "tgz");
          final FileOutputStream fout = new FileOutputStream(tempTar);
          try {
            IOUtils.copy(stream, fout);
          }
          finally {
            fout.close();
          }

          return tempTar;
      }

      protected InputStream getMetadataFromStream(final InputStream stream) throws Exception {
          // final File tempChart = createTempChartFile(stream);
          final CompressorInputStream decompressedIn = new CompressorStreamFactory().createCompressorInputStream(new BufferedInputStream(stream));
          final TarArchiveInputStream input = (TarArchiveInputStream) new ArchiveStreamFactory().createArchiveInputStream(new BufferedInputStream(decompressedIn));

          TarArchiveEntry entry = input.getNextTarEntry();

          while (entry != null) {
              if (entry.getName().endsWith(CHART_METADATA_FILENAME) && entry.isFile()) {
                  long pos = input.getBytesRead();
                  final ByteArrayOutputStream data = new ByteArrayOutputStream();
                  while ((input.getBytesRead() - entry.getSize()) < pos) {
                    data.write(input.read());
                  }
                  try {
                    return new ByteArrayInputStream(data.toByteArray());
                  }
                  finally {
                    data.close();
                  }
              }
              entry = input.getNextTarEntry();
          }
          // tempChart.delete();
          return null;
      }


    @TransactionalStoreMetadata
    public Asset getOrCreateAsset(final Repository repository, final String componentName, final String componentGroup,
                                  final String assetName) {
      final StorageTx tx = UnitOfWork.currentTx();

      final Bucket bucket = tx.findBucket(getRepository());
      Component component = tx.findComponentWithProperty(P_NAME, componentName, bucket);
      Asset asset;
      if (component == null) {
        // CREATE
        component = tx.createComponent(bucket, getRepository().getFormat())
            .group(componentGroup)
            .name(componentName);

        tx.saveComponent(component);

        asset = tx.createAsset(bucket, component);
        asset.name(assetName);
      }
      else {
        // UPDATE
        asset = tx.firstAsset(component);
      }

      asset.markAsAccessed();

      return asset;
    }

    @Override
    @TransactionalDeleteBlob
    public boolean delete(final String path) throws Exception {
      StorageTx tx = UnitOfWork.currentTx();

      final Component component = findComponent(tx, tx.findBucket(getRepository()), path);
      if (component == null) {
        return false;
      }

      tx.deleteComponent(component);
      return true;
    }

    @Override
    @TransactionalTouchMetadata
    public void setCacheInfo(final String path, final Content content, final CacheInfo cacheInfo) throws Exception {
      StorageTx tx = UnitOfWork.currentTx();
      Bucket bucket = tx.findBucket(getRepository());

      // by EntityId
      Asset asset = Content.findAsset(tx, bucket, content);
      if (asset == null) {
        // by format coordinates
        Component component = tx.findComponentWithProperty(P_NAME, path, bucket);
        if (component != null) {
          asset = tx.firstAsset(component);
        }
      }
      if (asset == null) {
        log.debug("Attempting to set cache info for non-existent chart component {}", path);
        return;
      }

      log.debug("Updating cacheInfo of {} to {}", path, cacheInfo);
      CacheInfo.applyToAsset(asset, cacheInfo);
      tx.saveAsset(asset);
    }

    private Component findComponent(StorageTx tx, Bucket bucket, String path) {
      return tx.findComponentWithProperty(P_NAME, path, bucket);
    }

    private Asset findAsset(StorageTx tx, String path) {
      return tx.findAssetWithProperty(P_NAME, path, tx.findBucket(getRepository()));
    }

    private Content toContent(final Asset asset, final Blob blob) {
      final Content content = new Content(new BlobPayload(blob, asset.requireContentType()));
      Content.extractFromAsset(asset, hashAlgorithms, content.getAttributes());
      return content;
    }
}
