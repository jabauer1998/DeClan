package declan.middleware.interfere;

public class Color implements ColorType {
	private int color;
	
	public Color(int item) {
		this.color = item;
	}
	
	public int literalColor() {
		return color;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Color) {
			Color col = (Color)obj;
			return col.color == color;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return Integer.toString(color);
	}

	@Override
	public boolean matches(Object obj) {
		return obj instanceof Color;
	}
}
