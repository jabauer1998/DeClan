CONST a = 355; b = 113;
VAR result: REAL; aAsReal: REAL; bAsReal: REAL; bRealBinary: INT; aRealBinary: INT;
BEGIN
    aAsReal := IntToReal(a);
    bAsReal := IntToReal(b);
    result := aAsReal / bAsReal;
    WriteString("Result as real: ");
    WriteReal(result);
    WriteLn()
END.