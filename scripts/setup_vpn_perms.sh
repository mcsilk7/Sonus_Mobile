#!/bin/bash
# Sonus VPN Permission Setup Script
# version 1.6 - Delegation to 'sonus' user

echo "======================================================="
echo "   SONUS STATION - VPN PERMISSION SETUP (DELEGATED)"
echo "======================================================="

# The user who is running the application (e.g., mcsilk)
CALLING_USER=$1
if [ -z "$CALLING_USER" ]; then
    CALLING_USER=${PKEXEC_UID:-$(id -u -n)}
fi

VPN_USER="sonus"
RULE_FILE="/etc/sudoers.d/sonus-vpn"
WG_PATH=$(which wg-quick)

if [ -z "$WG_PATH" ]; then
    for p in /usr/bin/wg-quick /usr/sbin/wg-quick /usr/local/bin/wg-quick; do
        if [ -f "$p" ]; then WG_PATH="$p"; break; fi
    done
fi

# 1. Tworzenie użytkownika 'sonus' jeśli nie istnieje
if ! id "$VPN_USER" >/dev/null 2>&1; then
    echo "Tworzenie użytkownika systemowego '$VPN_USER'..."
    sudo useradd -r -s /usr/sbin/nologin "$VPN_USER"
fi

# 2. Tworzenie reguł sudoers
# Reguła A: Użytkownik 'sonus' może uruchamiać wg-quick jako root bez hasła
# Reguła B: Użytkownik '$CALLING_USER' może uruchamiać sudo JAKO 'sonus' bez hasła
TMP_FILE=$(mktemp)
cat << EOF > "$TMP_FILE"
# Pozwól użytkownikowi sonus zarządzać WireGuardem jako root
$VPN_USER ALL=(ALL) NOPASSWD: $WG_PATH

# Pozwól $CALLING_USER na delegowanie zadań do sonus
$CALLING_USER ALL=($VPN_USER) NOPASSWD: ALL
EOF

# 3. Aplikowanie reguł
echo "Konfigurowanie delegacji uprawnień dla: $CALLING_USER -> $VPN_USER"
sudo cp "$TMP_FILE" "$RULE_FILE"
sudo chmod 440 "$RULE_FILE"
rm "$TMP_FILE"

if [ $? -eq 0 ]; then
    echo "SUKCES: Uprawnienia zostały oddelegowane do użytkownika '$VPN_USER'."
    echo "======================================================="
    exit 0
else
    echo "BŁĄD: Nie udało się skonfigurować sudoers."
    exit 1
fi
