import type { PageServerLoad } from './$types';
import { loadEarthquakes } from '$lib/server/earthquake-loader';

export const load: PageServerLoad = async ({ fetch }) => {
	return loadEarthquakes(fetch);
};
