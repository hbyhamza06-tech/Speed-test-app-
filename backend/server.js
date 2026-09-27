const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const morgan = require('morgan');
const rateLimit = require('express-rate-limit');
const crypto = require('crypto');
const sqlite3 = require('sqlite3').verbose();
const path = require('path');

const app = express();
const PORT = process.env.PORT || 3000;

// Security & Middleware
app.use(helmet({ contentSecurityPolicy: false }));
app.use(cors({ origin: '*' }));
app.use(morgan('combined'));
app.use(express.json());

// Initialize SQLite database
const db = new sqlite3.Database(path.join(__dirname, 'speedtest.db'), (err) => {
  if (err) {
    console.error('Error connecting to SQLite database:', err);
  } else {
    console.log('Connected to SQLite database.');
    db.run(`
      CREATE TABLE IF NOT EXISTS speed_results (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
        download_mbps REAL,
        upload_mbps REAL,
        ping_ms INTEGER,
        jitter_ms INTEGER,
        packet_loss REAL,
        server_name TEXT,
        client_ip TEXT,
        network_type TEXT,
        user_agent TEXT
      )
    `);
  }
});

// Rate limiter for general API endpoints
const apiLimiter = rateLimit({
  windowMs: 1 * 60 * 1000,
  max: 120,
  message: { error: 'Too many requests, please try again later.' }
});

// 1. Health Check
app.get('/api/health', (req, res) => {
  res.status(200).json({
    status: 'healthy',
    uptime: process.uptime(),
    timestamp: new Date().toISOString(),
    service: 'PulseSpeed High-Concurrency Test Engine'
  });
});

// 2. Ultra-low Latency Ping Endpoint
app.get('/api/ping', (req, res) => {
  res.setHeader('Cache-Control', 'no-cache, no-store, must-revalidate');
  res.setHeader('Pragma', 'no-cache');
  res.setHeader('Expires', '0');
  res.status(200).send('pong');
});

// 3. Test Servers Discovery
app.get('/api/servers', (req, res) => {
  res.json({
    servers: [
      {
        id: 'node_local',
        name: 'PulseSpeed Primary Node',
        sponsor: 'Dedicated Backend',
        city: 'Local Edge',
        country: 'Worldwide',
        host: req.get('host'),
        pingUrl: `/api/ping`,
        downloadUrl: `/api/download?size=`,
        uploadUrl: `/api/upload`
      },
      {
        id: 'cf_edge',
        name: 'Cloudflare Global Edge',
        sponsor: 'Anycast CDN',
        city: 'Nearest POP',
        country: 'Global',
        host: 'speed.cloudflare.com',
        pingUrl: 'https://speed.cloudflare.com/__down?bytes=0',
        downloadUrl: 'https://speed.cloudflare.com/__down?bytes=',
        uploadUrl: 'https://speed.cloudflare.com/__up'
      }
    ]
  });
});

// 4. Download Stream Endpoint (Streams configurable binary chunks)
// Generates random bytes chunk-by-chunk to accurately saturate high-bandwidth pipes up to 10 Gbps
const PRE_ALLOCATED_CHUNK = crypto.randomBytes(64 * 1024); // 64 KB reusable chunk

app.get('/api/download', (req, res) => {
  const sizeParam = req.query.size || '10MB';
  let totalBytes = 10 * 1024 * 1024; // Default 10 MB

  if (typeof sizeParam === 'string') {
    if (sizeParam.endsWith('MB')) {
      totalBytes = parseInt(sizeParam) * 1024 * 1024;
    } else if (sizeParam.endsWith('KB')) {
      totalBytes = parseInt(sizeParam) * 1024;
    } else if (!isNaN(parseInt(sizeParam))) {
      totalBytes = parseInt(sizeParam);
    }
  }

  // Cap size between 1 KB and 200 MB
  totalBytes = Math.max(1024, Math.min(totalBytes, 200 * 1024 * 1024));

  res.setHeader('Content-Type', 'application/octet-stream');
  res.setHeader('Content-Length', totalBytes);
  res.setHeader('Cache-Control', 'no-cache, no-store, must-revalidate');

  let bytesSent = 0;

  function streamChunk() {
    while (bytesSent < totalBytes) {
      const remaining = totalBytes - bytesSent;
      const chunkSize = Math.min(remaining, PRE_ALLOCATED_CHUNK.length);
      const chunk = chunkSize === PRE_ALLOCATED_CHUNK.length
        ? PRE_ALLOCATED_CHUNK
        : PRE_ALLOCATED_CHUNK.subarray(0, chunkSize);

      bytesSent += chunkSize;

      const canContinue = res.write(chunk);
      if (!canContinue) {
        res.once('drain', streamChunk);
        return;
      }
    }
    res.end();
  }

  streamChunk();
});

// 5. Upload Stream Endpoint (Accepts high-concurrency binary stream without storing to disk)
app.post('/api/upload', (req, res) => {
  const startTime = process.hrtime();
  let receivedBytes = 0;

  req.on('data', (chunk) => {
    receivedBytes += chunk.length;
  });

  req.on('end', () => {
    const diff = process.hrtime(startTime);
    const durationSec = diff[0] + diff[1] / 1e9;
    const mbps = durationSec > 0 ? (receivedBytes * 8) / (durationSec * 1_000_000) : 0;

    res.json({
      success: true,
      receivedBytes,
      durationSec: parseFloat(durationSec.toFixed(3)),
      calculatedMbps: parseFloat(mbps.toFixed(2))
    });
  });

  req.on('error', (err) => {
    res.status(500).json({ error: 'Stream error', details: err.message });
  });
});

// 6. Save Speed Test Result
app.post('/api/results', (req, res) => {
  const {
    downloadMbps,
    uploadMbps,
    pingMs,
    jitterMs,
    packetLoss,
    serverName,
    networkType
  } = req.body;

  const clientIp = req.headers['x-forwarded-for'] || req.socket.remoteAddress;
  const userAgent = req.headers['user-agent'] || 'PulseSpeed Android';

  const stmt = db.prepare(`
    INSERT INTO speed_results (
      download_mbps, upload_mbps, ping_ms, jitter_ms, packet_loss,
      server_name, client_ip, network_type, user_agent
    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
  `);

  stmt.run(
    downloadMbps || 0,
    uploadMbps || 0,
    pingMs || 0,
    jitterMs || 0,
    packetLoss || 0,
    serverName || 'Unknown Server',
    clientIp,
    networkType || 'Unknown',
    userAgent,
    function (err) {
      if (err) {
        return res.status(500).json({ error: 'Failed to record speed test result.' });
      }
      res.status(201).json({ success: true, id: this.lastID });
    }
  );
  stmt.finalize();
});

// 7. Get Recent Results
app.get('/api/results', (req, res) => {
  db.all('SELECT * FROM speed_results ORDER BY timestamp DESC LIMIT 50', [], (err, rows) => {
    if (err) {
      return res.status(500).json({ error: 'Failed to fetch results' });
    }
    res.json({ results: rows });
  });
});

app.listen(PORT, () => {
  console.log(`PulseSpeed Backend listening on port ${PORT}`);
});
