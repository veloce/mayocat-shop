package org.mayocat.shop.catalog.front.context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;

import org.mayocat.accounts.model.Tenant;
import org.mayocat.addons.front.builder.AddonContextBuilder;
import org.mayocat.addons.model.AddonGroup;
import org.mayocat.cms.pages.front.builder.PageContextBuilder;
import org.mayocat.cms.pages.model.Page;
import org.mayocat.cms.pages.store.PageStore;
import org.mayocat.configuration.ConfigurationService;
import org.mayocat.configuration.PlatformSettings;
import org.mayocat.configuration.general.GeneralSettings;
import org.mayocat.context.WebContext;
import org.mayocat.image.model.Image;
import org.mayocat.image.model.Thumbnail;
import org.mayocat.image.store.ThumbnailStore;
import org.mayocat.localization.EntityLocalizationService;
import org.mayocat.model.Attachment;
import org.mayocat.context.scope.Flash;
import org.mayocat.shop.catalog.front.builder.ProductContextBuilder;
import org.mayocat.shop.catalog.model.Collection;
import org.mayocat.shop.catalog.model.Product;
import org.mayocat.shop.catalog.store.CollectionStore;
import org.mayocat.shop.catalog.store.ProductStore;
import org.mayocat.shop.front.FrontContextSupplier;
import org.mayocat.shop.front.annotation.FrontContext;
import org.mayocat.shop.front.annotation.FrontContextContributor;
import org.mayocat.shop.front.builder.ImageContextBuilder;
import org.mayocat.shop.front.context.ContextConstants;
import org.mayocat.shop.front.resources.AbstractFrontResource;
import org.mayocat.shop.front.resources.ResourceResource;
import org.mayocat.store.AttachmentStore;
import org.mayocat.theme.ThemeDefinition;
import org.mayocat.url.EntityURLFactory;
import org.xwiki.component.annotation.Component;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @version $Id$
 */
@Component("root")
public class RootContextSupplier implements FrontContextSupplier, ContextConstants
{
    public final static String SITE = "site";

    public final static String SITE_TITLE = "title";

    public static final String THEME_PATH = "themePath";

    @Inject
    private WebContext context;

    @Inject
    private PlatformSettings platformSettings;

    @Inject
    private ConfigurationService configurationService;

    @Inject
    private Provider<PageStore> pageStore;

    @Inject
    private Provider<ProductStore> productStore;

    @Inject
    private Provider<AttachmentStore> attachmentStore;

    @Inject
    private Provider<ThumbnailStore> thumbnailStore;

    @Inject
    private Provider<CollectionStore> collectionStore;

    @Inject
    private EntityURLFactory urlFactory;

    @Inject
    private EntityLocalizationService entityLocalizationService;

