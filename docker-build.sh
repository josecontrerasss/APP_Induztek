#!/bin/sh
set -e
cd /app
if [ -n "$MAPS_API_KEY" ]; then
  printf 'MAPS_API_KEY=%s\n' "$MAPS_API_KEY" > local.properties
fi
printf 'sdk.dir=%s\n' "$ANDROID_HOME" >> local.properties
TASK="${1:-assembleDebug}"
./gradlew --no-daemon "$TASK"
mkdir -p /out
find app/build/outputs -name "*.apk" -exec cp {} /out/ \;
ls -la /out
