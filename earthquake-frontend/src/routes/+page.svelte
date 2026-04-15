<script lang="ts">
	type PageDataShape = {
		earthquakes: EarthquakeDto[];
		loadError: string;
	};

	export let data: PageDataShape;

	// This is the shape of the raw data we get from the backend API.
	type EarthquakeDto = {
		id: number;
		magnitude: number | null;
		place: string | null;
		time: string | null;
		longitude: number | null;
		latitude: number | null;
	};

	// This is the shape of the data we will use in the component after processing the raw Data Transfer Objects from the backend.
	type QuakeRow = {
		id: number;
		magnitude: number;
		place: string;
		time: Date;
		timeLabel: string;
		latitude: number | null;
		longitude: number | null;
	};

	// This function takes an array of EarthquakeDto objects and transforms them into an array of QuakeRow objects,
	// which are easier to work with in the UI.
	const toRows = (earthquakes: EarthquakeDto[]): QuakeRow[] => {
		return earthquakes
			.map((quake) => {
				const quakeTime = parseDate(quake.time);
				return {
					id: quake.id,
					magnitude: Number(quake.magnitude ?? 0),
					place: quake.place ?? 'Unknown location',
					time: quakeTime,
					timeLabel: displayTime(quakeTime),
					latitude: quake.latitude,
					longitude: quake.longitude
				};
			})
			.sort((a, b) => b.time.getTime() - a.time.getTime());
	};

	let deletingIds = new Set<number>();
	let isRefreshing = false;

	// Parse a date string into a Date object. If the input is null or invalid, it returns a default date (epoch).
	const parseDate = (value: string | null): Date => {
		if (!value) return new Date(0);
		const date = new Date(value);
		return Number.isNaN(date.getTime()) ? new Date(0) : date;
	};

	// Format a Date object into a string in the format "YYYY-MM-DD HH:mm:ss".
	const formatAbsolute = (value: Date): string => {
		const y = value.getFullYear();

		// Months are zero-indexed in JavaScript, so we add 1. We also pad with leading zeros to ensure two digits.
		const m = String(value.getMonth() + 1).padStart(2, '0');

		const d = String(value.getDate()).padStart(2, '0');
		const hh = String(value.getHours()).padStart(2, '0');
		const mm = String(value.getMinutes()).padStart(2, '0');
		const ss = String(value.getSeconds()).padStart(2, '0');
		return `${y}-${m}-${d} ${hh}:${mm}:${ss}`;
	};

	// Format a Date object into a relative time string like "JUST NOW", "1 MIN AGO", or "X MIN AGO".
	const formatMinutesAgo = (value: Date): string => {
		// Calculate the difference in minutes between the current time and the given date. 
		// We use Math.max to ensure we don't return negative minutes if the date is in the future.
		const minutes = Math.max(0, Math.floor((Date.now() - value.getTime()) / 60000));
		if (minutes < 1) return 'JUST NOW';
		if (minutes === 1) return '1 MIN AGO';
		return `${minutes} MIN AGO`;
	};

	// If the event occurred within the last 20 minutes, it shows a relative time. Otherwise, it shows the absolute time.
	const displayTime = (value: Date): string => {
		const minutes = Math.floor((Date.now() - value.getTime()) / 60000);
		if (minutes >= 0 && minutes <= 20) return formatMinutesAgo(value);
		return formatAbsolute(value);
	};

	 
	let rows: QuakeRow[] = toRows(data.earthquakes as EarthquakeDto[]);

	$: totalQuakes = rows.length;

	/*$: syntax is used to create reactive statements that automatically re-run whenever any of the variables they depend on change.
	*We use the map function to create a new array of just the magnitudes, and then we use the spread operator (...) to pass that array as individual arguments to Math.max. 
	*/
	$: maxMagnitude = rows.length > 0 ? Math.max(...rows.map((row) => row.magnitude)) : 0;

	$: latest = rows[0] ?? null;
	$: latestEventLabel = latest ? formatMinutesAgo(latest.time).replace(' MIN AGO', ' MINUTES AGO') : 'NO DATA';
	$: latestPlace = latest ? latest.place : 'No location';

	const coordinateText = (row: QuakeRow): string => {
		if (row.latitude === null || row.longitude === null) return '-';
		return `${row.latitude.toFixed(3)}, ${row.longitude.toFixed(3)}`;
	};

	const mapsUrl = (row: QuakeRow): string => {
		if (row.latitude === null || row.longitude === null) return '#';
		return `https://www.google.com/maps?q=${row.latitude},${row.longitude}`;
	};

	const deleteRow = async (id: number) => {
		
		// If the earthquake is already being deleted, we return early to prevent multiple delete requests for the same earthquake.
		if (deletingIds.has(id)) return;

		// We add the earthquake ID to the deletingIds set to indicate that it is currently being deleted. This will disable the delete button for that earthquake in the UI.
		deletingIds = new Set(deletingIds).add(id);

		/*We send a DELETE request to the backend API endpoint for that earthquake ID. If the response is not successful (not OK), 
		* we throw an error to be caught in the catch block. 
		* If the delete is successful, we filter out the deleted earthquake from the rows array to update the UI.
		*/
		try {
			const response = await fetch(`/api/earthquakes/${id}`, { method: 'DELETE' });
			if (!response.ok) {
				throw new Error('Delete failed');
			}

			rows = rows.filter((row) => row.id !== id);
		} catch {
			// Keep UI stable; delete is best effort and can be retried.
		} finally {
			const copy = new Set(deletingIds);
			copy.delete(id);
			deletingIds = copy;
		}
	};

	const refreshRows = async () => {
		if (isRefreshing) return;
		isRefreshing = true;

		try {
			const response = await fetch('/api/earthquakes/refresh', { method: 'POST' });
			if (!response.ok) {
				throw new Error('Refresh failed');
			}

			const refreshed = (await response.json()) as EarthquakeDto[];
			rows = toRows(refreshed);
		} catch {
			// Keep existing rows when refresh fails.
		} finally {
			isRefreshing = false;
		}
	};
