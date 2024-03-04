CONST a = 42;
  b = 42.0;
VAR c, d: INTEGER;
  e, f: REAL;
  g: BOOLEAN;
PROCEDURE Foo(VAR arg1: INTEGER; arg2, arg3: REAL);
  CONST h = a = b; i = 355 / 113;
  VAR j: INTEGER;
  BEGIN
    FOR j := a TO arg1 BY -20 DO
      Bar();
    END;
    WriteString("H is 255");
    RETURN h & (i > 3.14159265)
  END Foo;
PROCEDURE Bar();
  VAR k: BOOLEAN;
  BEGIN
    c := c + 1;
    IF ~g THEN g := TRUE; k := Foo(d, 0, 0) END
  END Bar;
BEGIN
  g := Foo(c, e, a * f);
  IF g THEN WriteInt(c) ELSE WriteReal(b - a) END
END.