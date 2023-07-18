VAR i: INTEGER;
BEGIN
  FOR i := 1 TO 10 DO 
    WriteInt(i);
    i := i + 1
  END;
  WriteLn();
END.