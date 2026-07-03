CONST x = 10; y = 2;
VAR result: INTEGER;
BEGIN
    WriteString("X is ");
    WriteInt(x);
    WriteLn();
    WriteString("Y is ");
    WriteInt(y);
    WriteLn();
    result := x MOD y;
    WriteString("Result is ");
    WriteInt(result);
    WriteLn()
END.