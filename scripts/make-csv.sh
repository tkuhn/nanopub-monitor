#!/bin/bash
#
# Usage:
#
# $ scripts/make-csv.sh path/to/nanopub-monitor.log > nanopub-monitor.csv
#

cat $1 \
  | grep " ch.tkuhn.nanopub.monitor.ServerData - Test result: " \
  | sed -r 's/^\[INFO\] ([^ ]*) .* Test result: ([^ ]*) ([^ ]*) ([^ ]*)( ([^ ]*))?$/\1,\2,\3,\4,\6/'
