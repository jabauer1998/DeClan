CONST a = 0;b = 1.2; c = -3.14 + a;
VAR result: REAL; bAndAResult: REAL;
BEGIN
    bAndAResult := b * a;
    result := c - bAndAResult;
    WriteString("Result is ");
    WriteReal(result);
    WriteLn()
END.