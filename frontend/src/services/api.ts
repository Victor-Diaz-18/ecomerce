const API_BASE = '/api/v1';

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const headers: HeadersInit = {
    'Content-Type': 'application/json',
    ...options.headers,
  };

  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers,
  });

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(`${response.status}: ${errorText}`);
  }

  if (response.status === 204) return null as T;

  return response.json();
}

export const categoryApi = {
  getAll: () => request<Category[]>('/categories'),
  create: (data: CreateCategoryRequest) => request<Category>('/categories', { method: 'POST', body: JSON.stringify(data) }),
  update: (id: number, data: Partial<CreateCategoryRequest>) => request<Category>(`/categories/${id}`, { method: 'PATCH', body: JSON.stringify(data) }),
  delete: (id: number) => request<void>(`/categories/${id}`, { method: 'DELETE' }),
};

export const customerApi = {
  getAll: () => request<Customer[]>('/customers'),
  create: (data: CreateCustomerRequest) => request<Customer>('/customers', { method: 'POST', body: JSON.stringify(data) }),
  update: (id: number, data: UpdateCustomerRequest) => request<Customer>(`/customers/${id}`, { method: 'PATCH', body: JSON.stringify(data) }),
  delete: (id: number) => request<void>(`/customers/${id}`, { method: 'DELETE' }),
  getAddresses: (customerId: number) => request<Address[]>(`/addresses/customer/${customerId}`),
};

export const addressApi = {
  create: (data: CreateAddressRequest) => request<Address>('/addresses', { method: 'POST', body: JSON.stringify(data) }),
  delete: (id: number) => request<void>(`/addresses/${id}`, { method: 'DELETE' }),
};

export const productApi = {
  getAll: () => request<PageResponse<Product>>('/products'),
  getAllList: () => request<Product[]>('/products/all'),
  getById: (id: number) => request<Product>(`/products/${id}`),
  create: (data: CreateProductRequest) => request<Product>('/products', { method: 'POST', body: JSON.stringify(data) }),
  update: (id: number, data: UpdateProductRequest) => request<Product>(`/products/${id}`, { method: 'PATCH', body: JSON.stringify(data) }),
  delete: (id: number) => request<void>(`/products/${id}`, { method: 'DELETE' }),
  getByCategory: (categoryId: number) => request<Product[]>(`/products/category/${categoryId}`),
};

export const inventoryApi = {
  getAll: () => request<Inventory[]>('/inventories'),
  getById: (id: number) => request<Inventory>(`/inventories/${id}`),
  getByProductId: (productId: number) => request<Inventory>(`/inventories/product/${productId}`),
  create: (data: CreateInventoryRequest) => request<Inventory>('/inventories', { method: 'POST', body: JSON.stringify(data) }),
  update: (id: number, data: UpdateInventoryRequest) => request<Inventory>(`/inventories/${id}`, { method: 'PATCH', body: JSON.stringify(data) }),
  lowStock: () => request<Inventory[]>('/inventories/low-stock'),
};

export const orderApi = {
  getAll: () => request<Order[]>('/orders'),
  search: (customerId?: number, status?: OrderStatus) => {
    const params = new URLSearchParams();
    if (customerId) params.set('customerId', customerId.toString());
    if (status) params.set('status', status);
    return request<Order[]>(`/orders/search?${params.toString()}`);
  },
  getById: (id: number) => request<Order>(`/orders/${id}`),
  create: (data: CreateOrderRequest) => request<Order>('/orders', { method: 'POST', body: JSON.stringify(data) }),
  pay: (id: number) => request<Order>(`/orders/${id}/pay`, { method: 'PATCH' }),
  ship: (id: number) => request<Order>(`/orders/${id}/ship`, { method: 'PATCH' }),
  deliver: (id: number) => request<Order>(`/orders/${id}/deliver`, { method: 'PATCH' }),
  cancel: (id: number) => request<Order>(`/orders/${id}/cancel`, { method: 'PATCH' }),
  return: (id: number, reason?: string) => request<Order>(`/orders/${id}/return`, { method: 'PATCH', body: JSON.stringify({ reason }) }),
  getHistory: (id: number) => request<OrderStatusHistory[]>(`/orders/${id}/history`),
  delete: (id: number) => request<void>(`/orders/${id}`, { method: 'DELETE' }),
};