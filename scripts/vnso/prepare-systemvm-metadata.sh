#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
TEMPLATES_DIR="${ROOT_DIR}/engine/schema/dist/systemvm-templates"
SHA_FILE="${TEMPLATES_DIR}/sha512sum.txt"
META_FILE="${TEMPLATES_DIR}/metadata.ini"
GEN_SCRIPT="${ROOT_DIR}/engine/schema/templateConfig.sh"

if [[ ! -f "${SHA_FILE}" ]]; then
  echo "[prepare-systemvm-metadata] Missing ${SHA_FILE}; cannot prepare metadata." >&2
  exit 1
fi

if [[ -s "${META_FILE}" ]]; then
  echo "[prepare-systemvm-metadata] metadata.ini already present."
  exit 0
fi

# Derive a systemvm template version from sha512sum entries, e.g. 4.22.0.
version_from_sha="$(awk '{print $2}' "${SHA_FILE}" | sed -n 's/.*systemvmtemplate-\([0-9]\+\.[0-9]\+\.[0-9]\+\)-.*/\1/p' | head -n1)"
if [[ -z "${version_from_sha}" ]]; then
  echo "[prepare-systemvm-metadata] Could not infer template version from ${SHA_FILE}." >&2
  exit 1
fi

if [[ ! -x "${GEN_SCRIPT}" ]]; then
  chmod +x "${GEN_SCRIPT}"
fi

# templateConfig.sh expects a 4-part version string.
version_for_generator="${version_from_sha}.0"

echo "[prepare-systemvm-metadata] Generating metadata.ini for ${version_for_generator}."
bash "${GEN_SCRIPT}" "${version_for_generator}"

if [[ ! -s "${META_FILE}" ]]; then
  echo "[prepare-systemvm-metadata] Failed to generate ${META_FILE}." >&2
  exit 1
fi

echo "[prepare-systemvm-metadata] Generated ${META_FILE}."
