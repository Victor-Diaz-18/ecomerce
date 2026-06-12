import { useState, useEffect, useCallback } from 'react';

export function useApi<T>(apiCall: () => Promise<T>, deps: unknown[] = []) {
  const [data, setData] = useState<T | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetch = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const result = await apiCall();
      setData(result);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error desconocido');
    } finally {
      setLoading(false);
    }
  }, [apiCall]);

  useEffect(() => {
    fetch();
  }, deps);

  return { data, loading, error, refetch: fetch };
}

export function useMutation<T, P>(mutationFn: (payload: P) => Promise<T>) {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const execute = async (payload: P): Promise<T | null> => {
    setLoading(true);
    setError(null);
    try {
      const result = await mutationFn(payload);
      return result;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error desconocido');
      return null;
    } finally {
      setLoading(false);
    }
  };

  return { execute, loading, error };
}