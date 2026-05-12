#!/bin/bash
set -e

cat > /tmp/reg.json <<'JSON'
{"username":"testclient1","password":"password123","email":"testclient1@example.com","role":"CLIENT","nombre":"Test Client","telefono":"3001112222"}
JSON

curl -s -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d @/tmp/reg.json -o /tmp/reg_response.json -w "\nHTTP_STATUS:%{http_code}\n"

cat /tmp/reg_response.json

cat > /tmp/payload.json <<'JSON'
{"direccion_origen":"Calle 1","direccion_destino":"Cra 2","lat_origen":7.89,"lon_origen":-72.5,"lat_destino":7.90,"lon_destino":-72.49,"tarifa":400}
JSON

# extract token (jq preferred)
if command -v jq >/dev/null 2>&1; then
  TOKEN=$(jq -r '.token' /tmp/reg_response.json)
else
  TOKEN=$(grep -oP '(?<="token":")[^"]+' /tmp/reg_response.json || true)
fi

echo "TOKEN:$TOKEN"

curl -s -X POST http://localhost:8080/api/orders/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${TOKEN}" \
  -d @/tmp/payload.json -o /tmp/create_resp.json -w "\nHTTP_STATUS:%{http_code}\n"

cat /tmp/create_resp.json

# Run SELECT (psql must be installed in WSL)
export PGPASSWORD=npg_wQDiy25hZqVn
psql -h ep-billowing-waterfall-amep92kg-pooler.c-5.us-east-1.aws.neon.tech -U neondb_owner -d neondb -c "SELECT id_servicio, tarifa, oferta_actual, fecha_solicitud FROM servicio ORDER BY fecha_solicitud DESC LIMIT 5;"
