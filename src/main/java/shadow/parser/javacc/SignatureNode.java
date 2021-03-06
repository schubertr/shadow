package shadow.parser.javacc;

import java.util.HashSet;
import java.util.Set;

import shadow.typecheck.type.MethodSignature;
import shadow.typecheck.type.SingletonType;

public class SignatureNode extends SimpleNode {
	
	private boolean hasBlock = false;
	private Set<SingletonType> singletons = new HashSet<SingletonType>();
	
	public SignatureNode(int id) {
    	super(id);
    }
    
    public SignatureNode(ShadowParser sp, int id) {
    	super(sp, id);
    }
	
	private MethodSignature signature;
	
	public MethodSignature getMethodSignature()
	{
		return signature;
	}
	
	public void setMethodSignature(MethodSignature signature)
	{
		this.signature = signature;
	}
	
	public void setBlock(boolean value) {
		hasBlock = true;
	}
	
	public boolean hasBlock() {
		return hasBlock;
	}
	
	public void addSingleton(SingletonType type) {
		singletons.add(type);
	}
	
	public Set<SingletonType> getSingletons() {
		return singletons;
	}
}
