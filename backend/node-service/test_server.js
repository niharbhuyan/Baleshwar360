'use strict';

const test = require('node:test');
const assert = require('node:assert/strict');
const {
  parseGoogleNewsRss,
  evaluateSevereWeatherAlert,
  filterTourismLocations,
  createServer,
} = require('./server');

test('parseGoogleNewsRss extracts structured Balasore articles', () => {
  const sampleXml = `
    <rss><channel>
      <item>
        <title>Chandipur Beach High Tide Advisory Issued for Weekend Visitors</title>
        <link>https://example.com/chandipur-tide</link>
        <pubDate>Thu, 26 Nov 2026 09:00:00 GMT</pubDate>
        <source>Odisha Samachar</source>
        <description><![CDATA[Coastal administration alerts visitors about spring tide window.]]></description>
      </item>
      <item>
        <title>Balasore Municipality Opens Smart Drainage Corridor</title>
        <link>https://example.com/balasore-drain</link>
        <pubDate>Thu, 26 Nov 2026 08:30:00 GMT</pubDate>
        <source>Baleshwar Wire</source>
      </item>
    </channel></rss>
  `;

  const articles = parseGoogleNewsRss(sampleXml, 10);
  assert.equal(articles.length, 2);
  assert.equal(articles[0].category, 'Weather');
  assert.equal(articles[0].source, 'Odisha Samachar');
  assert.equal(articles[1].category, 'Local News');
});

test('evaluateSevereWeatherAlert flags thunderstorm, gale winds, heatwave, and normal weather', () => {
  const storm = evaluateSevereWeatherAlert({ temperatureC: 29, windSpeedKmh: 65, weatherCode: 95 });
  assert.equal(storm.isSevere, true);
  assert.equal(storm.level, 'CRITICAL');

  const heat = evaluateSevereWeatherAlert({ temperatureC: 41.5, windSpeedKmh: 12, weatherCode: 0 });
  assert.equal(heat.isSevere, true);
  assert.equal(heat.level, 'URGENT');

  const calm = evaluateSevereWeatherAlert({ temperatureC: 30.2, windSpeedKmh: 14, weatherCode: 1 });
  assert.equal(calm.isSevere, false);
  assert.equal(calm.level, 'NORMAL');
});

test('filterTourismLocations filters accurately by attraction category', () => {
  const all = filterTourismLocations('ALL');
  const beaches = filterTourismLocations('BEACH');
  const temples = filterTourismLocations('TEMPLE');
  const nature = filterTourismLocations('NATURE');
  const heritage = filterTourismLocations('HERITAGE');

  assert.ok(all.length >= 8);
  assert.ok(beaches.length >= 2 && beaches.every((b) => b.category === 'BEACH'));
  assert.ok(temples.length >= 3 && temples.every((t) => t.category === 'TEMPLE'));
  assert.ok(nature.length >= 1 && nature.every((n) => n.category === 'NATURE'));
  assert.ok(heritage.length >= 2 && heritage.every((h) => h.category === 'HERITAGE'));
});

test('HTTP server serves /api/dashboard/balasore and /api/tourism/locations?category=BEACH', async () => {
  const server = createServer({
    weatherFetcher: async () => ({
      location: 'Balasore, Odisha',
      temperatureC: 31.2,
      windSpeedKmh: 18.4,
      condition: 'Partly Cloudy',
      alert: { isSevere: false, level: 'NORMAL' },
    }),
    newsFetcher: async () => ({
      provider: 'MockRSS',
      count: 1,
      articles: [{ id: 'a1', title: 'Chandipur Sea Festival', category: 'Tourism' }],
    }),
  });

  await new Promise((resolve) => server.listen(0, resolve));
  const { port } = server.address();

  try {
    const dashRes = await fetch(`http://127.0.0.1:${port}/api/dashboard/balasore`);
    assert.equal(dashRes.status, 200);
    const dashJson = await dashRes.json();
    assert.equal(dashJson.city, 'Balasore, Odisha');
    assert.equal(dashJson.weather.temperatureC, 31.2);
    assert.equal(dashJson.news.count, 1);

    const locRes = await fetch(`http://127.0.0.1:${port}/api/tourism/locations?category=BEACH`);
    assert.equal(locRes.status, 200);
    const locJson = await locRes.json();
    assert.equal(locJson.category, 'BEACH');
    assert.ok(locJson.locations.every((loc) => loc.category === 'BEACH'));

    const uiRes = await fetch(`http://127.0.0.1:${port}/`);
    assert.equal(uiRes.status, 200);
    const uiHtml = await uiRes.text();
    assert.ok(uiHtml.includes('Baleshwar 360'));
  } finally {
    await new Promise((resolve) => server.close(resolve));
  }
});
