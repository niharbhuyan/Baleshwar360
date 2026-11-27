'use strict';

const http = require('node:http');
const fs = require('node:fs');
const path = require('node:path');
const { URL } = require('node:url');

const BALASORE_LAT = 21.4934;
const BALASORE_LON = 86.9135;
const CACHE_TTL_MS = 5 * 60 * 1000; // 5-minute TTL cache

const TOURISM_LOCATIONS = [
  {
    id: 'chandipur_beach',
    name: 'Chandipur-on-Sea (Vanishing Beach)',
    odiaName: 'ଚାନ୍ଦିପୁର ବେଳାଭୂମି',
    category: 'BEACH',
    subcategory: 'Beach & Coast',
    shortDescription: 'World-famous vanishing sea beach where the Bay of Bengal recedes up to 5 km twice daily.',
    latitude: 21.4442,
    longitude: 87.0147,
    distanceFromTownKm: 16.0,
    timings: '05:30 AM - 08:30 PM',
    rating: 4.9,
  },
  {
    id: 'talasari_beach',
    name: 'Talasari Beach & Subarnarekha Estuary',
    odiaName: 'ତାଳସାରୀ ବେଳାଭୂମି',
    category: 'BEACH',
    subcategory: 'Estuary Beach',
    shortDescription: 'Palm-fringed serene beach at the Subarnarekha river mouth with boat rides across calm tidal creeks.',
    latitude: 21.6085,
    longitude: 87.4659,
    distanceFromTownKm: 88.0,
    timings: '06:00 AM - 07:00 PM',
    rating: 4.8,
  },
  {
    id: 'khirachora_gopinath',
    name: 'Khirachora Gopinatha Temple, Remuna',
    odiaName: 'କ୍ଷୀରଚୋରା ଗୋପୀନାଥ ମନ୍ଦିର, ରେମୁଣା',
    category: 'TEMPLE',
    subcategory: 'Heritage Temple',
    shortDescription: '13th-century Vaishnavite shrine renowned for sacred Amruta Keli condensed milk bhog.',
    latitude: 21.5292,
    longitude: 86.8711,
    distanceFromTownKm: 9.0,
    timings: '05:30 AM - 09:00 PM',
    rating: 4.9,
  },
  {
    id: 'emami_jagannath',
    name: 'Emami Jagannath Temple, Balgopalpur',
    odiaName: 'ଇମାମୀ ଜଗନ୍ନାଥ ମନ୍ଦିର',
    category: 'TEMPLE',
    subcategory: 'Architecture & Temple',
    shortDescription: 'Grand Konark Sun-Chariot style Jagannath temple featuring 16 carved stone wheels.',
    latitude: 21.4981,
    longitude: 86.8624,
    distanceFromTownKm: 11.0,
    timings: '06:00 AM - 08:30 PM',
    rating: 4.8,
  },
  {
    id: 'panchalingeswar_temple',
    name: 'Panchalingeswar Hill Shrine',
    odiaName: 'ପଞ୍ଚଲିଙ୍ଗେଶ୍ୱର ଶୈବ ପୀଠ',
    category: 'TEMPLE',
    subcategory: 'Hill Shrine & Stream',
    shortDescription: 'Five natural Shiva Lingams bathed continuously by a cool mountain stream in the Nilagiri hills.',
    latitude: 21.4253,
    longitude: 86.7441,
    distanceFromTownKm: 30.0,
    timings: '06:00 AM - 06:30 PM',
    rating: 4.9,
  },
  {
    id: 'kuldiha_wildlife_sanctuary',
    name: 'Kuldiha Wildlife Sanctuary',
    odiaName: 'କୁଳଡ଼ିହା ଅଭୟାରଣ୍ୟ',
    category: 'NATURE',
    subcategory: 'Wildlife Sanctuary & Forest',
    shortDescription: '272 sq km dense Sal forest sanctuary home to Asian elephants, gaur, and hornbills.',
    latitude: 21.4196,
    longitude: 86.6203,
    distanceFromTownKm: 40.0,
    timings: '06:30 AM - 04:30 PM',
    rating: 4.8,
  },
  {
    id: 'nilagiri_jagannath_palace',
    name: 'Nilagiri Palace & Heritage Fort Precinct',
    odiaName: 'ନୀଳଗିରି ରାଜବାଟୀ ଓ ଐତିହ୍ୟ',
    category: 'HERITAGE',
    subcategory: 'Royal Heritage & Fort',
    shortDescription: 'Historic princely estate architecture and century-old Jagannath temple precinct at Nilagiri.',
    latitude: 21.4622,
    longitude: 86.7715,
    distanceFromTownKm: 22.0,
    timings: '07:00 AM - 06:30 PM',
    rating: 4.6,
  },
  {
    id: 'bhusandeswar_temple',
    name: 'Bhusandeswar Shiva Temple',
    odiaName: 'ଭୂଷଣ୍ଡେଶ୍ୱର ଶିବ ମନ୍ଦିର',
    category: 'HERITAGE',
    subcategory: 'Ancient Megalithic Shrine',
    shortDescription: 'Home to one of Asia\'s largest monolithic black-granite Shiva Lingams on the Subarnarekha coast.',
    latitude: 21.6625,
    longitude: 87.3812,
    distanceFromTownKm: 92.0,
    timings: '05:30 AM - 08:30 PM',
    rating: 4.7,
  },
];

