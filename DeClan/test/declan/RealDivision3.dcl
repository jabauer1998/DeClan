CONST a = 355; b = 113;
VAR result: REAL; aAsReal: REAL; bAsReal: REAL; bRealBinary: INT; aRealBinary: INT;
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
    aRealBinary := RealBinaryAsInt(aAsReal);
    bRealBinary := RealBinaryAsInt(bAsReal);
    WriteString("A as Real Int: ");
    WriteInt(aRealBinary);
    WriteLn();
    WriteString("B as Real Int: ");
    WriteInt(bRealBinary);
    WriteLn();
    result := aAsReal / bAsReal;
    WriteString("Result as real: ");
    WriteReal(result);
    WriteLn()
END.