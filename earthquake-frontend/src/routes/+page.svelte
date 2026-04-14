<script lang="ts">
	type PageDataShape = {
		earthquakes: EarthquakeDto[];
		loadError: string;
	};

	export let data: PageDataShape;

	type EarthquakeDto = {
		id: number;
		magnitude: number | null;
		place: string | null;
		time: string | null;
		longitude: number | null;
		latitude: number | null;
	};

	type QuakeRow = {
		id: number;
		magnitude: number;
		place: string;
		time: Date;
		timeLabel: string;
		latitude: number | null;
		longitude: number | null;
	};

	let deletingIds = new Set<number>();

	const parseDate = (value: string | null): Date => {
		if (!value) return new Date(0);
		const date = new Date(value);
		return Number.isNaN(date.getTime()) ? new Date(0) : date;
	};

	const formatAbsolute = (value: Date): string => {
		const y = value.getFullYear();
		const m = String(value.getMonth() + 1).padStart(2, '0');
		const d = String(value.getDate()).padStart(2, '0');
		const hh = String(value.getHours()).padStart(2, '0');
		const mm = String(value.getMinutes()).padStart(2, '0');
		const ss = String(value.getSeconds()).padStart(2, '0');
		return `${y}-${m}-${d} ${hh}:${mm}:${ss}`;
	};

	const formatMinutesAgo = (value: Date): string => {
		const minutes = Math.max(0, Math.floor((Date.now() - value.getTime()) / 60000));
		if (minutes < 1) return 'JUST NOW';
		if (minutes === 1) return '1 MIN AGO';
		return `${minutes} MIN AGO`;
	};

	const displayTime = (value: Date): string => {
		const minutes = Math.floor((Date.now() - value.getTime()) / 60000);
		if (minutes >= 0 && minutes <= 20) return formatMinutesAgo(value);
		return formatAbsolute(value);
	};

	let rows: QuakeRow[] = (data.earthquakes as EarthquakeDto[])
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

	$: totalQuakes = rows.length;
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
		if (deletingIds.has(id)) return;

		deletingIds = new Set(deletingIds).add(id);

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
							<td class="empty-state-cell" colspan="5">No earthquakes found for the active backend filters.</td>
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
	</section>
</main>
