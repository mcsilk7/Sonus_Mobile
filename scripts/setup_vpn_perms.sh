#!/bin/bash
# Sonus VPN Permission Setup Script
# version 1.1

echo "======================================================="
echo "   SONUS STATION - VPN PERMISSION SETUP"
echo "======================================================="
echo ""

USER_NAME="sonus"
RULE_FILE="/etc/sudoers.d/sonus-vpn"

# Check if user exists
if ! id "$USER_NAME" >/dev/null 2>&1; then
    echo "ERROR: System user '$USER_NAME' does not exist."
    echo "Please create the user first: sudo useradd $USER_NAME"
    exit 1
fi

# Identify wg-quick path
WG_PATH=$(which wg-quick)

if [ -z "$WG_PATH" ]; then
    echo "ERROR: 'wg-quick' not found."
    echo "Please install WireGuard tools first:"
    echo "  sudo apt install wireguard-tools  (Ubuntu/Debian)"
    echo "  sudo dnf install wireguard-tools  (Fedora)"
    exit 1
fi

echo "This script will allow '$USER_NAME' to run VPN commands"
echo "without entering a password every time."
echo ""
echo "Command to be authorized: $WG_PATH"
echo ""

# Create sudoers rule
TMP_FILE=$(mktemp)
echo "$USER_NAME ALL=(ALL) NOPASSWD: $WG_PATH" > "$TMP_FILE"

# Apply the rule (requires sudo password once)
echo "Please enter your system password to apply permissions:"
sudo cp "$TMP_FILE" "$RULE_FILE"
sudo chmod 440 "$RULE_FILE"
rm "$TMP_FILE"

if [ $? -eq 0 ]; then
    echo ""
    echo "SUCCESS: Permissions granted."
    echo "Sonus will now establish secure tunnels automatically."
    echo "======================================================="
    exit 0
else
    echo ""
    echo "FAILED: Could not apply permissions."
    echo "======================================================="
    exit 1
fi
