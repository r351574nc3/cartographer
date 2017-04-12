package com.github.r351574nc3.nexus.repository.chart;
/**
 * Helper methods for extracting component/asset coordinates for chart artifacts.
 *
 * @since 3.0
 */
public class ChartCoordinatesHelper {
  public static String getGroup(String path) {
    StringBuilder group = new StringBuilder();
    if (!path.startsWith("/")) {
      group.append("/");
    }
    int i = path.lastIndexOf("/");
    if (i != -1) {
      group.append(path.substring(0, i));
    }
    return group.toString();
  }

  private ChartCoordinatesHelper() {
    // Don't instantiate
  }
}