    @FrontContextContributor(path = "/")
    public void contributeRootContext(@FrontContext Map data)
    {
        final GeneralSettings config = context.getSettings(GeneralSettings.class);
        Tenant tenant = context.getTenant();
        ThemeDefinition theme = context.getTheme().getDefinition();

        Map site = Maps.newHashMap();
        site.put(SITE_TITLE, tenant.getName());
        ImageContextBuilder imageContextBuilder = new ImageContextBuilder(theme);

        List<Attachment> siteAttachments = this.attachmentStore.get().findAllChildrenOf(tenant);
        List<Image> siteImages = new ArrayList<Image>();
        for (Attachment attachment : siteAttachments) {
            if (AbstractFrontResource.isImage(attachment)) {
                if (attachment.getId().equals(tenant.getFeaturedImageId())) {
                    List<Thumbnail> thumbnails = thumbnailStore.get().findAll(attachment);
                    Image image = new Image(attachment, thumbnails);
                    siteImages.add(image);
                    site.put("logo", imageContextBuilder.createImageContext(image, true));
                }
            }
        }

        if (tenant.getAddons().isLoaded()) {
            AddonContextBuilder addonContextBuilder = new AddonContextBuilder();
            Map<String, AddonGroup> platformAddons = platformSettings.getAddons();
            site.put("platform_addons", addonContextBuilder.build(platformAddons, tenant.getAddons().get(), "platform"));
        }

        data.put(THEME_PATH, ResourceResource.PATH);
        data.put(SITE, site);

        List<Collection> collections = this.collectionStore.get().findAll(24, 0);
        final List<Map<String, Object>> collectionsContext = Lists.newArrayList();

        for (final Collection collection : collections) {
            collectionsContext.add(new HashMap<String, Object>()
            {
                {
                    put(ContextConstants.URL, urlFactory.create(collection));
                    put("title", collection.getTitle());
                    put("description", collection.getDescription());
                }
            });
        }

        data.put(COLLECTIONS, new HashMap()
        {
            {
                put("all", collectionsContext);
            }
        });

        // Put page title and description, mainly for the home page, this will typically get overridden by sub-pages
        data.put(PAGE_TITLE, context.getTenant().getName());

        final List<Map<String, Object>> productsContext = Lists.newArrayList();
        List<Product> products = this.productStore.get().findAllOnShelf(120, 0);
        java.util.Collection<UUID> featuredImageIds = Collections2.transform(products,
                new Function<Product, UUID>()
                {
                    @Override
                    public UUID apply(final Product product)
                    {
                        return product.getFeaturedImageId();
                    }
                }
        );
        List<UUID> ids = new ArrayList<UUID>(Collections2.filter(featuredImageIds, Predicates.notNull()));
        List<Attachment> allImages;
        List<Thumbnail> allThumbnails;
        if (ids.isEmpty()) {
            allImages = Collections.emptyList();
            allThumbnails = Collections.emptyList();
        } else {
            allImages = this.attachmentStore.get().findByIds(ids);
            allThumbnails = this.thumbnailStore.get().findAllForIds(ids);
        }

        ProductContextBuilder builder = new ProductContextBuilder(urlFactory, configurationService,
                entityLocalizationService, attachmentStore.get(), thumbnailStore.get(), theme);

        for (final Product product : products) {
            java.util.Collection<Attachment> attachments = Collections2.filter(allImages, new Predicate<Attachment>()
            {
                @Override
                public boolean apply(@Nullable Attachment attachment)
                {
                    return attachment.getId().equals(product.getFeaturedImageId());
                }
            });
            List<Image> images = new ArrayList<Image>();
            for (final Attachment attachment : attachments) {
                java.util.Collection<Thumbnail> thumbnails =
                        Collections2.filter(allThumbnails, new Predicate<Thumbnail>()
                        {
                            @Override
                            public boolean apply(@Nullable Thumbnail thumbnail)
                            {
                                return thumbnail.getAttachmentId().equals(attachment.getId());
                            }
                        });
                Image image = new Image(entityLocalizationService.localize(attachment), new ArrayList<>(thumbnails));
                images.add(image);
            }
            List<org.mayocat.shop.catalog.model.Collection> productCollections =
                    collectionStore.get().findAllForProduct(product);
            product.setCollections(productCollections);
            if (collections.size() > 0) {
                // Here we take the first collection in the list, but in the future we should have the featured
                // collection as the parent entity of this product
                product.setFeaturedCollection(collections.get(0));
            }
            Map<String, Object> productContext = builder.build(entityLocalizationService.localize(product), images);
            productsContext.add(productContext);
        }

        data.put("products", new HashMap(){
            {
                put ("all",  productsContext);
            }
        });

        // Pages

        PageContextBuilder pageContextBuilder = new PageContextBuilder(urlFactory, theme);
        final List<Map<String, Object>> pagesContext = Lists.newArrayList();
        List<Page> rootPages = this.pageStore.get().findAllRootPages();

        featuredImageIds = Collections2.transform(rootPages,
                new Function<Page, UUID>()
                {
                    @Override
                    public UUID apply(final Page product)
                    {
                        return product.getFeaturedImageId();
                    }
                }
        );
        ids = new ArrayList<>(Collections2.filter(featuredImageIds, Predicates.notNull()));
        if (ids.isEmpty()) {
            allImages = Collections.emptyList();
            allThumbnails = Collections.emptyList();
        } else {
            allImages = this.attachmentStore.get().findByIds(ids);
            allThumbnails = this.thumbnailStore.get().findAllForIds(ids);
        }

        for (final Page page : rootPages) {
            java.util.Collection<Attachment> attachments = Collections2.filter(allImages, new Predicate<Attachment>()
            {
                @Override
                public boolean apply(@Nullable Attachment attachment)
                {
                    return attachment.getId().equals(page.getFeaturedImageId());
                }
            });
            List<Image> images = new ArrayList<Image>();
            for (final Attachment attachment : attachments) {
                java.util.Collection<Thumbnail> thumbnails =
                        Collections2.filter(allThumbnails, new Predicate<Thumbnail>()
                        {
                            @Override
                            public boolean apply(@Nullable Thumbnail thumbnail)
                            {
                                return thumbnail.getAttachmentId().equals(attachment.getId());
                            }
                        });
                Image image = new Image(attachment, new ArrayList<Thumbnail>(thumbnails));
                images.add(image);
            }
            Map<String, Object> pageContext = pageContextBuilder.build(page, images);
            pagesContext.add(pageContext);
        }

        data.put("pages", new HashMap(){
            {
                put ("roots",  pagesContext);
            }
        });

        Flash flash = context.getFlash();
        if (!flash.isEmpty()) {
            Map<String, Object> flashMap = new HashMap();
            for (String attribute : flash.getAttributeNames()) {
                flashMap.put(attribute, flash.getAttribute(attribute));
            }
            data.put("flash", flashMap);
        }
    }
}
