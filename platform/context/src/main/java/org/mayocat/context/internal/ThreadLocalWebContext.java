package org.mayocat.context.internal;

import java.io.Serializable;
import java.util.Locale;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import org.mayocat.accounts.model.Tenant;
import org.mayocat.accounts.model.User;
import org.mayocat.configuration.ExposedSettings;
import org.mayocat.context.scope.Flash;
import org.mayocat.context.scope.Session;
import org.mayocat.theme.Theme;
import org.xwiki.component.annotation.Component;

@Component
@Singleton
public class ThreadLocalWebContext implements org.mayocat.context.WebContext
{
    private ThreadLocal<org.mayocat.context.WebContext> context = new ThreadLocal<org.mayocat.context.WebContext>();

    public org.mayocat.context.WebContext getContext()
    {
        return this.context.get();
    }

    public void setContext(DefaultWebContext context)
    {
        this.context.set(context);
    }

    // -----------------------------------------------------------------------------------------------------------------

    // Wrapped methods

    @Override
    public Tenant getTenant()
    {
        return getContext().getTenant();
    }

    @Override
    public User getUser()
    {
        return getContext().getUser();
    }

    @Override
    public void setUser(User user)
    {
        getContext().setUser(user);
    }

    @Override
    public void setTenant(Tenant tenant)
    {
        getContext().setTenant(tenant);
    }

    @Override
    public Theme getTheme()
    {
        return getContext().getTheme();
    }

    @Override
    public void setTheme(Theme theme)
    {
        getContext().setTheme(theme);
    }

    @Override
    public void setSettings(Map<Class, Object> settings)
    {
        getContext().setSettings(settings);
    }

    @Override
    public <T extends ExposedSettings> T getSettings(Class<T> c)
    {
        return getContext().getSettings(c);
    }

    @Override
    public Locale getLocale()
    {
        return getContext().getLocale();
    }

    @Override
    public void setLocale(Locale locale)
    {
        getContext().setLocale(locale);
    }

    @Override
    public boolean isAlternativeLocale()
    {
        return getContext().isAlternativeLocale();
    }

    @Override
    public void setAlternativeLocale(boolean alternativeLocale)
    {
        getContext().setAlternativeLocale(alternativeLocale);
    }

    @Override
    public Session getSession()
    {
        return getContext().getSession();
    }

    @Override
    public void setSession(Session session)
    {
        getContext().setSession(session);
    }

    @Override
    public void setFlash(Flash flash)
    {
        getContext().setFlash(flash);
    }

    @Override
    public Flash getFlash()
    {
        return getContext().getFlash();
    }

    @Override
    public void flash(String name, Serializable value)
    {
        getContext().flash(name, value);
    }

    @Override
    public void session(String name, Serializable value)
    {
        getContext().session(name, value);
    }
}
