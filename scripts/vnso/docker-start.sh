#!/bin/bash
# Quick start script for CloudStack Docker infrastructure

set -e

echo "=========================================="
echo "  CloudStack Docker Infrastructure Setup"
echo "=========================================="

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Check Docker
if ! command -v docker &> /dev/null; then
    echo -e "${RED}✗ Docker not found. Please install Docker first.${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Docker found${NC}"

# Navigate to CloudStack directory
cd /root/cloudstack

echo ""
echo -e "${YELLOW}Preparing SystemVM template metadata...${NC}"
./scripts/vnso/prepare-systemvm-metadata.sh

echo ""
echo -e "${YELLOW}Building CloudStack Docker image...${NC}"
echo "This may take 10-15 minutes on first run (includes Maven build)..."

if docker build -t cloudstack-mgmt:latest -f Dockerfile.prod . > /tmp/docker-build.log 2>&1; then
    echo -e "${GREEN}✓ Docker image built successfully${NC}"
else
    echo -e "${RED}✗ Docker build failed. Check logs:${NC}"
    tail -50 /tmp/docker-build.log
    exit 1
fi

echo ""
echo -e "${YELLOW}Starting CloudStack containers...${NC}"

# Start docker-compose
if docker-compose -f docker-compose.prod.yml up -d; then
    echo -e "${GREEN}✓ Containers started${NC}"
else
    echo -e "${RED}✗ Failed to start containers${NC}"
    exit 1
fi

echo ""
echo -e "${YELLOW}Waiting for services to be healthy...${NC}"

# Wait for Management Server
echo "Waiting for CloudStack Management Server (port 28080)..."
for i in {1..60}; do
    if curl -s http://127.0.0.1:28080/ > /dev/null 2>&1; then
        echo -e "${GREEN}✓ Management Server is up!${NC}"
        break
    fi
    if [ $i -eq 60 ]; then
        echo -e "${RED}✗ Management Server failed to start. Check logs:${NC}"
        docker logs cloudstack-mgmt | tail -50
        exit 1
    fi
    echo "  Attempt $i/60..."
    sleep 2
done

echo ""
echo "=========================================="
echo -e "${GREEN}✓ CloudStack is ready!${NC}"
echo "=========================================="
echo ""
echo "Services:"
echo "  UI Dashboard:    http://127.0.0.1:25050"
echo "  Management API:  http://127.0.0.1:28080/client/api/"
echo "  Domain (HTTPS):  https://cloudstack.vnso.vn"
echo ""
echo "Database:"
echo "  MySQL:          localhost:3307"
echo "  Database:       cloud"
echo "  User:           cloud / cloud"
echo ""
echo "Logs:"
echo "  Management:     docker logs cloudstack-mgmt -f"
echo "  Database:       docker logs cloudstack-db -f"
echo ""
echo "Rotate admin API key into secret file:"
echo "  ./scripts/vnso/rotate-admin-api-key.sh"
echo "Cleanup stale management host records once:"
echo "  RUN_ONCE=1 ./scripts/vnso/cleanup-mshost-stale.sh"
echo "Run periodic cleanup every 15 minutes (foreground):"
echo "  INTERVAL_SECONDS=900 ./scripts/vnso/cleanup-mshost-stale.sh"
echo ""
echo "Stop services:"
echo "  docker-compose -f docker-compose.prod.yml down"
echo ""
