import { json } from '@sveltejs/kit';
import type { RequestHandler } from './$types';

/*
* This file defines two request handlers for the SvelteKit API routes: one for DELETE requests to delete an earthquake by ID, 
* and another for POST requests to refresh the earthquake data. 
* Both handlers use a helper function `requestBackends` to attempt the operation against multiple backend URLs, 
* providing fault tolerance in case one of the backends is down or unreachable.
*/
const BACKEND_CANDIDATES = [
	process.env.BACKEND_URL,
	'http://localhost:8080',
	'http://127.0.0.1:8080'
].filter(Boolean) as string[];

const requestBackends = async (
	fetchFn: typeof fetch,
	path: string,
	method: 'DELETE' | 'POST',
	fallbackMessage: string,
	successStatuses: number[] = [200]
) => {
	let lastStatus = 502;
	let lastMessage = fallbackMessage;

	for (const backendUrl of BACKEND_CANDIDATES) {
		try {
			const response = await fetchFn(`${backendUrl}${path}`, { method });
			if (successStatuses.includes(response.status)) {
				return { ok: true as const, response };
			}

			lastStatus = response.status;
			lastMessage = `${method} failed (${response.status})`;
		} catch (error) {
			lastMessage = error instanceof Error ? error.message : fallbackMessage;
		}
	}

	return { ok: false as const, lastStatus, lastMessage };
};


// Handler for DELETE requests to delete an earthquake by ID. Validates the ID and attempts to delete it from the backends.
export const DELETE: RequestHandler = async ({ params, fetch }) => {
	const id = params.id;
	if (!id || !/^\d+$/.test(id)) {
		return json({ error: 'Invalid id' }, { status: 400 });
	}

	const result = await requestBackends(
		fetch,
		`/api/earthquakes/${id}`,
		'DELETE',
		'Unable to reach backend delete endpoint.',
		[200, 204, 404]
	);

	if (result.ok) {
		return new Response(null, { status: 204 });
	}

	return json({ error: result.lastMessage }, { status: result.lastStatus });
};

// Handler for POST requests to refresh earthquake data. Only supports the "refresh" operation and attempts it against the backends.
export const POST: RequestHandler = async ({ params, fetch }) => {
	if (params.id !== 'refresh') {
		return json({ error: 'Unsupported operation' }, { status: 400 });
	}

	const result = await requestBackends(
		fetch,
		'/api/earthquakes/refresh',
		'POST',
		'Unable to reach backend refresh endpoint.'
	);

	if (result.ok) {
		const earthquakes = await result.response.json();
		return json(earthquakes);
	}

	return json({ error: result.lastMessage }, { status: result.lastStatus });
};
