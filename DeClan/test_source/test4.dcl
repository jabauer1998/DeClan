(* Hofstadter Male-Female Sequences: https://mathworld.wolfram.com/HofstadterMale-FemaleSequences.html *)
VAR n, i, result: INTEGER;
PROCEDURE M(n: INTEGER);
VAR temp: INTEGER;
VAR result: INTEGER;
BEGIN
  IF n = 0
  THEN result := 0
  ELSE
    temp := M(n - 1);
    result := F(temp);
    result := n - result
  END
  RETURN result
END M;
PROCEDURE F(n: INTEGER);
VAR temp: INTEGER;
VAR result: INTEGER;
BEGIN
  IF n = 0
  THEN result := 1
  ELSE
    temp := F(n - 1);
    result := M(temp);
    result := n - result
  END
  RETURN result
END F;
PROCEDURE Fact(n: INTEGER);
VAR temp: INTEGER;
VAR result: INTEGER;
BEGIN
  IF n = 0
  THEN result := 1
  ELSE
    temp := Fact(n - 1);
    RETURN n * temp
  END
END Fact;
BEGIN
  ReadInt(n);
  FOR i := 0 TO n DO
    result := F(i);
    WriteInt(result);
    result := Fact(i);
    WriteInt(result);
    WriteLn()
  END
END.