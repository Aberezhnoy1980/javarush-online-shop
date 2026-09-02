-- liquibase formatted sql

-- changeset aberezhnoy:012-index-order-items-product-id
-- comment Analytics joins order_items to products; PostgreSQL does not index FK columns automatically.
CREATE INDEX idx_order_items_product_id ON order_items (product_id);
