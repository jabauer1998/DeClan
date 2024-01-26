CONST i=5;
VAR result: REAL; readResult: REAL;
BEGIN
    result := IntToReal(i);
    WriteReal(result);
    readResult = ReadReal();
    IF(result = readResult)
END.