-- V1: Create all tables for University Store

CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP,
    version BIGINT DEFAULT 0
);

CREATE TABLE addresses (
    id BIGSERIAL PRIMARY KEY,
    street VARCHAR(150) NOT NULL,
    city VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL,
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP,
    version BIGINT DEFAULT 0
);

CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP,
    version BIGINT DEFAULT 0
);

CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    sku VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    price NUMERIC(19,2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    category_id BIGINT NOT NULL REFERENCES categories(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP,
    version BIGINT DEFAULT 0
);

CREATE TABLE inventories (
    id BIGSERIAL PRIMARY KEY,
    available_stock INTEGER NOT NULL,
    minimum_stock INTEGER NOT NULL,
    product_id BIGINT NOT NULL UNIQUE REFERENCES products(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP,
    version BIGINT DEFAULT 0
);

CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    total NUMERIC(19,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    address_id BIGINT NOT NULL REFERENCES addresses(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP,
    version BIGINT DEFAULT 0
);

CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(19,2) NOT NULL,
    subtotal NUMERIC(19,2) NOT NULL,
    order_id BIGINT NOT NULL REFERENCES orders(id),
    product_id BIGINT NOT NULL REFERENCES products(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP,
    version BIGINT DEFAULT 0
);

CREATE TABLE order_status_history (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(20) NOT NULL,
    changed_at TIMESTAMP NOT NULL,
    reason VARCHAR(500),
    order_id BIGINT NOT NULL REFERENCES orders(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP,
    version BIGINT DEFAULT 0
);

CREATE TABLE product_price_history (
    id BIGSERIAL PRIMARY KEY,
    old_price NUMERIC(19,2) NOT NULL,
    new_price NUMERIC(19,2) NOT NULL,
    changed_at TIMESTAMP NOT NULL,
    product_id BIGINT NOT NULL REFERENCES products(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP,
    version BIGINT DEFAULT 0
);
