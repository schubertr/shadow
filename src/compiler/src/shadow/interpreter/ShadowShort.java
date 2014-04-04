package shadow.interpreter;

import shadow.parser.javacc.ShadowException;
import shadow.typecheck.type.Modifiers;
import shadow.typecheck.type.Type;

public class ShadowShort extends ShadowValue
{
	private short value;
	public ShadowShort(short value)
	{
		super(Modifiers.IMMUTABLE);
		this.value = value;
	}
	@Override
	public Type getType()
	{
		return Type.SHORT;
	}

	public short getValue()
	{
		return this.value;
	}

	@Override
	protected ShadowValue cast(Type type) throws ShadowException
	{
		if (type.typeEquals(Type.BYTE))
			return new ShadowByte((byte)getValue());
		if (type.typeEquals(Type.SHORT))
			return new ShadowShort((short)getValue());
		if (type.typeEquals(Type.INT))
			return new ShadowInt((int)getValue());
		if (type.typeEquals(Type.LONG))
			return new ShadowLong((long)getValue());
		if (type.typeEquals(Type.UBYTE))
			return new ShadowUByte((byte)getValue());
		if (type.typeEquals(Type.USHORT))
			return new ShadowUShort((short)getValue());
		if (type.typeEquals(Type.UINT))
			return new ShadowUInt((int)getValue());
		if (type.typeEquals(Type.ULONG))
			return new ShadowULong((long)getValue());
		return this;
	}
	@Override
	public ShadowValue copy() throws ShadowException
	{
		return new ShadowShort(getValue());
	}
	@Override
	public String toString()
	{
		return Short.toString(getValue());
	}
}
