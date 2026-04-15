//SvelteKit server load function for the root page of the application. 
//It imports a function `loadEarthquakes` from a local module and uses it to fetch earthquake data when the page is loaded.
import type { PageServerLoad } from './$types';
import { loadEarthquakes } from '$lib/server/earthquake-loader';

export const load: PageServerLoad = async ({ fetch }) => {
	return loadEarthquakes(fetch);
};
