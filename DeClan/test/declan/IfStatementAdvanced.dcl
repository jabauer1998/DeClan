CONST ifTrue = TRUE; ifFalse = FALSE;
BEGIN
    IF ifTrue THEN 
        IF ifFalse THEN 
            WriteInt(5) 
        ELSE 
            WriteInt(6) 
        END
    ELSE 
        IF ifFalse THEN
            WriteInt(7)
        ELSE
            WriteInt(8)
        END
    END
END.