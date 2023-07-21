VAR i: INTEGER;
BEGIN
    i := 1;
    REPEAT 
        WriteInt(i);
        i := i + 1;
    UNTIL i >= 10;
    WriteLn()
END.