VAR x : REAL; n: INTEGER;
PROCEDURE p(n: INTEGER; x: REAL);
  VAR m : INTEGER;
  BEGIN
    m := Round(x + n);
    RETURN m
  END p;
BEGIN
  n := 1;
  x := 2;
  WriteInt(n);
  WriteReal(n);
  WriteReal(x);
  WriteLn();
  WriteReal(n / x);
  WriteInt((n + 5) * (n + 6));
  WriteReal((x + 4) * (x + 5.));
  WriteLn();
  x := p(n, 3.1415);
  WriteReal(x);
  WriteLn();
END.