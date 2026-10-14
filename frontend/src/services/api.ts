import type {
  Category, Customer, Address, Department, Product, Inventory, Order, OrderStatus, OrderStatusHistory,
  CreateCategoryRequest, CreateCustomerRequest, UpdateCustomerRequest, CreateAddressRequest, UpdateAddressRequest,
  CreateProductRequest, UpdateProductRequest, CreateInventoryRequest, UpdateInventoryRequest,
  CreateOrderRequest, PageResponse, MonthlyIncomeResponse, BestSellingProductResponse,
  TopCustomerResponse, IncomeByStatusResponse, Session,
} from '../types';
import { loadSession, saveSession } from './session';

const API_BASE = '/api/v1';

// The server answers errors as JSON with a "message"; show that instead of the raw body
function errorMessage(status: number, body: string): string {
  try {
    const message = JSON.parse(body).message;
    if (typeof message === 'string' && message) return message;
  } catch {
    // not JSON: fall through
  }
  return `${status}: ${body}`;
}

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const session = loadSession();
  const headers: HeadersInit = {
    'Content-Type': 'application/json',
    ...(session ? { Authorization: `Bearer ${session.token}` } : {}),
    ...options.headers,
  };

  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers,
  });

  if (!response.ok) {
    // The saved login expired or is no longer valid: forget it, which sends the user to the login page
    if (response.status === 401 && session && !path.startsWith('/auth/')) saveSession(null);
    throw new Error(errorMessage(response.status, await response.text()));
  }

  if (response.status === 204) return null as T;

  return response.json();
}

export const authApi = {
  login: (data: { email: string; password: string }) =>
    request<Session>('/auth/login', { method: 'POST', body: JSON.stringify(data) }),
  register: (data: { name: string; email: string; password: string }) =>
    request<Session>('/auth/register', { method: 'POST', body: JSON.stringify(data) }),
};

// The customer portal: everything refers to the customer who is logged in
export const accountApi = {
  updateProfile: (name: string) => request<Customer>('/me', { method: 'PUT', body: JSON.stringify({ name }) }),
  changePassword: (currentPassword: string, newPassword: string) =>
    request<void>('/me/password', { method: 'PATCH', body: JSON.stringify({ currentPassword, newPassword }) }),
  getAddresses: () => request<Address[]>('/me/addresses'),
  addAddress: (data: UpdateAddressRequest) => request<Address>('/me/addresses', { method: 'POST', body: JSON.stringify(data) }),
  updateAddress: (id: number, data: UpdateAddressRequest) => request<Address>(`/me/addresses/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  getOrders: () => request<Order[]>('/me/orders'),
  createOrder: (data: { addressId: number; items: { productId: number; quantity: number }[] }) =>
    request<Order>('/me/orders', { method: 'POST', body: JSON.stringify(data) }),
  getOrder: (id: number) => request<Order>(`/me/orders/${id}`),
  buyNow: (data: { addressId: number; productId: number; quantity: number }) =>
    request<Order>('/me/orders/buy-now', { method: 'POST', body: JSON.stringify(data) }),
  pay: (id: number) => request<Order>(`/me/orders/${id}/pay`, { method: 'PATCH' }),
  cancel: (id: number) => request<Order>(`/me/orders/${id}/cancel`, { method: 'PATCH' }),
  return: (id: number, reason: string) => request<Order>(`/me/orders/${id}/return`, { method: 'PATCH', body: JSON.stringify({ reason }) }),
  changeAddress: (id: number, addressId: number) => request<Order>(`/me/orders/${id}/address`, { method: 'PATCH', body: JSON.stringify({ addressId }) }),
};

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
  update: (id: number, data: UpdateAddressRequest) => request<Address>(`/addresses/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  delete: (id: number) => request<void>(`/addresses/${id}`, { method: 'DELETE' }),
};

export const locationApi = {
  getAll: () => request<Department[]>('/locations'),
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
  changeAddress: (id: number, addressId: number) => request<Order>(`/orders/${id}/address`, { method: 'PATCH', body: JSON.stringify({ addressId }) }),
  return: (id: number, reason?: string) => request<Order>(`/orders/${id}/return`, { method: 'PATCH', body: JSON.stringify({ reason }) }),
  confirmReturn: (id: number) => request<Order>(`/orders/${id}/return/confirm`, { method: 'PATCH' }),
  rejectReturn: (id: number, reason?: string) => request<Order>(`/orders/${id}/return/reject`, { method: 'PATCH', body: JSON.stringify({ reason }) }),
  getHistory: (id: number) => request<OrderStatusHistory[]>(`/orders/${id}/history`),
  delete: (id: number) => request<void>(`/orders/${id}`, { method: 'DELETE' }),
};

export const dashboardApi = {
  getMonthlyIncome: () => request<MonthlyIncomeResponse[]>('/orders/stats/monthly-income'),
  getBestSellingProducts: () => request<BestSellingProductResponse[]>('/orders/stats/best-selling'),
  getTopCustomers: () => request<TopCustomerResponse[]>('/orders/stats/top-customers'),
  getIncomeByStatus: () => request<IncomeByStatusResponse[]>('/orders/stats/income-by-status'),
};