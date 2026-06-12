export interface Category {
  id: number;
  name: string;
}

export interface Customer {
  id: number;
  name: string;
  email: string;
  status: 'ACTIVE' | 'INACTIVE';
}

export interface Address {
  id: number;
  street: string;
  city: string;
  country: string;
  customerId: number;
  customerName?: string;
  addressLine?: string;
}

export interface Product {
  id: number;
  name: string;
  sku: string;
  price: number;
  active: boolean;
  categoryId: number;
  categoryName?: string;
}

export interface Inventory {
  id: number;
  availableStock: number;
  minimumStock: number;
  productId: number;
  productName?: string;
}

export interface OrderItem {
  id: number;
  productId: number;
  productName?: string;
  quantity: number;
  unitPrice: number;
  subtotal?: number;
}

export interface Order {
  id: number;
  status: OrderStatus;
  createdAt: string;
  customerId: number;
  customerName?: string;
  addressId: number;
  addressLine?: string;
  items: OrderItem[];
  total?: number;
}

export type OrderStatus = 'CREATED' | 'PAID' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED' | 'RETURNED';

export interface OrderStatusHistory {
  id: number;
  orderId: number;
  status: OrderStatus;
  changedAt: string;
  reason?: string;
}

export interface CreateCategoryRequest {
  name: string;
}

export interface CreateCustomerRequest {
  name: string;
  email: string;
}

export interface UpdateCustomerRequest {
  name: string;
  email: string;
  status: 'ACTIVE' | 'INACTIVE';
}

export interface CreateAddressRequest {
  street: string;
  city: string;
  country: string;
  customerId: number;
}

export interface CreateProductRequest {
  name: string;
  sku: string;
  price: number;
  categoryId: number;
}

export interface UpdateProductRequest {
  name: string;
  sku: string;
  price: number;
  active: boolean;
  categoryId: number;
}

export interface CreateInventoryRequest {
  availableStock: number;
  minimumStock: number;
  productId: number;
}

export interface UpdateInventoryRequest {
  availableStock: number;
  minimumStock: number;
}

export interface CreateOrderRequest {
  customerId: number;
  addressId: number;
  items: CreateOrderItemRequest[];
}

export interface CreateOrderItemRequest {
  productId: number;
  quantity: number;
}

export interface PageResponse<T> {
  content: T[];
  empty: boolean;
  first: boolean;
  last: boolean;
  number: number;
  numberOfElements: number;
  pageable: {
    offset: number;
    pageNumber: number;
    pageSize: number;
    paged: boolean;
    sort: {
      empty: boolean;
      sorted: boolean;
      unsorted: boolean;
    };
    unpaged: boolean;
  };
  size: number;
  sort: {
    empty: boolean;
    sorted: boolean;
    unsorted: boolean;
  };
  totalElements: number;
  totalPages: number;
}