/*
 * Datapipe Trebuchet (TM) 
 * Copyright (c) 2017-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code.
 */
/*global Ext, NX*/

/**
 * Repository "Settings" form for a Chart Hosted repository.
 *
 * @since 3.0
 */
Ext.define('trebuchet.view.repository.recipe.ChartHosted', {
  extend: 'trebuchet.view.repository.RepositorySettingsForm',
  alias: 'widget.trebuchet-repository-chart-hosted',
  requires: [
    'NX.coreui.view.repository.facet.StorageFacet',
    'NX.coreui.view.repository.facet.StorageFacetHosted'
  ],

  /**
   * @override
   */
  initComponent: function () {
    var me = this;

    me.items = [
      {xtype: 'nx-coreui-repository-storage-facet'},
      {xtype: 'nx-coreui-repository-storage-hosted-facet', writePolicy: 'ALLOW'}
    ];

    me.callParent();
  }
});
