package shadow.typecheck.type;

import java.util.ArrayList;
import java.util.List;

public class TypeParameter extends Type
{
	private List<Type> bounds = new ArrayList<Type>();

	public TypeParameter(String typeName)
	{
		super(typeName, 0, null, Kind.TYPE_PARAMETER );
	}
	
	public void addBound(Type type) {
		bounds.add(type);
	}
	
	public List<Type> getBounds()
	{
		return bounds;
	}
}