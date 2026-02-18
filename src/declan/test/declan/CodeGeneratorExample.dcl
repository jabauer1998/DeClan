CONST b = 1; c = 8;
VAR u: INTEGER; v: INTEGER; a: INTEGER; t: INTEGER; d: INTEGER;
BEGIN
    a := 80;
    d := 40;
	t := a - b;
	u := a - c;
	v := t + u;
	a := d;
	d := v + u;
	WriteInt(d);
	WriteInt(a)
END.