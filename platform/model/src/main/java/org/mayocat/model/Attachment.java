package org.mayocat.model;

import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.mayocat.model.annotation.LocalizedField;

import com.google.common.base.Objects;

/**
 * @version $Id$
 */
public class Attachment implements Entity, Child, Localized
{
    private UUID id;

    private String slug;

    @LocalizedField
    private String title;

    @LocalizedField
    private String description;

    private String extension;

    private transient InputStream data;

    private UUID parentId;

    private Map<Locale, Map<String, Object>> localizedVersions;

    public Attachment()
    {
    }

    @Override
    public String getSlug()
    {
        return this.slug;
    }

    @Override
    public void setSlug(String slug)
    {
        this.slug = slug;
    }

    @Override
    public UUID getId()
    {
        return this.id;
    }

    @Override
    public void setId(UUID id)
    {
        this.id = id;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getExtension()
    {
        return extension;
    }

    public void setExtension(String extension)
    {
        this.extension = extension;
    }

    public InputStream getData()
    {
        return data;
    }

    public void setData(InputStream data)
    {
        this.data = data;
    }

    @Override
    public UUID getParentId()
    {
        return this.parentId;
    }

    public void setParentId(UUID parentId)
    {
        this.parentId = parentId;
    }

    @Override
    public Map<Locale, Map<String, Object>> getLocalizedVersions()
    {
        return localizedVersions;
    }

    public void setLocalizedVersions(Map<Locale, Map<String, Object>> versions)
    {
        this.localizedVersions = versions;
    }

    ////////////////////////////////////////////////

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Attachment other = (Attachment) obj;

        return     Objects.equal(this.id, other.id)
                && Objects.equal(this.title, other.title)
                && Objects.equal(this.slug, other.slug)
                && Objects.equal(this.extension, other.extension)
                && Objects.equal(this.description, other.description)
                && Objects.equal(this.parentId, other.parentId)
                && Objects.equal(this.data, other.data);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(
                this.id,
                this.slug,
                this.title,
                this.extension,
                this.description,
                this.parentId,
                this.data
        );
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this)
                .addValue(this.title)
                .addValue(this.slug)
                .toString();
    }
}
