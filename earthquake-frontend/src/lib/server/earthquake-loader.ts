export type EarthquakeDto = {
	id: number;
	magnitude: number | null;
	place: string | null;
	time: string | null;
	longitude: number | null;
	latitude: number | null;
};

export type EarthquakeLoadResult = {
	earthquakes: EarthquakeDto[];
	loadError: string;
};

type FetchLike = (input: string, init?: RequestInit) => Promise<Response>;

export const DEFAULT_BACKEND_CANDIDATES = [
	process.env.BACKEND_URL,
	'http://localhost:8080',
	'http://127.0.0.1:8080'
].filter(Boolean) as string[];

export const loadEarthquakes = async (
	fetchFn: FetchLike,
	candidates: string[] = DEFAULT_BACKEND_CANDIDATES
): Promise<EarthquakeLoadResult> => {
	let earthquakes: EarthquakeDto[] | null = null;
	let lastError = 'Unable to load earthquakes.';

	for (const backendUrl of candidates) {
		try {
			const response = await fetchFn(`${backendUrl}/api/earthquakes`);
			if (!response.ok) {
				lastError = `Failed to load earthquakes (${response.status})`;
				continue;
			}

			earthquakes = (await response.json()) as EarthquakeDto[];
			break;
		} catch (error) {
			lastError = error instanceof Error ? error.message : 'Unable to load earthquakes.';
		}
	}

	if (!earthquakes) {
		return { earthquakes: [], loadError: lastError };
	}

	return { earthquakes, loadError: '' };
};
