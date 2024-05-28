CONST a = 0; e = TRUE; x = 1.0;
VAR result: BOOLEAN;
BEGIN
    WriteString("e is ");
    WriteBool(e);
    WriteLn();
    result := ~e OR (x >= a);
    WriteBool(result)
END.