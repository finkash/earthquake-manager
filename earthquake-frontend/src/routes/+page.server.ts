import type { PageServerLoad } from './$types';

type EarthquakeDto = {
	id: number;
	magnitude: number | null;
	place: string | null;
	time: string | null;
	longitude: number | null;
	latitude: number | null;
};

const BACKEND_CANDIDATES = [
	process.env.BACKEND_URL,
	'http://localhost:8080',
	'http://127.0.0.1:8080'
].filter(Boolean) as string[];

export const load: PageServerLoad = async ({ fetch }) => {
	try {
		let earthquakes: EarthquakeDto[] | null = null;
		let lastError = 'Unable to load earthquakes.';

		for (const backendUrl of BACKEND_CANDIDATES) {
			try {
				const response = await fetch(`${backendUrl}/api/earthquakes`);
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
			throw new Error(lastError);
		}

		return { earthquakes, loadError: '' };
	} catch (error) {
		return {
			earthquakes: [] as EarthquakeDto[],
			loadError: error instanceof Error ? error.message : 'Unable to load earthquakes.'
		};
	}
};