</script>

<svelte:head>
	<title>Monitoring Earthquakes</title>
</svelte:head>

<main class="page-shell">
	<section class="panel hero">
		<h1>MONITORING EARTHQUAKES</h1>
	</section>

	<section class="stats-grid">
		<article class="panel stat-card">
			<h2>TOTAL QUAKES IN THE LAST 4 HOURS</h2>
			<p class="stat-value">{totalQuakes}</p>
		</article>

		<article class="panel stat-card middle-card">
			<h2>MAX MAGNITUDE</h2>
			<p class="stat-value danger">{maxMagnitude.toFixed(1)}</p>
		</article>

		<article class="panel stat-card">
			<h2>LAST EVENT</h2>
			<p class="last-event-time">{latestEventLabel}</p>
			<p class="last-event-place">{latestPlace}</p>
		</article>
	</section>

	<section class="panel table-wrap">
		{#if data.loadError}
			<p class="table-message">{data.loadError}</p>
		{:else}
			<table>
				<thead>
					<tr>
						<th>Place</th>
						<th>Mag.</th>
						<th>Time</th>
						<th>Coordinates</th>
						<th>Action</th>
					</tr>
				</thead>
				<tbody>
					{#if rows.length === 0}
						<tr>
							<td class="empty-state-cell" colspan="5">No earthquakes found.</td>
						</tr>
					{:else}
						{#each rows as row}
							<tr>
								<td>{row.place}</td>
								<td class="mag" class:danger={row.magnitude > 3.5}>{row.magnitude.toFixed(1)}</td>
								<td>{row.timeLabel}</td>
								<td>
									{#if row.latitude !== null && row.longitude !== null}
										<a
											class="coord-link"
											href={mapsUrl(row)}
											target="_blank"
											rel="noopener noreferrer"
										>
											{coordinateText(row)}
										</a>
									{:else}
										<span class="coord-empty">-</span>
									{/if}
								</td>
								<td>
									<button
										type="button"
										class="delete-label"
										on:click={() => deleteRow(row.id)}
										disabled={deletingIds.has(row.id)}
									>
										DELETE
									</button>
								</td>
							</tr>
						{/each}
					{/if}
				</tbody>
			</table>
		{/if}
	</section>

	<section class="panel safety-box">
		<h2>SAFETY TIPS:</h2>
		<p>Drop, cover and hold on immediately until the shaking stops.</p>
		<p>Stay away from windows, heavy furniture, or hanging objects that could fall.</p>
		<p>Once the movement ceases, check yourself for injuries and move cautiously to an open area.</p>
		<a
			class="see-more-link"
			href="https://www.redcross.org/get-help/how-to-prepare-for-emergencies/types-of-emergencies/earthquake.html?srsltid=AfmBOooK9n2a2gJ5aSSGEljulQhI9r_hXUa1JRcs2TxxiVR-MxB7Dazy"
			target="_blank"
			rel="noopener noreferrer"
		>
			See more
		</a>
	</section>

	<div class="refresh-wrap">
		<button type="button" class="delete-label refresh-label" on:click={refreshRows} disabled={isRefreshing}>
			REFRESH
		</button>
	</div>
</main>
