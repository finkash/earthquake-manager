import { describe, expect, it, vi } from 'vitest';
import { loadEarthquakes, type EarthquakeDto } from './earthquake-loader';

describe('loadEarthquakes', () => {
	it('returns earthquakes when the first backend candidate succeeds', async () => {
		const payload: EarthquakeDto[] = [
			{ id: 1, magnitude: 3.4, place: 'Sofia', time: '2026-01-01T12:00:00Z', longitude: 23.32, latitude: 42.7 }
		];
		const fetchFn = vi.fn(async () => new Response(JSON.stringify(payload), { status: 200 }));

		const result = await loadEarthquakes(fetchFn, ['http://localhost:8080']);

		// Must stop after the 1 try.
		expect(fetchFn).toHaveBeenCalledTimes(1);

		//No error message should exist and the earthquakes should be returned.
		expect(result.loadError).toBe('');
		expect(result.earthquakes).toEqual(payload);
	});

	it('falls back to the next backend candidate when the first fails', async () => {
		const payload: EarthquakeDto[] = [
			{ id: 2, magnitude: 4.1, place: 'Plovdiv', time: '2026-01-01T13:00:00Z', longitude: 24.75, latitude: 42.14 }
		];
		const fetchFn = vi.fn(async (url: string) => {
			if (url.includes('localhost')) {
				throw new Error('connect ECONNREFUSED'); // Simulated network cras
			}
			return new Response(JSON.stringify(payload), { status: 200 });
		});

		const result = await loadEarthquakes(fetchFn, ['http://localhost:8080', 'http://127.0.0.1:8080']);

		// Must try both candidates.
		expect(fetchFn).toHaveBeenCalledTimes(2);

		// No error message should exist becase the second candidate succeeded
		expect(result.loadError).toBe('');
		expect(result.earthquakes).toEqual(payload);
	});

	it('returns a load error and empty earthquakes when all candidates fail', async () => {
		
		// This fetch always returns 502.
		const fetchFn = vi.fn(async () => new Response('Bad gateway', { status: 502 }));

		//Try two different URLs, both of which will fail.
		const result = await loadEarthquakes(fetchFn, ['http://localhost:8080', 'http://127.0.0.1:8080']);

		//No data available, so it should try all candidates before giving up.
		expect(fetchFn).toHaveBeenCalledTimes(2);
		expect(result.earthquakes).toEqual([]);
		expect(result.loadError).toBe('Failed to load earthquakes (502)');
	});
});
