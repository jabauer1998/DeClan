CONST a = 0;b = 1.2; c = -3.14 + a;
VAR result: REAL;
BEGIN
    WriteString("A is ");
    WriteInt(a);
    WriteLn();
    WriteString("B is ");
    WriteReal(b);
    WriteLn();
    WriteString("C is ");
    WriteReal(c);
    WriteLn();
    result := -c - b*a;
    WriteString("Result is ");
    WriteReal(result);
    WriteLn()
END.