CONST r=6.5;
VAR result: INTEGER;
BEGIN
    WriteString("Real is ");
    WriteReal(r);
    WriteLn();
    result := RealToInt(r);
    WriteString("Int is: ");
    WriteInt(result);
    WriteLn()
END.

