-- liquibase formatted sql

-- changeset aberezhnoy:009-add-indexes
CREATE INDEX idx_products_category_id ON products (category_id);
CREATE INDEX idx_products_brand_id ON products (brand_id);
CREATE INDEX idx_products_price ON products (price);
CREATE INDEX idx_orders_user_id_created_at ON orders (user_id, created_at);
CREATE INDEX idx_orders_status_created_at ON orders (status, created_at);
CREATE INDEX idx_order_items_order_id ON order_items (order_id);
CREATE INDEX idx_cart_items_cart_id ON cart_items (cart_id);
CREATE INDEX idx_reviews_product_id ON reviews (product_id);
