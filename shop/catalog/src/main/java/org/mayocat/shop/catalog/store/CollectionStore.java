package org.mayocat.shop.catalog.store;

import java.util.List;
import java.util.UUID;

import org.mayocat.shop.catalog.model.Collection;
import org.mayocat.model.EntityAndCount;
import org.mayocat.shop.catalog.model.Product;
import org.mayocat.store.EntityStore;
import org.mayocat.store.HasOrderedCollections;
import org.mayocat.store.InvalidMoveOperation;
import org.mayocat.store.Store;
import org.xwiki.component.annotation.Role;

@Role
public interface CollectionStore extends Store<Collection, UUID>, EntityStore, HasOrderedCollections
{
    Collection findBySlug(String slug);

    void addProduct(Collection collection, Product product);

    void removeProduct(Collection c, Product p);

    void moveCollection(String collectionToMove, String collectionToMoveRelativeTo, RelativePosition relativePosition)
            throws InvalidMoveOperation;

    void moveProductInCollection(Collection collection, String productToMove, String productToMoveRelativeTo,
            RelativePosition relativePosition) throws InvalidMoveOperation;

    List<EntityAndCount<Collection>> findAllWithProductCount();

    List<Collection> findAll();

    List<Collection> findAllForProduct(Product product);
}