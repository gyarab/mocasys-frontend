#!/usr/bin/env bash
OUTFILE=cssout/out.css

find sass "(" -name "*.scss" -o -name "*.sass" ")" -exec sassc "{}" ";" > $OUTFILE
find target/css/out -name "*.css" -exec cat "{}" ";" >> $OUTFILE
