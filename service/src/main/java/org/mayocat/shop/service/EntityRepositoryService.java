package org.mayocat.shop.service;

import java.util.List;

import javax.validation.Valid;

import org.mayocat.shop.model.Entity;
import org.mayocat.shop.store.EntityAlreadyExistsException;
import org.mayocat.shop.store.InvalidEntityException;
import org.mayocat.shop.store.StoreException;

public interface EntityRepositoryService<T extends Entity>
{
    void create(@Valid T entity) throws InvalidEntityException, EntityAlreadyExistsException, StoreException;
    
    void update(@Valid T entity) throws InvalidEntityException, StoreException;
    
    List<T> findAll(int number, int offset) throws StoreException;
}
