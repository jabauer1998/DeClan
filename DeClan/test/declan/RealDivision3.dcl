CONST a = 355; b = 113;
VAR result: REAL; aAsReal: REAL; bAsReal: REAL;
BEGIN
    WriteString("A as Int: ");
    WriteInt(a);
    WriteLn();
    WriteString("B as Int: ");
    WriteInt(b);
    WriteLn();
    aAsReal := IntToReal(a);
    bAsReal := IntToReal(b);
    WriteString("A as Real: ");
    WriteReal(aAsReal);
    WriteLn();
    WriteString("B as Real: ");
    WriteReal(bAsReal);
    WriteLn();
    result := aAsReal / bAsReal;
    WriteString("Result as real: ");
    WriteReal(result);
    WriteLn()
END.