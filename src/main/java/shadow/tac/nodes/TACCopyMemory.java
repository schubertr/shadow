package shadow.tac.nodes;

import shadow.parser.javacc.ShadowException;
import shadow.tac.TACVisitor;
import shadow.typecheck.type.SequenceType;
import shadow.typecheck.type.Type;

/**
 * Instruction to perform a memcopy from source memory to destination memory.
 * 
 * @author Barry Wittman
 */

public class TACCopyMemory extends TACOperand
{		
	TACOperand destination;
	TACOperand source;
	TACOperand size;
	
	public TACCopyMemory(TACNode node, TACOperand destination, TACOperand source, TACOperand size)
	{
		super(node);				
		//checks can remove references
		this.destination = check(destination, destination);
		this.source = check(source, source);
		this.size = size;
	}
	
	
	@Override
	public Type getType() {
		return source.getType();
	}

	@Override
	public int getNumOperands() {		
		return 3;
	}
	
	public TACOperand getSource()
	{
		return source;
	}
	
	public TACOperand getDestination()
	{
		return destination;
	}
	
	
	public TACOperand getSize()
	{
		return size;
	}

	@Override
	public TACOperand getOperand(int num) {
		if( num == 0 )
			return destination;
		if( num == 1)
			return source;
		if( num == 2)
			return size;

		throw new IndexOutOfBoundsException("" + num);
	}

	@Override
	public void accept(TACVisitor visitor) throws ShadowException {
		visitor.visit(this);
	}

}
