package com.github.r351574nc3.nexus.repository.chart.internal;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.sonatype.nexus.blobstore.api.Blob;
import org.sonatype.nexus.common.collect.AttributesMap;
import org.sonatype.nexus.common.hash.HashAlgorithm;
import org.sonatype.nexus.common.text.Strings2;
import org.sonatype.nexus.orient.entity.AttachedEntityHelper;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.proxy.ProxyFacet;
import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.repository.storage.AssetBlob;
import org.sonatype.nexus.repository.storage.Bucket;
import org.sonatype.nexus.repository.storage.Component;
import org.sonatype.nexus.repository.storage.StorageTx;
import org.sonatype.nexus.repository.storage.TempBlob;
import org.sonatype.nexus.repository.types.ProxyType;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.ContentTypes;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Request;
import org.sonatype.nexus.repository.view.payloads.StreamPayload;
import org.sonatype.nexus.repository.view.payloads.StreamPayload.InputStreamSupplier;
import org.sonatype.nexus.transaction.Transactional;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.repository.view.payloads.BlobPayload;
import org.sonatype.nexus.transaction.UnitOfWork;

import com.google.common.base.Predicate;
import com.google.common.io.Closer;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.r351574nc3.nexus.repository.chart.ChartContentFacet;
import com.github.r351574nc3.nexus.repository.chart.ChartCoordinatesHelper;
import com.github.r351574nc3.nexus.repository.chart.models.ChartIndex;
import com.github.r351574nc3.nexus.repository.chart.models.ChartMetadata;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import com.google.common.base.Suppliers;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static java.util.Collections.singletonList;
import static java.util.stream.StreamSupport.stream;
import static org.sonatype.nexus.common.hash.HashAlgorithm.MD5;
import static org.sonatype.nexus.common.hash.HashAlgorithm.SHA1;
import static org.sonatype.nexus.repository.http.HttpMethods.GET;
import static org.sonatype.nexus.repository.storage.AssetEntityAdapter.P_ASSET_KIND;
import static org.sonatype.nexus.repository.storage.MetadataNodeEntityAdapter.P_BUCKET;
import static org.sonatype.nexus.repository.storage.MetadataNodeEntityAdapter.P_NAME;

/**
 * Helpers for MI index publishing.
 *
 * @since 3.0
 */
public final class ChartIndexPublisher {
  private static final Logger log = LoggerFactory.getLogger(ChartIndexPublisher.class);

  private static final List<HashAlgorithm> hashAlgorithms = Arrays.asList(MD5, SHA1);

  private static final String INDEX_PROPERTY_FILE      = "index.yaml";
  private static final String INDEX_PROPERTY_FILE_TYPE = "text/yaml";
  private static final String GENERATED_DATE_FORMAT    = "yyyy-MM-dd'T'HH:mm:ss.'000000000'ZZ";
  private static final String DEFAULT_CHART_ENGINE     = "gotpl";

  private static final String SELECT_HOSTED_ARTIFACTS =
      "SELECT " +
          "last_updated AS lastModified, " +
          "component.name AS name, " +
          "component.version AS version, " +
          "component.attributes.chart.apiVersion AS apiVersion, " +
          "fileName AS path, " +
          "attributes.content.last_modified AS contentLastModified, " +
          "size AS contentSize, " +
          "attributes.checksum.sha1 AS sha1 " +
          "FROM asset " +
          "WHERE bucket=:bucket " +
          "AND attributes.chart.asset_kind=:asset_kind " +
          "AND component IS NOT NULL";

  private ChartIndexPublisher() {
    // nop
  }

  /**
   * Returns the {@link DateTime} when index of the given repository was last published.
   */
  public static DateTime lastPublished(final Repository repository) throws Exception {
    checkNotNull(repository);
    return null;
  }

  /**
   * Prefetch proxy repository index files, if possible. Returns {@code true} if successful. Accepts only maven proxy
   * types. Returns {@code true} if successfully prefetched files (they exist on remote and are locally cached).
   */
  public static boolean prefetchIndexFiles(final Repository repository) throws Exception {
    checkNotNull(repository);
    checkArgument(ProxyType.NAME.equals(repository.getType().getValue()));
    return prefetch(repository, INDEX_PROPERTY_FILE);
  }

