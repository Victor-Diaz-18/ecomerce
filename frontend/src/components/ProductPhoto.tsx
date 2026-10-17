import { useState } from 'react';
import { Package } from 'lucide-react';
import { productImageUrl } from '../services/api';
import type { Product } from '../types';

interface Props {
  product: Pick<Product, 'id' | 'name' | 'imageVersion'>;
  size?: 'card' | 'thumb';
}

/** The product's photo, or a neutral placeholder when it has none (or it fails to load). */
export function ProductPhoto({ product, size = 'card' }: Props) {
  const url = productImageUrl(product);
  // Remembers which address failed, so a new photo (new address) gets its chance
  const [failedUrl, setFailedUrl] = useState<string | null>(null);

  return (
    <div className={`product-photo ${size}`}>
      {url && failedUrl !== url
        ? <img src={url} alt={product.name} loading="lazy" onError={() => setFailedUrl(url)} />
        : <Package size={size === 'thumb' ? 20 : 40} strokeWidth={1.4} aria-hidden="true" />}
    </div>
  );
}
