CONST a = 0; b = 1.2; c = -3.14; d = 42;
VAR result: BOOLEAN;
BEGIN
    result := a * b # c / d;
    WriteBool(result)
END.