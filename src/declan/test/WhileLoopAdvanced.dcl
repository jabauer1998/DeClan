CONST MAX = 10; MIN = 0;
VAR x, y: INTEGER;
BEGIN
    x := MIN;
    WHILE (x > MAX) DO 
        WriteInt(x);
        x := x + 1
    ELSIF (x < MAX) DO
        y := MIN;
        WHILE (y < MAX) DO 
            WriteInt(y);
            y := y + 1
        END;
        WriteLn();
        WriteInt(x);
        WriteLn();
        x := x + 1
    END
END.