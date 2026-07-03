CONST r1=30.32;r2=2.0;
VAR result: REAL;
BEGIN
    result := r1 * r2;
    WriteString("R1 is ");
    WriteReal(r1);
    WriteLn();
    WriteString("R2 is ");
    WriteReal(r2);
    WriteLn();
    WriteString("Result is ");
    WriteReal(result);
    WriteLn()
END.