  /**
   * Deletes index files from given repository, returns {@code true} if there was index in repository.
   */
  public static boolean unpublishIndexFiles(final Repository repository) throws Exception {
    checkNotNull(repository);
    return delete(repository, INDEX_PROPERTY_FILE);
  }

  /**
   * Publishes MI index into {@code target}, sourced from {@code repositories} repositories.
   */
  public static void publishMergedIndex(final Repository target, final List<Repository> repositories) throws IOException {
    checkNotNull(target);
    checkNotNull(repositories);
  }

    protected static Iterable<Asset> browseAssets(final Repository repository) throws Exception {
        final String repoName = repository.getName();

        final StorageTx tx = UnitOfWork.currentTx();
        final Bucket chartBucket = stream(tx.browseBuckets().spliterator(), false)
                  .filter((bucket) -> bucket.getRepositoryName().equals(repoName))
                  .findFirst()
                  .orElseThrow(
          () -> new IllegalStateException("No bucket exists for repository: " + repoName)
        );
        return stream(tx.browseAssets(chartBucket).spliterator(), false).collect(Collectors.toList());
    }

    protected static Optional<Asset> findIndexAsset(final Repository repository) throws Exception {
          final StorageTx tx = UnitOfWork.currentTx();
          final Bucket chartBucket = tx.findBucket(repository);
          return Optional.ofNullable(tx.findAssetWithProperty("name", INDEX_PROPERTY_FILE, chartBucket));
    } 

