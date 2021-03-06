package org.mayocat.multitenancy;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;

import org.mayocat.event.EventListener;
import org.xwiki.component.annotation.Component;

@Component
@Named("defaultHostAndSubdomainSlugTenantResolverEventListener")
public class DefaultHostAndSubdomainSlugTenantResolverServletEventListener
        implements ServletRequestListener, EventListener
{
    @Inject
    @Named("defaultHostAndSubdomain")
    private TenantResolver defaultTenantResolver;

    @Override
    public void requestDestroyed(ServletRequestEvent sre)
    {
        try {
            DefaultHostAndSubdomainSlugTenantResolver dtr =
                    (DefaultHostAndSubdomainSlugTenantResolver) this.defaultTenantResolver;
            if (dtr != null) {
                dtr.requestDestroyed();
            }
        } catch (ClassCastException e) {
            // This mean we are not using the sub-domain slug tenant resolver...
        }
    }

    @Override
    public void requestInitialized(ServletRequestEvent sre)
    {
    }
}
