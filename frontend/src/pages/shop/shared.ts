import { useEffect, useState } from 'react';
import { inventoryApi } from '../../services/api';

export const money = (value: number) => `$${value.toLocaleString('es-CO')}`;

// "T00:00" keeps the date in local time; a bare "2026-10-16" would be read as UTC and show the day before
export const day = (date: string) => new Date(`${date}T00:00`).toLocaleDateString();

/** Units available for each product id; a product without inventory counts as sold out. */
export function useStock(productIds: number[]) {
  const [stock, setStock] = useState<Record<number, number>>({});
  const key = productIds.join(',');

  useEffect(() => {
    if (!key) return;
    Promise.all(productIds.map(id =>
      inventoryApi.getByProductId(id)
        .then(inventory => [id, inventory.availableStock] as const)
        .catch(() => [id, 0] as const)
    )).then(entries => setStock(Object.fromEntries(entries)));
    // productIds is rebuilt on every render; key is its stable version
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [key]);

  return stock;
}
