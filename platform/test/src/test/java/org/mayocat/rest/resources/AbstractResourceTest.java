package org.mayocat.rest.resources;

import java.util.Arrays;
import java.util.Map;

import javax.jdo.JDOHelper;

import org.mayocat.configuration.SecuritySettings;
import org.mayocat.rest.Provider;
import org.mayocat.configuration.DataSourceConfiguration;
import org.mayocat.configuration.MayocatShopConfiguration;
import org.mayocat.configuration.MultitenancySettings;
import org.mayocat.accounts.store.TenantStore;
import org.mayocat.store.datanucleus.PersistenceManagerProvider;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.component.embed.EmbeddableComponentManager;

import com.yammer.dropwizard.testing.ResourceTest;

public abstract class AbstractResourceTest extends ResourceTest
{

    protected EmbeddableComponentManager componentManager;

    private TenantStore tenantStore;

    public AbstractResourceTest()
    {
        super();
    }

    @Override
    protected void setUpResources() throws Exception
    {
        // Initialize Rendering components and allow getting instances
        componentManager = new EmbeddableComponentManager();
        componentManager.initialize(this.getClass().getClassLoader());

        this.registerConfigurationsAsComponents();

        // Registering provider component implementations against the test
        // environment...
        Map<String, Provider> providers =
                componentManager.getInstanceMap(Provider.class);
        for (Map.Entry<String, Provider> provider : providers.entrySet()) {
            this.addResource(provider.getValue());
        }
    }

    private void registerConfigurationsAsComponents() throws Exception
    {
        for (Class<?> clazz : Arrays.<Class<?>> asList(
            MayocatShopConfiguration.class,
            DataSourceConfiguration.class,
            MultitenancySettings.class,
            SecuritySettings.class
        )) {
            DefaultComponentDescriptor cd =
                new DefaultComponentDescriptor();
            cd.setRoleType(clazz);
            try {
                componentManager.registerComponent(cd, clazz.newInstance());
            } catch (InstantiationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}
