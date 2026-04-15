/**'export type' defines a blueprint for an object.
 * The '| null' - Union Type - the data could be a number OR it could be empty.
 */
export type EarthquakeDto = {
	id: number;
	magnitude: number | null;
	place: string | null;
	time: string | null;
	longitude: number | null;
	latitude: number | null;
};

/**This defines what the loadEarthquakes function will return.
 * It ensures the frontend always gets an array (even if empty) and a string.
 */
export type EarthquakeLoadResult = {
	earthquakes: EarthquakeDto[];
	loadError: string;
};

/**FetchLike must be a function that takes a string and returns a Promise. 
 * The Promise resolves to a Response object. This allows us to use the real fetch or a mock version in tests.
 */
type FetchLike = (input: string, init?: RequestInit) => Promise<Response>;

/**Setting up fallback URLs.
 * .filter(Boolean) - if process.env.BACKEND_URL is undefined, 
 * it removes it from the list so the code doesn't try to fetch from "undefined".
 */
export const DEFAULT_BACKEND_CANDIDATES = [
	process.env.BACKEND_URL,
	'http://localhost:8080',
	'http://127.0.0.1:8080'
].filter(Boolean) as string[];


// The main function that tries to load earthquake data from the backend candidates.
export const loadEarthquakes = async (
	fetchFn: FetchLike,
	candidates: string[] = DEFAULT_BACKEND_CANDIDATES
): Promise<EarthquakeLoadResult> => {
	let earthquakes: EarthquakeDto[] | null = null;
	let lastError = 'Unable to load earthquakes.'; 

	// Loop through each backend candidate until we get a successful response.
	for (const backendUrl of candidates) {
		try {
			const response = await fetchFn(`${backendUrl}/api/earthquakes`);
			if (!response.ok) {
				lastError = `Failed to load earthquakes (${response.status})`;
				continue; // Try the next candidate if the response is not successful.
			}

			// Successful response, so we parse the earthquakes.
			earthquakes = (await response.json()) as EarthquakeDto[];
			break;
		} catch (error) {
			lastError = error instanceof Error ? error.message : 'Unable to load earthquakes.';
		}
	}

	// If we tried all candidates and still don't have earthquakes, return an empty array and the last error message.
	if (!earthquakes) {
		return { earthquakes: [], loadError: lastError };
	}

	return { earthquakes, loadError: '' };
};
