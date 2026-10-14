import { useEffect, useState } from 'react';
import { inventoryApi } from '../../services/api';
import type { Order } from '../../types';

export const money = (value: number) => `$${value.toLocaleString('es-CO')}`;

// "T00:00" keeps the date in local time; a bare "2026-10-16" would be read as UTC and show the day before
export const day = (date: string) => new Date(`${date}T00:00`).toLocaleDateString();

// Same value as simulation.unpaid-order-hours in the backend's application.properties
export const UNPAID_ORDER_HOURS = 24;

/** When an unpaid order is cancelled automatically, as "15/10/2026, 3:40 p. m.". */
export function paymentDeadline(order: Order) {
  const deadline = new Date(new Date(order.createdAt).getTime() + UNPAID_ORDER_HOURS * 3600 * 1000);
  return deadline.toLocaleString('es-CO', { dateStyle: 'short', timeStyle: 'short' });
}

export interface StockInfo {
  available: number;
  // The minimum the store wants to keep, set by the admin in Inventory
  minimum: number;
}

/** Few units left: at or below the minimum the store wants to keep. */
export const isLow = (info?: StockInfo) => Boolean(info && info.available > 0 && info.available <= info.minimum);

/** Stock of each product id; a product without inventory counts as sold out. */
export function useStock(productIds: number[]) {
  const [stock, setStock] = useState<Record<number, StockInfo>>({});
  const key = productIds.join(',');

  useEffect(() => {
    if (!key) return;
    Promise.all(productIds.map(id =>
      inventoryApi.getByProductId(id)
        .then(inventory => [id, { available: inventory.availableStock, minimum: inventory.minimumStock }] as const)
        .catch(() => [id, { available: 0, minimum: 0 }] as const)
    )).then(entries => setStock(Object.fromEntries(entries)));
    // productIds is rebuilt on every render; key is its stable version
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [key]);

  return stock;
}