    protected static ChartIndex parseIndex(final Asset indexAsset) throws Exception {
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
          final ChartIndex index = mapper.readValue(toContent(indexAsset).openInputStream(), ChartIndex.class);
          if (index == null) {
            return createIndex();
          }
        }
        catch (Exception e) {
        }
        return createIndex();
    }

    protected static ChartMetadata parseMetadata(final InputStream chartStream) throws Exception {
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        final ChartMetadata retval = mapper.readValue(chartStream, ChartMetadata.class);
        
        if (retval.getCreated() == null) {
          retval.setCreated(chartDate());
        }
        if (retval.getEngine() == null) {
          retval.setEngine(DEFAULT_CHART_ENGINE);
        }

        return retval;
    }

    protected static ChartIndex createIndex() {
        final ChartIndex retval = new ChartIndex();
        retval.setApiVersion("v1");
        retval.setGenerated(chartDate());
        return retval;
    }

    protected static String chartDate() {
      final DateTime dt = new DateTime();
      final DateTimeFormatter fmt = DateTimeFormat.forPattern(GENERATED_DATE_FORMAT);
      return fmt.print(dt);
    }

    protected static Asset indexAsset(final Repository repository) throws Exception {
        final StorageTx tx = UnitOfWork.currentTx();

        final Bucket bucket = tx.findBucket(repository);
        Component component = tx.findComponentWithProperty(P_NAME, INDEX_PROPERTY_FILE, bucket);
        Asset asset;
        if (component == null) {
          // CREATE
          component = tx.createComponent(bucket, repository.getFormat())
              .group(ChartCoordinatesHelper.getGroup(INDEX_PROPERTY_FILE))
              .name(INDEX_PROPERTY_FILE);

          tx.saveComponent(component);

          log.info("Creating a new asset.");
          final ChartIndex index = createIndex();
          asset = tx.createAsset(bucket, component);
          asset.name(INDEX_PROPERTY_FILE);
          saveIndex(index, asset);
        }
        else {
            asset = tx.firstAsset(component);
            asset.markAsAccessed();
        }

        return asset;      
    }

    protected static void saveIndex(final ChartIndex index, final Asset asset) throws Exception {
          final StorageTx tx = UnitOfWork.currentTx();

          log.info("Saving index: {}", index.toYaml());
          final AssetBlob blob = tx.setBlob(asset, 
                                            INDEX_PROPERTY_FILE, 
                                            Suppliers.ofInstance(new ByteArrayInputStream(index.toYaml().getBytes())),
                                            hashAlgorithms,
                                            null,
                                            INDEX_PROPERTY_FILE_TYPE,
                                            false);
          asset.markAsAccessed();
          tx.saveAsset(asset);
    }

    public static void updateHostedIndex(final Repository repository, final InputStream chartStream) throws Exception {
        final Asset indexAsset = findIndexAsset(repository)
                .orElse(indexAsset(repository));
        final ChartIndex index = parseIndex(indexAsset);
        final ChartMetadata metadata = parseMetadata(chartStream);
        index.updateMetadata(metadata);
        saveIndex(index, indexAsset);
        log.info("Updated metadata for: {}", metadata.getName());
    }

    /**
     * Publishes MI index into {@code target}, sourced from repository's own CMA structures.
     */
    public static void publishHostedIndex(final Repository repository) throws Exception {
        checkNotNull(repository);
        log.info("Reindexing: {}", repository.getUrl());
        
        browseAssets(repository).forEach(
          (asset) -> {
            log.info("Indexing asset: {} ", asset.name());
          }
        );

        final Asset indexAsset = findIndexAsset(repository)
          .orElse(indexAsset(repository));
        
        parseIndex(indexAsset);

        Transactional.operation.throwing(IOException.class).call(
            () -> {
              // Generate index from repository
              // Write new index here
              // To generate an  index, need to iterate over the repository and grab files. 
              // Parse files and then generate an index from them.
              // Need to get the current repository URL.
              // Probably specify a URL 
              final StorageTx tx = UnitOfWork.currentTx();
              return null;
            }
        );
    }

  /**
   * Primes proxy cache with given path and return {@code true} if succeeds. Accepts only chart proxy type.
   */
  private static boolean prefetch(final Repository repository, final String path) throws IOException {
      return false;
  }

  /**
   * Deletes given path from repository's storage/cache.
   */
  private static boolean delete(final Repository repository, final String path) throws Exception {
    final ChartContentFacet chartFacet = repository.facet(ChartContentFacet.class);
    return chartFacet.delete(path);
  }

  /**
   * Returns the records to publish of a hosted repository, the SELECT result count will be in parity with published
   * records count!
   */
    /*
  private static Iterable<Record> getHostedRecords(final StorageTx tx, final Repository repository) throws IOException {
    Map<String, Object> sqlParams = new HashMap<>();
    sqlParams.put(P_BUCKET, AttachedEntityHelper.id(tx.findBucket(repository)));
    sqlParams.put(P_ASSET_KIND, AssetKind.ARTIFACT.name());
    return transform(
        tx.browse(SELECT_HOSTED_ARTIFACTS, sqlParams),
        (ODocument document) -> toRecord(repository.facet(ChartContentFacet.class), document)
    );
  }
     */

  /**
   * This method is copied from MI and Plexus related methods, to produce exactly same (possibly buggy) extensions out
   * of a file path, as MI client will attempt to "fix" those.
   */
  private static String pathExtension(final String path) {
    String filename = path.toLowerCase(Locale.ENGLISH);
    if (filename.endsWith("tar.gz")) {
      return "tar.gz";
    }
    else if (filename.endsWith("tar.bz2")) {
      return "tar.bz2";
    }
    int lastSep = filename.lastIndexOf('/');
    int lastDot;
    if (lastSep < 0) {
      lastDot = filename.lastIndexOf('.');
    }
    else {
      lastDot = filename.substring(lastSep + 1).lastIndexOf('.');
      if (lastDot >= 0) {
        lastDot += lastSep + 1;
      }
    }
    if (lastDot >= 0 && lastDot > lastSep) {
      return filename.substring(lastDot + 1);
    }
    return null;
  }

    protected static Content toContent(final Asset asset) {
        final StorageTx tx = UnitOfWork.currentTx();
        final Blob blob = tx.requireBlob(asset.requireBlobRef());
        final Content content = new Content(new BlobPayload(blob, asset.requireContentType()));
        Content.extractFromAsset(asset, hashAlgorithms, content.getAttributes());
        return content;
    }
}