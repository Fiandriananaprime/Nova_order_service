-- ============================================================
-- NovaMarket - Order Service
-- PostgreSQL migration
-- ============================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;


-- ============================================================
-- ENUMS
-- ============================================================

CREATE TYPE order_status AS ENUM (
    'pending',
    'confirmed',
    'processing',
    'preparing',
    'delivered',
    'cancelled'
);

CREATE TYPE delivery_method AS ENUM (
    'standard',
    'express',
    'pickup'
);

CREATE TYPE order_payment_method AS ENUM (
    'mvola',
    'orange_money',
    'card',
    'cod'
);

CREATE TYPE payment_status AS ENUM (
    'pending',
    'paid',
    'failed',
    'refunded',
    'partially_refunded'
);


-- ============================================================
-- CARTS
-- ============================================================

CREATE TABLE carts (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Owned by User/Auth service.
    -- No foreign key because this is a microservice boundary.
                       buyer_id UUID NOT NULL UNIQUE,

                       created_at TIMESTAMPTZ(6) NOT NULL DEFAULT NOW(),
                       updated_at TIMESTAMPTZ(6) NOT NULL DEFAULT NOW()
);


-- ============================================================
-- CART ITEMS
-- ============================================================

CREATE TABLE cart_items (
                            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                            cart_id UUID NOT NULL,

    -- Owned by Product service.
    -- No foreign key.
                            product_id UUID NOT NULL,

                            qty INTEGER NOT NULL,

                            CONSTRAINT chk_cart_items_qty_positive
                                CHECK (qty > 0),

                            CONSTRAINT fk_cart_items_cart
                                FOREIGN KEY (cart_id)
                                    REFERENCES carts(id)
                                    ON DELETE CASCADE,

                            CONSTRAINT uq_cart_items_cart_product
                                UNIQUE (cart_id, product_id)
);

CREATE INDEX idx_cart_items_product
    ON cart_items(product_id);


-- ============================================================
-- ORDERS
-- ============================================================

CREATE TABLE orders (
                        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Owned by User/Auth service.
                        buyer_id UUID NOT NULL,

                        subtotal NUMERIC(19, 4) NOT NULL,

                        delivery_fee NUMERIC(19, 4) NOT NULL DEFAULT 0,

                        total NUMERIC(19, 4) NOT NULL,

                        status order_status NOT NULL DEFAULT 'pending',

                        delivery_method delivery_method NOT NULL,

                        payment_method order_payment_method NOT NULL,

                        payment_status payment_status NOT NULL DEFAULT 'pending',

                        estimated_delivery TIMESTAMPTZ(6),

                        note TEXT,

                        cancel_reason TEXT,

    -- Owned by Delivery service.
    -- No foreign key.
                        delivery_id UUID,

                        discount_total NUMERIC(19, 4) NOT NULL DEFAULT 0,

                        created_at TIMESTAMPTZ(6) NOT NULL DEFAULT NOW(),

                        updated_at TIMESTAMPTZ(6) NOT NULL DEFAULT NOW(),

                        CONSTRAINT chk_orders_subtotal_positive
                            CHECK (subtotal >= 0),

                        CONSTRAINT chk_orders_delivery_fee_positive
                            CHECK (delivery_fee >= 0),

                        CONSTRAINT chk_orders_total_positive
                            CHECK (total >= 0),

                        CONSTRAINT chk_orders_discount_positive
                            CHECK (discount_total >= 0)
);


-- ============================================================
-- ORDER INDEXES
-- ============================================================

CREATE INDEX idx_orders_buyer_status_date
    ON orders (
               buyer_id,
               status,
               created_at DESC
        );

CREATE INDEX idx_orders_status_date
    ON orders (
               status,
               created_at DESC
        );

CREATE INDEX idx_orders_delivery_id
    ON orders(delivery_id);


-- ============================================================
-- ORDER ADDRESSES
-- ============================================================

CREATE TABLE order_addresses (
                                 order_id UUID PRIMARY KEY,

                                 label TEXT,

                                 recipient_name TEXT NOT NULL,

                                 phone TEXT NOT NULL,

                                 street TEXT NOT NULL,

                                 district TEXT NOT NULL,

                                 city TEXT NOT NULL,

                                 region TEXT NOT NULL,

                                 postal_code TEXT,

                                 latitude DOUBLE PRECISION,

                                 longitude DOUBLE PRECISION,

                                 instructions TEXT,

                                 created_at TIMESTAMPTZ(6) NOT NULL DEFAULT NOW(),

                                 CONSTRAINT fk_order_addresses_order
                                     FOREIGN KEY (order_id)
                                         REFERENCES orders(id)
                                         ON DELETE CASCADE
);


-- ============================================================
-- ORDER ITEMS
-- ============================================================

CREATE TABLE order_items (
                             id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                             order_id UUID NOT NULL,

    -- Owned by Product service.
    -- Nullable intentionally for historical orders.
                             product_id UUID,

    -- Snapshot of product information at checkout.
                             product_name TEXT NOT NULL,

                             image TEXT,

                             price NUMERIC(19, 4) NOT NULL,

                             qty INTEGER NOT NULL,

    -- Owned by User/Seller service.
                             seller_id UUID NOT NULL,

    -- Snapshot of seller information at checkout.
                             seller_name TEXT NOT NULL,

                             CONSTRAINT fk_order_items_order
                                 FOREIGN KEY (order_id)
                                     REFERENCES orders(id)
                                     ON DELETE CASCADE,

                             CONSTRAINT chk_order_items_price_positive
                                 CHECK (price >= 0),

                             CONSTRAINT chk_order_items_qty_positive
                                 CHECK (qty > 0)
);


-- ============================================================
-- ORDER ITEM INDEXES
-- ============================================================

CREATE INDEX idx_order_items_order
    ON order_items(order_id);

CREATE INDEX idx_order_items_product
    ON order_items(product_id);

CREATE INDEX idx_order_items_seller
    ON order_items(seller_id, order_id);


-- ============================================================
-- ORDER STATUS HISTORY
-- ============================================================

CREATE TABLE order_status_history (
                                      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                                      order_id UUID NOT NULL,

                                      old_status order_status,

                                      new_status order_status NOT NULL,

    -- Owned by User/Auth service.
                                      actor_id UUID,

                                      tracking TEXT,

                                      created_at TIMESTAMPTZ(6) NOT NULL DEFAULT NOW(),

                                      CONSTRAINT fk_order_status_history_order
                                          FOREIGN KEY (order_id)
                                              REFERENCES orders(id)
                                              ON DELETE CASCADE
);


-- ============================================================
-- ORDER STATUS HISTORY INDEX
-- ============================================================

CREATE INDEX idx_order_status_history_order
    ON order_status_history(
                            order_id,
                            created_at DESC
        );


-- ============================================================
-- UPDATED_AT TRIGGER
-- ============================================================

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    NEW.updated_at = NOW();
RETURN NEW;
END;
$$;


CREATE TRIGGER trg_carts_updated_at
    BEFORE UPDATE ON carts
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();


CREATE TRIGGER trg_orders_updated_at
    BEFORE UPDATE ON orders
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();