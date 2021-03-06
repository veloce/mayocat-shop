package org.mayocat.configuration;

import java.util.Collections;
import java.util.Map;

import javax.validation.Valid;

import org.mayocat.addons.model.AddonGroup;
import org.mayocat.configuration.images.ImageFormatDefinition;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;

/**
 * @version $Id$
 */
public class PlatformSettings
{
    @Valid
    @JsonProperty
    private Map<String, ImageFormatDefinition> thumbnails = Maps.newHashMap();

    @Valid
    @JsonProperty
    private Map<String, AddonGroup> addons = Collections.emptyMap();

    public Map<String, ImageFormatDefinition> getThumbnails()
    {
        return thumbnails;
    }

    public Map<String, AddonGroup> getAddons()
    {
        return this.addons;
    }
}
