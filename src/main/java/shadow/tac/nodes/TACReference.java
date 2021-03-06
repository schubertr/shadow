package shadow.tac.nodes;

import shadow.typecheck.type.ModifiedType;

public abstract class TACReference extends TACOperand
{
	public TACReference(TACNode node)
	{
		super(node);
	}

	public ModifiedType getGetType()
	{
		return this;
	}
	public ModifiedType getSetType()
	{
		return this;
	}

	@Override
	protected TACOperand checkVirtual(ModifiedType type, TACNode node)
	{
		if (type instanceof TACReference)
			type = ((TACReference)type).getGetType();
		return new TACLoad(node, this).checkVirtual(type, node);
	}
}
