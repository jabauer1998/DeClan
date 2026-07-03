CONST r1=3.12145;r2=3.12145;
VAR result: REAL;
BEGIN
    WriteString("R1 is ");
    WriteReal(r1);
    WriteLn();
    WriteString("R2 is ");
    WriteReal(r2);
    WriteLn();
    result := r1 * r2;
    WriteString("Result is ");
    WriteReal(result);
    WriteLn()
END.

