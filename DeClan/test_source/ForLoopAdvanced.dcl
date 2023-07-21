VAR i, j: INTEGER;
BEGIN
  FOR i := 1 TO 10 DO 
    FOR j := 1 TO 10 BY 1 DO
        WriteInt(j);
    END;
    WriteLn();
    FOR j := 10 TO 0 BY -1 DO
        WriteInt(j);
    END;
    WriteLn();
    i := i + 1
  END;
END.