package org.mayocat.attachment.url;

import java.net.MalformedURLException;
import java.net.URL;

import org.mayocat.accounts.model.Tenant;
import org.mayocat.attachment.util.AttachmentUtils;
import org.mayocat.model.Attachment;
import org.mayocat.url.AbstractEntityURLFactory;
import org.mayocat.url.EntityURLFactory;
import org.mayocat.url.URLType;
import org.xwiki.component.annotation.Component;

/**
 * @version $Id$
 */
@Component
public class AttachmentURLFactory extends AbstractEntityURLFactory<Attachment> implements EntityURLFactory<Attachment>
{
    @Override
    public URL create(Attachment entity, Tenant tenant, URLType type)
    {
        String fileName = entity.getSlug() + "." + entity.getExtension();
        if (!AttachmentUtils.isImage(fileName)) {
            throw new UnsupportedOperationException("Not implemented");
        }
        try {
            return new URL(getSchemeAndDomain(tenant) + "/images/" + fileName);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public URL create(Attachment entity, Tenant tenant)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public URL create(Attachment entity)
    {
        throw new UnsupportedOperationException("Not implemented");
    }
}
