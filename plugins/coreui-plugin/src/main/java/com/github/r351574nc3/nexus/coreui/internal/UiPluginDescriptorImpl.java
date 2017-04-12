package com.github.r351574nc3.nexus.coreui.internal;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.rapture.UiPluginDescriptor;
import org.sonatype.nexus.rapture.UiPluginDescriptorSupport;

import org.eclipse.sisu.Priority;

/**
 * Rapture {@link UiPluginDescriptor} for {@code coreui-plugin}.
 *
 * @since 3.0
 */
@Named
@Singleton
@Priority(Integer.MAX_VALUE - 100) // after nexus-rapture
public class UiPluginDescriptorImpl extends UiPluginDescriptorSupport {
    public UiPluginDescriptorImpl() {
        super("coreui-plugin");
        setNamespace("NX.coreui");
        setConfigClassName("NX.coreui.app.PluginConfig");
    }
}
