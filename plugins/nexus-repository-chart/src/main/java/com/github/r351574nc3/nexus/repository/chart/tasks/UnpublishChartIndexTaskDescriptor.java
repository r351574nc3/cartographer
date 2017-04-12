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
public class UnpublishChartIndexTaskDescriptor extends TaskDescriptorSupport {
  public static final String TYPE_ID = "repository.chart.unpublish-dotindex";

  public static final String REPOSITORY_NAME_FIELD_ID = "repositoryName";

  public UnpublishChartIndexTaskDescriptor() {
    super(TYPE_ID,
        UnpublishChartIndexTask.class,
        "Unpublish Chart indexes",
        VISIBLE,
        EXPOSED,
        new RepositoryCombobox(
            REPOSITORY_NAME_FIELD_ID,
            "Repository",
            "Select the Chart repository to unpublish indexes for",
            true
        ).includingAnyOfFormats(ChartFormat.NAME).includeAnEntryForAllRepositories()
    );
  }
}