const memoryCache = {
  weather: { data: null, expiresAt: 0 },
  news: { data: null, expiresAt: 0 },
};

function decodeXmlEntities(text) {
  return String(text || '')
    .replace(/<!\[CDATA\[([\s\S]*?)\]\]>/g, '$1')
    .replace(/&amp;/g, '&')
    .replace(/&lt;/g, '<')
    .replace(/&gt;/g, '>')
    .replace(/&quot;/g, '"')
    .replace(/&#39;/g, "'")
    .replace(/<[^>]+>/g, ' ')
    .replace(/\s+/g, ' ')
    .trim();
}

function parseGoogleNewsRss(xmlText, limit = 12) {
  const items = [];
  const itemRegex = /<item>([\s\S]*?)<\/item>/gi;
  let match;
  while ((match = itemRegex.exec(xmlText)) !== null && items.length < limit) {
    const block = match[1];
    const titleMatch = block.match(/<title>([\s\S]*?)<\/title>/i);
    const linkMatch = block.match(/<link>([\s\S]*?)<\/link>/i);
    const pubDateMatch = block.match(/<pubDate>([\s\S]*?)<\/pubDate>/i);
    const sourceMatch = block.match(/<source[^>]*>([\s\S]*?)<\/source>/i);
    const descMatch = block.match(/<description>([\s\S]*?)<\/description>/i);

    const rawTitle = decodeXmlEntities(titleMatch ? titleMatch[1] : '');
    if (!rawTitle) continue;

    items.push({
      id: `rss_${items.length + 1}_${Buffer.from(rawTitle).toString('base64url').slice(0, 12)}`,
      title: rawTitle,
      summary: decodeXmlEntities(descMatch ? descMatch[1] : rawTitle).slice(0, 220),
      source: decodeXmlEntities(sourceMatch ? sourceMatch[1] : 'Google News Balasore'),
      url: decodeXmlEntities(linkMatch ? linkMatch[1] : ''),
      publishedAt: pubDateMatch ? pubDateMatch[1].trim() : new Date().toISOString(),
      category: /\b(cyclone|rain|rainfall|weather|tide|flood)\b/i.test(rawTitle)
        ? 'Weather'
        : /\b(chandipur|temple|tourism|kuldiha)\b/i.test(rawTitle)
        ? 'Tourism'
        : 'Local News',
    });
  }
  return items;
}

function evaluateSevereWeatherAlert({ temperatureC, windSpeedKmh, weatherCode }) {
  if (weatherCode >= 95 || windSpeedKmh >= 60) {
    return {
      isSevere: true,
      level: 'CRITICAL',
      title: 'Severe Thunderstorm / Coastal Squall Warning',
      advisory: `Hazardous storm conditions over Balasore (${Math.round(windSpeedKmh)} km/h winds). Avoid coastal areas.`,
    };
  }
  if (windSpeedKmh >= 45) {
    return {
      isSevere: true,
      level: 'WARNING',
      title: 'High Coastal Wind Advisory',
      advisory: `Gusty coastal winds (${Math.round(windSpeedKmh)} km/h) along Chandipur & Balaramgadi.`,
    };
  }
  if (temperatureC >= 40) {
    return {
      isSevere: true,
      level: 'URGENT',
      title: 'Balasore Heatwave Warning',
      advisory: `High temperature (${temperatureC.toFixed(1)}°C) recorded in Balasore. Stay hydrated.`,
    };
  }
  return {
    isSevere: false,
    level: 'NORMAL',
    title: 'Normal Coastal Conditions',
    advisory: 'No severe weather alerts active for Balasore.',
  };
}

function describeWmoCode(code) {
  if (code === 0) return 'Clear Sky';
  if (code <= 3) return 'Partly Cloudy';
  if (code <= 48) return 'Coastal Fog / Mist';
  if (code <= 67) return 'Rain Showers';
  if (code <= 82) return 'Heavy Coastal Rain';
  if (code >= 95) return 'Thunderstorm';
  return 'Coastal Breeze';
}

async function fetchLiveBalasoreWeather(fetchImpl = fetch) {
  const now = Date.now();
  if (memoryCache.weather.data && memoryCache.weather.expiresAt > now) {
    return { ...memoryCache.weather.data, cached: true };
  }

  const owmKey = (process.env.OPENWEATHERMAP_API_KEY || '').trim();
  let payload;

  if (owmKey) {
    const owmUrl = `https://api.openweathermap.org/data/2.5/weather?lat=${BALASORE_LAT}&lon=${BALASORE_LON}&units=metric&appid=${encodeURIComponent(owmKey)}`;
    const res = await fetchImpl(owmUrl);
    if (!res.ok) throw new Error(`OpenWeatherMap HTTP ${res.status}`);
    const json = await res.json();
    const tempC = Number(json.main?.temp ?? 30);
    const windKmh = Number(json.wind?.speed ?? 0) * 3.6;
    const owmId = Number(json.weather?.[0]?.id ?? 800);
    const syntheticWmo = owmId >= 200 && owmId < 300 ? 95 : owmId >= 502 && owmId <= 504 ? 82 : 1;

    payload = {
      location: 'Balasore, Odisha',
      coordinates: { lat: BALASORE_LAT, lon: BALASORE_LON },
      provider: 'OpenWeatherMap',
      temperatureC: tempC,
      feelsLikeC: Number(json.main?.feels_like ?? tempC),
      humidityPercent: Number(json.main?.humidity ?? 72),
      windSpeedKmh: Number(windKmh.toFixed(1)),
      condition: json.weather?.[0]?.description || 'Coastal Breeze',
      alert: evaluateSevereWeatherAlert({
        temperatureC: tempC,
        windSpeedKmh: windKmh,
        weatherCode: syntheticWmo,
      }),
      fetchedAt: new Date().toISOString(),
    };
  } else {
    const meteoUrl =
      `https://api.open-meteo.com/v1/forecast?latitude=${BALASORE_LAT}&longitude=${BALASORE_LON}` +
      '&current=temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,wind_speed_10m' +
      '&hourly=temperature_2m,wind_speed_10m&forecast_days=2&timezone=Asia%2FKolkata';
    const res = await fetchImpl(meteoUrl);
    if (!res.ok) throw new Error(`Open-Meteo HTTP ${res.status}`);
    const json = await res.json();
    const cur = json.current || {};
    const tempC = Number(cur.temperature_2m ?? 30);
    const windKmh = Number(cur.wind_speed_10m ?? 12);
    const wmo = Number(cur.weather_code ?? 1);

    payload = {
      location: 'Balasore, Odisha',
      coordinates: { lat: BALASORE_LAT, lon: BALASORE_LON },
      provider: 'Open-Meteo Public API',
      temperatureC: tempC,
      feelsLikeC: Number(cur.apparent_temperature ?? tempC),
      humidityPercent: Number(cur.relative_humidity_2m ?? 74),
      windSpeedKmh: Number(windKmh.toFixed(1)),
      condition: describeWmoCode(wmo),
      alert: evaluateSevereWeatherAlert({
        temperatureC: tempC,
        windSpeedKmh: windKmh,
        weatherCode: wmo,
      }),
      hourlyPreview: (json.hourly?.time || []).slice(0, 8).map((time, idx) => ({
        time,
        temperatureC: json.hourly?.temperature_2m?.[idx] ?? tempC,
        windSpeedKmh: json.hourly?.wind_speed_10m?.[idx] ?? windKmh,
      })),
      fetchedAt: new Date().toISOString(),
    };
  }

  memoryCache.weather = { data: payload, expiresAt: now + CACHE_TTL_MS };
  return { ...payload, cached: false };
}

async function fetchLiveBalasoreNews(fetchImpl = fetch) {
  const now = Date.now();
  if (memoryCache.news.data && memoryCache.news.expiresAt > now) {
    return { ...memoryCache.news.data, cached: true };
  }

  const newsApiKey = (process.env.NEWS_API_KEY || '').trim();
  let payload;

  if (newsApiKey) {
    const url =
      'https://newsapi.org/v2/everything?q=Balasore+OR+Baleshwar+OR+Chandipur&language=en&sortBy=publishedAt&pageSize=12';
    const res = await fetchImpl(url, {
      headers: { 'X-Api-Key': newsApiKey, 'User-Agent': 'Baleshwar360-NodeService/1.0' },
    });
    if (!res.ok) throw new Error(`NewsAPI HTTP ${res.status}`);
    const json = await res.json();
    const articles = (json.articles || []).map((a, idx) => ({
      id: `newsapi_${idx + 1}`,
      title: a.title || 'Balasore District Update',
      summary: a.description || a.title || '',
      source: a.source?.name || 'NewsAPI',
      url: a.url || '',
      publishedAt: a.publishedAt || new Date().toISOString(),
      category: 'Local News',
    }));
    payload = {
      provider: 'NewsAPI',
      count: articles.length,
      articles,
      fetchedAt: new Date().toISOString(),
    };
  } else {
    const rssUrl =
      'https://news.google.com/rss/search?q=Balasore+Odisha+OR+Chandipur&hl=en-IN&gl=IN&ceid=IN:en';
    const res = await fetchImpl(rssUrl, {
      headers: { 'User-Agent': 'Baleshwar360-NodeService/1.0' },
    });
    if (!res.ok) throw new Error(`Google News RSS HTTP ${res.status}`);
    const xml = await res.text();
    const articles = parseGoogleNewsRss(xml, 12);
    payload = {
      provider: 'Google News RSS',
      count: articles.length,
      articles,
      fetchedAt: new Date().toISOString(),
    };
  }

  memoryCache.news = { data: payload, expiresAt: now + CACHE_TTL_MS };
  return { ...payload, cached: false };
}

function filterTourismLocations(categoryParam) {
  const normalized = String(categoryParam || 'ALL').trim().toUpperCase();
  if (!normalized || normalized === 'ALL') {
    return TOURISM_LOCATIONS;
  }
  return TOURISM_LOCATIONS.filter((spot) => spot.category === normalized);
}

function sendJson(res, statusCode, body) {
  const json = JSON.stringify(body);
  res.writeHead(statusCode, {
    'Content-Type': 'application/json; charset=utf-8',
    'Access-Control-Allow-Origin': '*',
    'Access-Control-Allow-Methods': 'GET, OPTIONS',
    'Access-Control-Allow-Headers': 'Content-Type',
  });
  res.end(json);
}

function createServer({ weatherFetcher = fetchLiveBalasoreWeather, newsFetcher = fetchLiveBalasoreNews } = {}) {
  return http.createServer(async (req, res) => {
    if (req.method === 'OPTIONS') {
      res.writeHead(204, {
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Methods': 'GET, OPTIONS',
        'Access-Control-Allow-Headers': 'Content-Type',
      });
      res.end();
      return;
    }

    const reqUrl = new URL(req.url || '/', `http://${req.headers.host || 'localhost'}`);

    try {
      if (req.method === 'GET' && (reqUrl.pathname === '/' || reqUrl.pathname === '/index.html')) {
        const htmlPath = path.join(__dirname, 'public', 'index.html');
        const html = fs.readFileSync(htmlPath, 'utf8');
        res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
        res.end(html);
        return;
      }

      if (req.method === 'GET' && reqUrl.pathname === '/api/health') {
        return sendJson(res, 200, {
          status: 'ok',
          service: 'balasore-realtime-dashboard-service',
          timestamp: new Date().toISOString(),
        });
      }

      if (req.method === 'GET' && reqUrl.pathname === '/api/tourism/locations') {
        const category = reqUrl.searchParams.get('category') || 'ALL';
        const locations = filterTourismLocations(category);
        return sendJson(res, 200, {
          category: category.toUpperCase(),
          count: locations.length,
          locations,
        });
      }

      if (req.method === 'GET' && reqUrl.pathname === '/api/weather/balasore') {
        const weather = await weatherFetcher();
        return sendJson(res, 200, weather);
      }

      if (req.method === 'GET' && reqUrl.pathname === '/api/news/balasore') {
        const news = await newsFetcher();
        return sendJson(res, 200, news);
      }

      if (req.method === 'GET' && reqUrl.pathname === '/api/dashboard/balasore') {
        const [weather, news] = await Promise.all([weatherFetcher(), newsFetcher()]);
        return sendJson(res, 200, {
          city: 'Balasore, Odisha',
          weather,
          news,
          featuredLocations: TOURISM_LOCATIONS.slice(0, 4),
          generatedAt: new Date().toISOString(),
        });
      }

      return sendJson(res, 404, { error: 'Not Found', path: reqUrl.pathname });
    } catch (error) {
      return sendJson(res, 502, {
        error: 'Upstream Balasore feed temporarily unavailable',
        detail: error.message,
      });
    }
  });
}

if (require.main === module) {
  const port = Number(process.env.PORT || 3001);
  const server = createServer();
  server.listen(port, () => {
    console.log(`Balasore Real-Time News & Weather Node.js service listening on port ${port}`);
  });
}

module.exports = {
  BALASORE_LAT,
  BALASORE_LON,
  TOURISM_LOCATIONS,
  parseGoogleNewsRss,
  evaluateSevereWeatherAlert,
  filterTourismLocations,
  fetchLiveBalasoreWeather,
  fetchLiveBalasoreNews,
  createServer,
};
