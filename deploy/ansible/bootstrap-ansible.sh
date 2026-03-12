#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
VENV_PATH="${ROOT_DIR}/.venv"

export DEBIAN_FRONTEND=noninteractive
apt-get update
apt-get install -y openssh-client python3-pip python3-venv
python3 -m venv "${VENV_PATH}"
"${VENV_PATH}/bin/pip" install --upgrade pip
"${VENV_PATH}/bin/pip" install 'ansible-core>=2.17,<2.18'

echo
echo "Bootstrap complete. Use:"
echo "  ${VENV_PATH}/bin/ansible --version"
echo "  ${VENV_PATH}/bin/ansible -i ${ROOT_DIR}/inventory.ini all -m ping"
echo "  ${VENV_PATH}/bin/ansible-playbook -i ${ROOT_DIR}/inventory.ini ${ROOT_DIR}/playbooks/site.yml"
