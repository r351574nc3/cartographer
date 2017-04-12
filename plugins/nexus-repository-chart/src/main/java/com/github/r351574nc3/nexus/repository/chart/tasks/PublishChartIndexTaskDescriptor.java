package com.github.r351574nc3.nexus.repository.chart.tasks;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.formfields.RepositoryCombobox;
import org.sonatype.nexus.scheduling.TaskDescriptorSupport;

import com.github.r351574nc3.nexus.repository.chart.internal.ChartFormat;

/**
 * Task descriptor for {@link PublishChartIndexTask}.
 *
 * @since 3.0
 */
@Named
@Singleton
public class PublishChartIndexTaskDescriptor extends TaskDescriptorSupport {
  public static final String TYPE_ID = "repository.chart.publish-dotindex";

  public static final String REPOSITORY_NAME_FIELD_ID = "repositoryName";

  public PublishChartIndexTaskDescriptor() {
    super(TYPE_ID,
        PublishChartIndexTask.class,
        "Publish Chart indexes",
        VISIBLE,
        EXPOSED,
        new RepositoryCombobox(
            REPOSITORY_NAME_FIELD_ID,
            "Repository",
            "Select the Chart repository to publish indexes for",
            true
        ).includingAnyOfFormats(ChartFormat.NAME).includeAnEntryForAllRepositories()
    );
  }
}