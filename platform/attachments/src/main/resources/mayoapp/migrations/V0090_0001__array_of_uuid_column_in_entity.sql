/* images are now listed in the gallery column which is an array of uuid */
ALTER TABLE product ADD COLUMN gallery uuid[];
ALTER TABLE page ADD COLUMN gallery uuid[];
ALTER TABLE article ADD COLUMN gallery uuid[];
ALTER TABLE collection ADD COLUMN gallery uuid[];

/* migrate all attachments that are images and whose parent is in one of the tables above to the new gallery field */
UPDATE product SET gallery=array(SELECT entity_id FROM attachment JOIN entity ON attachment.entity_id = entity.id WHERE entity.parent_id = product.entity_id AND lower(extension) IN ('jpg', 'jpeg', 'gif', 'bmp', 'png', 'tiff'));
UPDATE page SET gallery=array(SELECT entity_id FROM attachment JOIN entity ON attachment.entity_id = entity.id WHERE entity.parent_id = page.entity_id AND lower(extension) IN ('jpg', 'jpeg', 'gif', 'bmp', 'png', 'tiff'));
UPDATE article SET gallery=array(SELECT entity_id FROM attachment JOIN entity ON attachment.entity_id = entity.id WHERE entity.parent_id = article.entity_id AND lower(extension) IN ('jpg', 'jpeg', 'gif', 'bmp', 'png', 'tiff'));
UPDATE collection SET gallery=array(SELECT entity_id FROM attachment JOIN entity ON attachment.entity_id = entity.id WHERE entity.parent_id = collection.entity_id AND lower(extension) IN ('jpg', 'jpeg', 'gif', 'bmp', 'png', 'tiff'));
