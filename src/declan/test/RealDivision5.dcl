CONST a = 6 * (6 + 1); b = a MOD 10;
VAR result: REAL;
BEGIN
    WriteString("A is ");
    WriteInt(a);
    WriteLn();
    WriteString("B is ");
    WriteInt(b);
    WriteLn();
    WriteString("Result is ");
    result := a / b;
    WriteReal(result);
    WriteLn();
END.

