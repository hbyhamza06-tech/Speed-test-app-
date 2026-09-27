# PulseSpeed Dedicated Backend Engine

High-concurrency streaming backend server for network throughput, latency, jitter, and packet loss benchmarking.

## Endpoints

- `GET /api/health` - Server health status and uptime
- `GET /api/ping` - Zero-latency ping probe
- `GET /api/servers` - Server discovery list
- `GET /api/download?size=25MB` - High-throughput binary chunk streaming
- `POST /api/upload` - Chunked streaming upload bandwidth calculation
- `POST /api/results` - Store test report in database
- `GET /api/results` - Retrieve previous test history

## Running Locally

```bash
cd backend
npm install
npm start
```

Runs on `http://localhost:3000`. In the Android Emulator, use `http://10.0.2.2:3000`.

## Running with Docker Compose

```bash
docker-compose up -d --build
```
