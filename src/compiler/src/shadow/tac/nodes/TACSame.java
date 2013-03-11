package shadow.tac.nodes;

import shadow.parser.javacc.ShadowException;
import shadow.tac.TACVisitor;
import shadow.tac.nodes.TACBinary.Operation;
import shadow.typecheck.type.Type;

public class TACSame extends TACOperand
{
	private TACOperand first, second;
	public TACSame(TACOperand firstOperand, TACOperand secondOperand)
	{
		this(null, firstOperand, secondOperand);
	}
	public TACSame(TACNode node, TACOperand firstOperand,
			TACOperand secondOperand)
	{
		super(node);
		firstOperand = check(firstOperand);
		secondOperand = check(secondOperand);
		if (firstOperand.getType().isSubtype(secondOperand.getType()))
			firstOperand = check(firstOperand, secondOperand.getType());
		else
			secondOperand = check(secondOperand, firstOperand.getType());
		first = firstOperand;
		second = secondOperand;
	}

	public TACOperand getFirst()
	{
		return first;
	}
	public TACOperand getSecond()
	{
		return second;
	}

	@Override
	public Type getType()
	{
		return Type.BOOLEAN;
	}
	@Override
	public int getNumOperands()
	{
		return 2;
	}
	@Override
	public TACOperand getOperand(int num)
	{
		if (num == 0)
			return first;
		if (num == 1)
			return second;
		throw new IndexOutOfBoundsException();
	}

	@Override
	public void accept(TACVisitor visitor) throws ShadowException
	{
		visitor.visit(this);
	}
}
