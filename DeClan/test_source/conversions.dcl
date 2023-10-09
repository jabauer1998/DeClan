VAR x : INTEGER; n: INTEGER; n2: REAL; x2: REAL;
PROCEDURE p(n: INTEGER; x: REAL);
  VAR m : INTEGER;
  BEGIN
    m := Round(x + n);
    RETURN m
  END p;
BEGIN
  n := 1;
  n2 := 1.0;
  x := 2;
  x2 := 2.0;
  WriteInt(n);
  WriteReal(n2);
  WriteReal(x2);
  WriteLn();
  WriteReal(n / x);
  WriteInt((n + 5) * (n + 6));
  WriteReal((x + 4) * (x + 5.));
  WriteLn();
  x := p(n, 3.1415);
  WriteReal(x2);
  WriteLn();
END.