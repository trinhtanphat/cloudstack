#!/usr/bin/env bash
# VNSO CloudStack — async job throughput benchmark.
#
# Deploys N "no-op" listVirtualMachines requests in parallel against the
# management API and reports p50/p95/p99 latency and success rate.
#
# Requirements: bash 4+, curl, jq, openssl (for HMAC), GNU date.
#
# Env vars:
#   VNSO_URL    e.g. https://manage.example.vn
#   VNSO_KEY    API key
#   VNSO_SECRET API secret
#   VNSO_N      total requests   (default 200)
#   VNSO_C      concurrency      (default 20)
#   VNSO_CMD    API command      (default listVirtualMachines)

set -euo pipefail

: "${VNSO_URL:?VNSO_URL is required}"
: "${VNSO_KEY:?VNSO_KEY is required}"
: "${VNSO_SECRET:?VNSO_SECRET is required}"
N=${VNSO_N:-200}
C=${VNSO_C:-20}
CMD=${VNSO_CMD:-listVirtualMachines}

cleanup() { rm -rf "$tmp"; }
tmp="$(mktemp -d)"; trap cleanup EXIT

# CloudStack signing — sorted, lowercased, %20 not '+'.
sign_url() {
  local cmd="$1"
  local raw="apikey=${VNSO_KEY}&command=${cmd}&response=json"
  local lower; lower="$(printf '%s' "$raw" | tr 'A-Z' 'a-z')"
  local sig
  sig="$(printf '%s' "$lower" \
        | openssl dgst -sha1 -hmac "$VNSO_SECRET" -binary \
        | openssl base64 -A)"
  # URL-encode '+' '=' '/'
  sig="${sig//+/%2B}"; sig="${sig//=/%3D}"; sig="${sig//\//%2F}"
  printf '%s/client/api?%s&signature=%s' "$VNSO_URL" "$raw" "$sig"
}

URL="$(sign_url "$CMD")"

worker() {
  local i="$1"
  local out="$tmp/r.$i"
  curl --silent --output /dev/null \
       --write-out '%{http_code} %{time_total}\n' \
       --max-time 30 "$URL" >"$out" || true
}
export -f worker
export tmp URL

echo "Benchmarking $CMD: N=$N concurrency=$C against $VNSO_URL"
SECONDS=0
seq 1 "$N" | xargs -I{} -P "$C" bash -c 'worker "$@"' _ {}
elapsed=$SECONDS

# Aggregate
total=0; ok=0; fail=0
times=()
while read -r code t; do
  total=$((total+1))
  if [[ "$code" == "200" ]]; then ok=$((ok+1)); else fail=$((fail+1)); fi
  times+=("$t")
done < <(cat "$tmp"/r.*)

# Sort times
IFS=$'\n' sorted=($(printf '%s\n' "${times[@]}" | sort -n)); unset IFS
pct() {
  local p="$1"
  local n=${#sorted[@]}
  [[ $n -eq 0 ]] && { echo "0"; return; }
  local idx=$(( (p * n + 99) / 100 - 1 ))
  [[ $idx -lt 0 ]] && idx=0
  [[ $idx -ge $n ]] && idx=$((n-1))
  echo "${sorted[$idx]}"
}

throughput=$(awk -v n="$ok" -v s="$elapsed" 'BEGIN{ if (s==0) s=1; printf "%.2f", n/s }')

cat <<EOF

==== Result ====
URL          : $VNSO_URL
Command      : $CMD
Requests     : $total (ok=$ok fail=$fail)
Concurrency  : $C
Wall time    : ${elapsed}s
Throughput   : ${throughput} req/s
Latency p50  : $(pct 50) s
Latency p95  : $(pct 95) s
Latency p99  : $(pct 99) s
EOF
