CONST six = 6; seven = 7;
VAR answer : INTEGER;
PROCEDURE gcd(a, b: INTEGER);
  BEGIN
    WHILE a # b DO
      IF a > b THEN a := a - b ELSE b := b - a END
    END;
    RETURN a
  END gcd;
BEGIN
  answer := gcd(six, seven);
  WriteString("Answer is ");
  WriteInt(answer);
  WriteLn();
  WriteInt(six);
  WriteInt(seven);
  WriteInt(answer);
  answer := six * seven * answer;
  WriteString("Answer is ");
  WriteInt(answer);
  WriteReal(answer * 1.);
  WriteLn()
END. (* Don't forget the ending period! *)