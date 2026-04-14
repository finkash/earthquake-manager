import { json } from '@sveltejs/kit';
import type { RequestHandler } from './$types';

const BACKEND_CANDIDATES = [
	process.env.BACKEND_URL,
	'http://localhost:8080',
	'http://127.0.0.1:8080'
].filter(Boolean) as string[];

export const DELETE: RequestHandler = async ({ params, fetch }) => {
	const id = params.id;
	if (!id || !/^\d+$/.test(id)) {
		return json({ error: 'Invalid id' }, { status: 400 });
	}

	let lastStatus = 502;
	let lastMessage = 'Unable to reach backend delete endpoint.';

	for (const backendUrl of BACKEND_CANDIDATES) {
		try {
			const response = await fetch(`${backendUrl}/api/earthquakes/${id}`, { method: 'DELETE' });
			if (response.ok || response.status === 404) {
				return new Response(null, { status: 204 });
			}

			lastStatus = response.status;
			lastMessage = `Delete failed (${response.status})`;
		} catch (error) {
			lastMessage = error instanceof Error ? error.message : 'Delete request failed.';
		}
	}

	return json({ error: lastMessage }, { status: lastStatus });
};
