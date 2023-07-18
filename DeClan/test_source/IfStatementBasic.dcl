CONST ifTrue = 1; ifFalse = 0;
BEGIN
    IF ifTrue THEN WriteInt(4) 
    ELSE WriteInt(5)
    END;

    IF ifFalse THEN WriteInt(2)
    ELSIF ifTrue THEN WriteInt(5)
    ELSE WriteInt(6)
    END;
END.