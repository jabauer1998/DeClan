CONST a = 42;
  b = 42.0;
VAR c, d: INTEGER;
  e, f: REAL;
  g: BOOLEAN;
PROCEDURE Foo(VAR arg1: INTEGER; arg2, arg3: REAL);
  CONST h = a = b; i = 355 / 113;
  VAR j: INTEGER; z: BOOLEAN;
  BEGIN
  	WriteString("Foo Running");
  	WriteLn();
    FOR j := a TO arg1 BY -20 DO
      Bar();
    END;
    z := h & (i > 3.14159265);
    RETURN z
  END Foo;
PROCEDURE Bar();
  VAR k: BOOLEAN;
  BEGIN
    WriteString("Bar Running");
    WriteLn();
    c := c + 1;
    WriteString("C is ");
    WriteInt(c);
    WriteLn();
    IF ~g THEN 
      g := TRUE;
      WriteString("Doing foo with arg1=");
      WriteInt(d);
      WriteLn();
      k := Foo(d, 0, 0) 
    END
  END Bar;
BEGIN
  g := Foo(c, e, a * f);
  IF g THEN
  	WriteString("Writing C: ");
  	WriteInt(c);
  	WriteLn()
  ELSE
    WriteString("Writing B - A: ");
  	WriteReal(b - a);
  	WriteLn()
  END
END.