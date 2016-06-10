# just words

IN="just words"

OUT=$(indented "$IN")
assert-equals " just words" "$OUT"

# multiline

IN="line1
line2
line three"

OUT=$(indented "$IN")
assert-equals " line1
 line2
 line three" "$OUT"

# indented multiline

IN=" line1
 line2"
OUT=$(indented "$IN")
assert-equals "  line1
  line2" "$OUT"
