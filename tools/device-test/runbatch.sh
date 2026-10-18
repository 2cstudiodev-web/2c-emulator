#!/usr/bin/env bash
cd "$(dirname "$0")"
while IFS='|' read -r ids title wait; do
  [ -z "$ids" ] && continue
  python -I -X utf8 batchtest.py "$ids" "$title" "$wait" < /dev/null
done < "$1"
echo BATCH_DONE
