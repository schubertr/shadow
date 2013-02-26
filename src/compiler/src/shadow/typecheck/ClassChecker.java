package shadow.typecheck;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import shadow.AST.ASTUtils;
import shadow.AST.ASTWalker.WalkType;
import shadow.parser.javacc.ASTAdditiveExpression;
import shadow.parser.javacc.ASTAllocation;
import shadow.parser.javacc.ASTArgumentList;
import shadow.parser.javacc.ASTArguments;
import shadow.parser.javacc.ASTArrayCreate;
import shadow.parser.javacc.ASTArrayInitializer;
import shadow.parser.javacc.ASTAssertStatement;
import shadow.parser.javacc.ASTAssignmentOperator;
import shadow.parser.javacc.ASTBitwiseAndExpression;
import shadow.parser.javacc.ASTBitwiseExclusiveOrExpression;
import shadow.parser.javacc.ASTBitwiseOrExpression;
import shadow.parser.javacc.ASTBlock;
import shadow.parser.javacc.ASTBrackets;
import shadow.parser.javacc.ASTBreakStatement;
import shadow.parser.javacc.ASTCastExpression;
import shadow.parser.javacc.ASTCheckExpression;
import shadow.parser.javacc.ASTClassOrInterfaceBody;
import shadow.parser.javacc.ASTClassOrInterfaceType;
import shadow.parser.javacc.ASTClassOrInterfaceTypeSuffix;
import shadow.parser.javacc.ASTCoalesceExpression;
import shadow.parser.javacc.ASTCompilationUnit;
import shadow.parser.javacc.ASTConcatenationExpression;
import shadow.parser.javacc.ASTConditionalAndExpression;
import shadow.parser.javacc.ASTConditionalExclusiveOrExpression;
import shadow.parser.javacc.ASTConditionalExpression;
import shadow.parser.javacc.ASTConditionalOrExpression;
import shadow.parser.javacc.ASTCreate;
import shadow.parser.javacc.ASTCreateDeclaration;
import shadow.parser.javacc.ASTDestroy;
import shadow.parser.javacc.ASTDestroyDeclaration;
import shadow.parser.javacc.ASTDoStatement;
import shadow.parser.javacc.ASTEqualityExpression;
import shadow.parser.javacc.ASTExplicitCreateInvocation;
import shadow.parser.javacc.ASTExpression;
import shadow.parser.javacc.ASTExtendsList;
import shadow.parser.javacc.ASTFieldDeclaration;
import shadow.parser.javacc.ASTForInit;
import shadow.parser.javacc.ASTForStatement;
import shadow.parser.javacc.ASTForeachStatement;
import shadow.parser.javacc.ASTFormalParameter;
import shadow.parser.javacc.ASTFormalParameters;
import shadow.parser.javacc.ASTFunctionType;
import shadow.parser.javacc.ASTIfStatement;
import shadow.parser.javacc.ASTImplementsList;
import shadow.parser.javacc.ASTImportDeclaration;
import shadow.parser.javacc.ASTInstance;
import shadow.parser.javacc.ASTIsExpression;
import shadow.parser.javacc.ASTLiteral;
import shadow.parser.javacc.ASTLocalMethodDeclaration;
import shadow.parser.javacc.ASTLocalVariableDeclaration;
import shadow.parser.javacc.ASTMethod;
import shadow.parser.javacc.ASTMethodCall;
import shadow.parser.javacc.ASTMethodDeclaration;
import shadow.parser.javacc.ASTMethodDeclarator;
import shadow.parser.javacc.ASTMultiplicativeExpression;
import shadow.parser.javacc.ASTName;
import shadow.parser.javacc.ASTPrimaryExpression;
import shadow.parser.javacc.ASTPrimaryPrefix;
import shadow.parser.javacc.ASTPrimarySuffix;
import shadow.parser.javacc.ASTPrimitiveType;
import shadow.parser.javacc.ASTProperty;
import shadow.parser.javacc.ASTQualifiedKeyword;
import shadow.parser.javacc.ASTReferenceType;
import shadow.parser.javacc.ASTRelationalExpression;
import shadow.parser.javacc.ASTResultType;
import shadow.parser.javacc.ASTResultTypes;
import shadow.parser.javacc.ASTReturnStatement;
import shadow.parser.javacc.ASTRightRotate;
import shadow.parser.javacc.ASTRightShift;
import shadow.parser.javacc.ASTRotateExpression;
import shadow.parser.javacc.ASTScopeSpecifier;
import shadow.parser.javacc.ASTSequence;
import shadow.parser.javacc.ASTSequenceAssignment;
import shadow.parser.javacc.ASTShiftExpression;
import shadow.parser.javacc.ASTStatementExpression;
import shadow.parser.javacc.ASTSubscript;
import shadow.parser.javacc.ASTSwitchLabel;
import shadow.parser.javacc.ASTSwitchStatement;
import shadow.parser.javacc.ASTTryStatement;
import shadow.parser.javacc.ASTType;
import shadow.parser.javacc.ASTTypeArguments;
import shadow.parser.javacc.ASTTypeBound;
import shadow.parser.javacc.ASTTypeParameter;
import shadow.parser.javacc.ASTUnaryExpression;
import shadow.parser.javacc.ASTUnaryExpressionNotPlusMinus;
import shadow.parser.javacc.ASTUnaryToString;
import shadow.parser.javacc.ASTUnqualifiedName;
import shadow.parser.javacc.ASTVariableInitializer;
import shadow.parser.javacc.ASTWhileStatement;
import shadow.parser.javacc.Node;
import shadow.parser.javacc.ShadowException;
import shadow.parser.javacc.SignatureNode;
import shadow.parser.javacc.SimpleNode;
import shadow.typecheck.type.ArrayType;
import shadow.typecheck.type.ClassType;
import shadow.typecheck.type.ExceptionType;
import shadow.typecheck.type.InterfaceType;
import shadow.typecheck.type.MethodSignature;
import shadow.typecheck.type.MethodType;
import shadow.typecheck.type.ModifiedType;
import shadow.typecheck.type.Modifiers;
import shadow.typecheck.type.PropertyType;
import shadow.typecheck.type.SequenceType;
import shadow.typecheck.type.SingletonType;
import shadow.typecheck.type.Type;
import shadow.typecheck.type.TypeParameter;
import shadow.typecheck.type.UnboundMethodType;


//no automatic promotion for bitwise operators

public class ClassChecker extends BaseChecker {	

	protected LinkedList<Node> curPrefix = null; 	/** Stack for current prefix (needed for arbitrarily long chains of expressions) */
	protected LinkedList<Node> labels = null; 	/** Stack of labels for labeled break statements */
	protected LinkedList<ASTTryStatement> tryBlocks = null; /** Stack of try blocks currently nested inside */	
	protected LinkedList<HashMap<String, ModifiedType>> symbolTable; /** List of scopes with a hash of symbols & types for each scope */
	protected LinkedList<Node> scopeMethods; /** Keeps track of the method associated with each scope (sometimes null) */
	
	public ClassChecker(boolean debug, HashMap<Package, HashMap<String, Type>> typeTable, List<String> importList, Package packageTree ) {
		super(debug, typeTable, importList, packageTree );		
		symbolTable = new LinkedList<HashMap<String, ModifiedType>>();
		curPrefix = new LinkedList<Node>();
		labels = new LinkedList<Node>();	
		tryBlocks = new LinkedList<ASTTryStatement>();
		scopeMethods = new LinkedList<Node>();
	}
	
	public Object visitMethod( SignatureNode node, Boolean secondVisit  )
	{
		if(!secondVisit)
		{			
			if( node instanceof ASTLocalMethodDeclaration )
			{				
				MethodSignature signature = new MethodSignature( currentType, node.jjtGetChild(0).getImage(), node.getModifiers(), node);
				node.setMethodSignature(signature);
				MethodType methodType = signature.getMethodType();
				node.setType(methodType);
				node.setEnclosingType(currentType);
				//what modifiers (if any) are allowed for a local method declaration?
			}			
			currentMethod.addFirst(node);
		}
		else
			currentMethod.removeFirst();
		
		createScope(secondVisit); 
		
		return WalkType.POST_CHILDREN;
	}
	
	private void createScope(Boolean secondVisit) {
		// we have a new scope, so we need a new HashMap in the linked list
		if(secondVisit)			
		{
			symbolTable.removeFirst();
			scopeMethods.removeFirst();
		}
		else
		{
			symbolTable.addFirst(new HashMap<String, ModifiedType>());
			
			if( currentMethod.isEmpty() )
				scopeMethods.addFirst(null);
			else
				scopeMethods.addFirst(currentMethod.getFirst());
		}
	}
	
	//Important!  Set the current type on entering the body, not the declaration, otherwise extends and imports are improperly checked with the wrong outer class
	public Object visit(ASTClassOrInterfaceBody node, Boolean secondVisit) throws ShadowException {		
		if( secondVisit )
			currentType = currentType.getOuter();		
		else
			currentType = node.jjtGetParent().getType(); //get type from declaration
			
		return WalkType.POST_CHILDREN;
	}	
	
	
	public Object visit(ASTSwitchStatement node, Boolean secondVisit) throws ShadowException {
		if(!secondVisit)
			return WalkType.POST_CHILDREN;
		Type type = node.jjtGetChild(0).getType();
		if(!type.isIntegral() && !type.isString())//TODO allow enum types
			addError(node,Error.INVL_TYP, "Found type " + type + ", but integral or string type required for switch.");
		for(int i=1;i<node.jjtGetNumChildren();++i) {
			Node childNode = node.jjtGetChild(i);
			if(childNode.getClass() == ASTSwitchLabel.class) {
				if(childNode.getType() != null){ //default label should have null type 
					if(!childNode.getType().isSubtype(type)) {
						addError(childNode,Error.TYPE_MIS,"Label type " + childNode.getType() + " does not match switch type " + type + ".");
					}
				}
			}
		}
		return WalkType.POST_CHILDREN;
	}
	
	public Object visit(ASTSwitchLabel node, Boolean secondVisit) throws ShadowException {
		pushUpType(node, secondVisit);
		
		if( secondVisit && node.jjtGetNumChildren() > 0 && !node.getModifiers().isConstant() )
			addError(node, Error.INVL_MOD, "Label must have constant value");			
		
		return WalkType.POST_CHILDREN;
	}
	
	public Object visit(ASTBlock node, Boolean secondVisit) throws ShadowException {
		createScope(secondVisit);
		return WalkType.POST_CHILDREN;
	}
	
	public Object visit(ASTMethodDeclaration node, Boolean secondVisit) throws ShadowException {
		return visitMethod( node, secondVisit );
	}
	
	
	
	public Object visit(ASTLocalMethodDeclaration node, Boolean secondVisit) throws ShadowException {
		if( !secondVisit )
		{	
			Node declaration = node.jjtGetChild(0);			
			addSymbol(declaration.getImage(), node);
		}		
		return visitMethod( node, secondVisit );
	}
	
	
	public Object visit(ASTFormalParameters node, Boolean secondVisit) throws ShadowException
	/* Note:  The ASTFormalParameters node can only be found in methods (including creates)
	 * However, the ASTFormalParameter node can also be found in a try-catch block
	 * This is why we add parameters to the symbol table in ASTFormalParameters and not ASTFormalParameter
	 */
	{
		
		Node parent = node.jjtGetParent();
		Node grandparent = parent.jjtGetParent();
		
		if( parent instanceof ASTCreateDeclaration || grandparent instanceof ASTMethodDeclaration )
		{
			MethodType methodType = currentMethod.getFirst().getMethodSignature().getMethodType();
			for(String symbol : methodType.getParameterNames())
				addSymbol( symbol, methodType.getParameterType(symbol));
			
			return WalkType.NO_CHILDREN;
		}		
		
		if(!secondVisit)
			return WalkType.POST_CHILDREN;	
		
		if( grandparent instanceof ASTLocalMethodDeclaration )
		{
			MethodSignature signature = null;
			signature = ((SignatureNode)grandparent).getMethodSignature();
			for(int i = 0; i < node.jjtGetNumChildren(); ++i)
			{
				Node parameter = node.jjtGetChild(i);
				//child 0 is Modifiers
				//child 1 is type
				String paramSymbol = parameter.jjtGetChild(2).getImage();			
				
				//child 0 is Modifiers				
				// get the name of the parameter							
				// check if it's already in the set of parameter names
				if(signature.containsParam(paramSymbol))
					addError(parameter.jjtGetChild(1), Error.MULT_SYM, "In parameter names");					
							
				//child 1 is type
				Node child = parameter.jjtGetChild(1);			
				
				// get the type of the parameter
				parameter.setType(child.getType());				
				
				// make sure this type is in the type table
				if(parameter.getType() == null)		
				{
					addError(child, Error.UNDEF_TYP);
					parameter.setType(Type.UNKNOWN);
				}
					
				// add the parameter type to the signature
				signature.addParameter(paramSymbol, parameter);
				addSymbol( paramSymbol, parameter );			
			}
		}

		return WalkType.POST_CHILDREN;
	}
	
	public void addSymbol( String name, ModifiedType node )
	{
		if( symbolTable.get(0).containsKey( name ) ) //we only look at current scope
			addError(Error.MULT_SYM, name);
		else if( symbolTable.size() == 0 )
			addError(Error.INVL_TYP, "No valid scope for variable declaration");
		else
			symbolTable.getFirst().put(name, node);  //uses node for modifiers
	}
	
	
	public ModifiedType findSymbol( String name )
	{
		ModifiedType node = null;
		for( int i = 0; i < symbolTable.size(); i++ )
		{
			HashMap<String,ModifiedType> map = symbolTable.get(i);		
			if( (node = map.get(name)) != null )
			{
				Node method = scopeMethods.get(i);
				if( method != null && method != currentMethod.getFirst() )
				{
					//situation where we are pulling a variable from an outer method
					//it must be final!
					//local method declarations don't count
					
					//add a check to deal with this, even without final
					
					//if( !(node instanceof ASTLocalMethodDeclaration) && !node.getModifiers().isFinal() )
					//	addError(Error.INVL_TYP, "Variables accessed by local methods from outer methods must be marked final");
				}
				return node;
			}
		}
		
		
		return node;
	}
	
	public Object visit(ASTFormalParameter node, Boolean secondVisit) throws ShadowException
	{
		if(secondVisit)
		{	
			//child 0 is Modifiers
			Type type = node.jjtGetChild(1).getType();
			node.setType( type );
			if( node.getModifiers().isNullable() && type.isPrimitive() )
				addError(node, Error.TYPE_MIS, "Cannot mark primitive type " + type + " as nullable");			
		}
	
		return WalkType.POST_CHILDREN;
	}
	
	
	public Object visit(ASTCreateDeclaration node, Boolean secondVisit) throws ShadowException {
		if( secondVisit )
		{
			if( currentType instanceof ClassType)
			{
				ClassType classType = (ClassType) currentType;
				ClassType parentType = classType.getExtendType();
				
				if( parentType != null )
				{	
					if( !node.hasExplicitInvocation() ) 
						//only worry if there is no explicit invocation
						//explicit invocations are handled separately
					{
						boolean foundDefault = false;
						SequenceType emptyParameters = new SequenceType();
						for( MethodSignature method : parentType.getMethods("create") )
						{
							if( method.matches(emptyParameters) )
							{
								foundDefault = true;
								break;
							}
						}
					
						if( !foundDefault )
							addError(node, Error.INVL_TYP, "No explicit create invocation and parent class does not implement the default create");
					}					
				}
			}
		}		
		
		return visitMethod( node, secondVisit );
	}
	
	public Object visit(ASTDestroyDeclaration node, Boolean secondVisit) throws ShadowException {
		return visitMethod( node, secondVisit );
	}
	
	public Object visit(ASTReferenceType node, Boolean secondVisit) throws ShadowException
	{
		if(secondVisit)			
		{
			Node child =  node.jjtGetChild(0);			
			Type type = child.getType();			
			List<Integer> dimensions = node.getArrayDimensions();
			
			if( dimensions.size() == 0 )
				node.setType(type);
			else
			{
				ArrayType arrayType = new ArrayType(type, dimensions);
				((ClassType)currentType).addReferencedType(arrayType);
				node.setType(arrayType);
			}
		}
		
		return WalkType.POST_CHILDREN;
	}
	
	private void processDeclaration( Node node )
	{
		boolean isLocal = (node instanceof ASTLocalVariableDeclaration);		
		int start = 0;				
		
		if( node.jjtGetChild(start) instanceof ASTType ) //skip type declaration
			start++;	
		
		Type type = node.getType();
		//type is set for local declarations immediately previously and for fields in the field and method checker
		
		if(type == null)
		{			
			addError(node.jjtGetChild(start), Error.UNDEF_TYP, node.jjtGetChild(start).jjtGetChild(0).getImage());
			return;
		}		
		
		if( type.isPrimitive() && node.getModifiers().isNullable() )		
			addError(node.jjtGetChild(start), Error.TYPE_MIS, "Cannot declare primitive type " + type + " as nullable");				
			
		// go through and add the variables
		for(int i = start; i < node.jjtGetNumChildren(); ++i)
		{
			Node declaration = node.jjtGetChild(i);
			Node identifier = declaration.jjtGetChild(0);
			
			if(declaration.jjtGetNumChildren() == 2) // check for initializer
			{
				Node initializer = declaration.jjtGetChild(1); 					
				Type initializerType = initializer.getType();
				
				if(!initializerType.isSubtype(type)) 
					addError(initializer, Error.TYPE_MIS, "Cannot assign " + initializerType + " to " + type);
				else if( !node.getModifiers().isNullable() && initializer.getModifiers().isNullable())
					addError(identifier, Error.TYPE_MIS, "Cannot assign a nullable value to non-nullable variable " + identifier.getImage() );				
			}
			else
			{
				if( !node.getModifiers().isNullable() && !type.isPrimitive() )
					addError(declaration, Error.TYPE_MIS, "Non-nullable variable " + declaration + "does not have initializer");		
			}
			
			declaration.setType(type);
			declaration.setModifiers(node.getModifiers());
			identifier.setType(type);
			identifier.setModifiers(node.getModifiers());
			
			if( isLocal ) // add the symbol to the scope table				
				addSymbol( identifier.getImage(), node);
		}		
	}
	
	
	public Object visit(ASTLocalVariableDeclaration node, Boolean secondVisit) throws ShadowException {		
		if(secondVisit)
		{	
			Type type = null;
			Node child = node.jjtGetChild(0); 
			
			if( child instanceof ASTType )
			{
				type = child.getType();
			}
			else //auto type
			{	
				for( int i = 0; i < node.jjtGetNumChildren(); i++ )
				{
					Node declaration = node.jjtGetChild(i);
					if( declaration.jjtGetNumChildren() == 2 )
					{
						type = declaration.jjtGetChild(1).getType();
						break;
					}
				}
				
				if( type == null )
				{
					addError(node, Error.UNDEF_TYP, "Variable declared auto has no initializer to infer type from");
					type = Type.UNKNOWN;
				}
			}			
			
			node.setType(type);
			
			processDeclaration( node );
		}

		return WalkType.POST_CHILDREN;
	}

	public Object visit(ASTFieldDeclaration node, Boolean secondVisit) throws ShadowException {		
		if(secondVisit)
			processDeclaration( node );							  		

		return WalkType.POST_CHILDREN;
	}
	
	
	
	
	public boolean setTypeFromContext( Node node, String name, Type context, boolean directAccess  ) //directAccess is true if there is no prefix and false if there is
	{
		if( context instanceof TypeParameter )
		{
			TypeParameter typeParameter = (TypeParameter) context;
			for( Type type : typeParameter.getBounds() )
				if( setTypeFromContext( node, name, type, directAccess ) )
					return true;
			
			return setTypeFromContext( node, name, Type.OBJECT, directAccess );			
		}
		/*else if( context instanceof InstantiatedType )
		{
			InstantiatedType instantiatedType = (InstantiatedType)context;
			return setTypeFromContext( node, name, instantiatedType.getInstantiatedType(), directAccess );			
		}*/
		else if( context instanceof InterfaceType )
		{			
			InterfaceType interfaceType = (InterfaceType)context;
			
			if( interfaceType.recursivelyContainsMethod( name ) )
			{
				node.setType( new UnboundMethodType( name, interfaceType ) );				
				return true;
			}			
		}		
		else if( context instanceof ClassType  )
		{
			ClassType classType;
			
			if( context instanceof ArrayType )
				classType = Type.ARRAY;
			else
				classType = (ClassType)context;
					
			if(classType.recursivelyContainsField(name))
			{
				Node field = classType.recursivelyGetField(name);
				node.setType(field.getType());
				node.setModifiers(field.getModifiers());
				node.addModifier(Modifiers.ASSIGNABLE);
				node.addModifier(Modifiers.FIELD);
				
				if( field.getModifiers().isPrivate() && currentType != classType   )
					addError(node, Error.INVL_MOD, "Cannot access private variable " + field.getImage());						
				
				return true;			
			}
				
			if( classType.recursivelyContainsMethod(name))
			{
				node.setType( new UnboundMethodType( name, classType ) );				
				return true;
			}
			
			if( classType.recursivelyContainsInnerClass(name))
			{
				Type innerClass = classType.recursivelyGetInnerClass(name);
				node.setType(innerClass);
				node.setModifiers(Modifiers.TYPE_NAME);
				return true;
			}
		}

		return false;
	}
	
	
	public boolean setTypeFromName( Node node, String name ) 
	{			
		// next go through the scopes trying to find the variable
		ModifiedType declaration = findSymbol( name );
		
		if( declaration != null ) 
		{
			node.setType(declaration.getType());
			node.setModifiers(declaration.getModifiers());
			node.addModifier(Modifiers.ASSIGNABLE);
			return true;
		}
			
		// now check the parameters of the methods
		MethodType methodType = null;
		
		for( Node method : currentMethod)
		{
			methodType = (MethodType)method.getType();
		
			if(methodType != null && methodType.containsParam(name))
			{	
				node.setType(methodType.getParameterType(name).getType());
				node.setModifiers(methodType.getParameterType(name).getModifiers());
				node.addModifier(Modifiers.ASSIGNABLE);	//is this right?  Shouldn't all method parameters be unassignable?		
				return true;
			}
		}
				
		// check to see if it's a field or a method			
		if( setTypeFromContext( node, name, currentType, true ) )
			return true;
				
		//is it a type?
		Type type = lookupType( name );		
				
		if(type != null)
		{
			((ClassType)currentType).addReferencedType(type);
			node.setType(type);
			node.addModifier(Modifiers.TYPE_NAME);
			return true;
		}
		
		return false;
	}
	
	
	
	public Object visit(ASTName node, Boolean secondVisit) throws ShadowException 
	{	
		Type type = lookupType( node.getImage() );
				
		if( type == null)
		{ 
			addError(node, Error.UNDEC_VAR, node.getImage());
			node.setType(Type.UNKNOWN);
			ASTUtils.DEBUG(node, "DIDN'T FIND: " + node.getImage());
		}
		else
			node.setType(type);

		return WalkType.NO_CHILDREN;
	}
	
	public Object visit(ASTRelationalExpression node, Boolean secondVisit) throws ShadowException {
		if( !secondVisit )
			return WalkType.POST_CHILDREN;		

		Type result = node.jjtGetChild(0).getType();
					
		for( int i = 1; i < node.jjtGetNumChildren(); i++ )
		{
			Type current = node.jjtGetChild(i).getType(); 
			if( !result.isNumerical() || !current.isNumerical() )
			{
				addError(node, Error.INVL_TYP, "Relational operator not defined on types " + result + " and " + current);
				node.setType(Type.UNKNOWN);
				return WalkType.POST_CHILDREN;
			}	
			
			result = Type.BOOLEAN;  //boolean after one comparison
		
		}
		
		node.setType(result); //propagates type up if only one child
		pushUpModifiers(node); //can make ASSIGNABLE (if only one child)	
		
		return WalkType.POST_CHILDREN;	
	}
	
	public Object visit(ASTConcatenationExpression node, Boolean secondVisit) throws ShadowException 
	{
		if( secondVisit )
		{		
			Type result = node.jjtGetChild(0).getType();			
			
			if( node.jjtGetNumChildren() > 1 )
				result = Type.STRING;
				
			node.setType(result); //propagates type up if only one child
			pushUpModifiers(node); //can add ASSIGNABLE (if only one child)
		}
		
		return WalkType.POST_CHILDREN;	
	}	
	
	public Object visit(ASTEqualityExpression node, Boolean secondVisit) throws ShadowException {
		if( !secondVisit )
			return WalkType.POST_CHILDREN;

		Type result = node.jjtGetChild(0).getType();
					
		for( int i = 1; i < node.jjtGetNumChildren(); i++ )
		{
			Type current = node.jjtGetChild(i).getType(); 
			if( !result.isSubtype(current) && !current.isSubtype(result) )
			{
				addError(node, Error.INVL_TYP, "Equality operator not defined on types " + result + " and " + current);
				node.setType(Type.UNKNOWN);
				return WalkType.POST_CHILDREN;
			}	
			
			result = Type.BOOLEAN;  //boolean after one comparison			
		}
		
		node.setType(result); //propagates type up if only one child
		pushUpModifiers(node); //can overwrite ASSIGNABLE (if only one child)
			
		
		return WalkType.POST_CHILDREN;		
	}
	
	public void visitShiftRotate( SimpleNode node ) throws ShadowException
	{			
		Type result = node.jjtGetChild(0).getType();
			
		for( int i = 1; i < node.jjtGetNumChildren(); i++ ) //cycle through types, upgrading to broadest legal one, short => int => long is possible
		{
			Node child = node.jjtGetChild(i); 
	
			if( !(child  instanceof ASTRightShift) && !(child instanceof ASTRightRotate)  ) //RightRotate() and RightShift() have their own productions
			{					
				Type current = child.getType();										
				
				if( !current.isIntegral() || !result.isIntegral() )				
				{
					addError(child, Error.INVL_TYP, "Shift and rotate operations not defined on types " + result + " and " + current);
					node.setType(Type.UNKNOWN);
					return;
				}
			}				
		}				
		node.setType(result); //propagates type up if only one child	
		pushUpModifiers(node);  //can add ASSIGNABLE (if only one child)
	}
	
	public Object visit(ASTShiftExpression node, Boolean secondVisit) throws ShadowException {
		if(secondVisit)
			visitShiftRotate( node );
		
		return WalkType.POST_CHILDREN;
	}
	
	public Object visit(ASTRotateExpression node, Boolean secondVisit) throws ShadowException {
		if(secondVisit)
			visitShiftRotate( node );
		
		return WalkType.POST_CHILDREN;
	}
	
	public void visitArithmetic(SimpleNode node) throws ShadowException 
	{
		Type result = node.jjtGetChild(0).getType();
	
		for( int i = 1; i < node.jjtGetNumChildren(); i++ ) //cycle through types, upgrading to broadest legal one
		{
			Type current = node.jjtGetChild(i).getType();
			
			if( result.isNumerical() && current.isNumerical() )
			{
				if( result.isSubtype( current ))  //upgrades type to broader type (e.g. int goes to double)
					result = current;
				else if( !current.isSubtype(result) )
				{
					addError(node.jjtGetChild(i), Error.INVL_TYP, "Cannot apply arithmetic operations to " + result + " and " + current);
					node.setType(Type.UNKNOWN);
					return;					
				}					
			}
			else		
			{
				addError(node.jjtGetChild(i), Error.INVL_TYP, "Cannot apply arithmetic operations to " + result + " and " + current);
				node.setType(Type.UNKNOWN);
				return;						
			}				
		}
			
		node.setType(result); //propagates type up if only one child
		pushUpModifiers(node); //can add ASSIGNABLE (if only one child)
	}
	
	public Object visit(ASTAdditiveExpression node, Boolean secondVisit) throws ShadowException {
		if(secondVisit)
			visitArithmetic( node );
		
		return WalkType.POST_CHILDREN;
	}
	
	public Object visit(ASTMultiplicativeExpression node, Boolean secondVisit) throws ShadowException {
		if(secondVisit)
			visitArithmetic( node );
		
		return WalkType.POST_CHILDREN;
	}
	
	public Object visit(ASTUnaryToString node, Boolean secondVisit) throws ShadowException {
		if(secondVisit)
		{				
			Type type = node.jjtGetChild(0).getType();
			String symbol = node.getImage();
			
			if( symbol.equals("#")  )
				node.setType(Type.STRING);
			else
			{
				node.setType(type);			
				pushUpModifiers( node );
			}
		}
		
		return WalkType.POST_CHILDREN;
	}
		
	public Object visit(ASTUnaryExpression node, Boolean secondVisit) throws ShadowException {
		if(secondVisit)
		{				
			Type type = node.jjtGetChild(0).getType();
			String symbol = node.getImage();
			
			if( (symbol.equals("-") || symbol.equals("+")) ) 
			{
				if( !type.isNumerical() )
				{
					addError(node, Error.INVL_TYP, "Found type " + type + ", but numerical type required for arithmetic operations");
					node.setType(Type.UNKNOWN);
					return WalkType.POST_CHILDREN;
				}
			}
			else
				pushUpModifiers( node );
			
			if(symbol.contains("-"))
				node.setType(Type.makeSigned((ClassType)type));
			else
				node.setType(type);
		}
		
		return WalkType.POST_CHILDREN;
	}
		
	public Object visit(ASTUnaryExpressionNotPlusMinus node, Boolean secondVisit) throws ShadowException {
		if(!secondVisit)
			return WalkType.POST_CHILDREN;
		
		Type type = node.jjtGetChild(0).getType();
		
		if(node.getImage().startsWith("~") )
		{
			if( !type.isIntegral() )
			{
				addError(node, Error.INVL_TYP, "Found type " + type + ", but integral type required for bitwise operations");
				type = Type.UNKNOWN;				
			}
		}		
		else if(node.getImage().startsWith("!") )
		{
			if( !type.equals(Type.BOOLEAN)) 
			{
				addError(node, Error.INVL_TYP, "Found type " + type + ", but boolean type required for logical operations");
				type = Type.UNKNOWN;				
			}
		}
		else
			pushUpModifiers( node ); //can add ASSIGNABLE (if only one child)
		
		node.setType(type);		
		return WalkType.POST_CHILDREN;
	}
	
	
	public Object visit(ASTConditionalExpression node, Boolean secondVisit) throws ShadowException {
		if( !secondVisit )
			return WalkType.POST_CHILDREN;
		
		if(node.jjtGetNumChildren() == 1)
			pushUpType( node, secondVisit ); //propagate type and modifiers up
		else if(node.jjtGetNumChildren() == 3)
		{			
			Type t1 = node.jjtGetChild(0).getType();
			Type t2 = node.jjtGetChild(1).getType();
			Type t3 = node.jjtGetChild(2).getType();
			
			if( !t1.equals(Type.BOOLEAN) ) 
			{			
				addError(node.jjtGetChild(0), Error.INVL_TYP, "Found type" + t1 + ", but boolean type required for conditional operations");
				node.setType(Type.UNKNOWN);
			}
			else if( t2.isSubtype(t3) )
				node.setType(t3);
			else if( t3.isSubtype(t2) )
				node.setType(t2);
			else 
			{
				addError(node, Error.TYPE_MIS, "Type " + t2 + " must match " + t3 + " in ternary operator");
				node.setType(Type.UNKNOWN);
			}
		}		
		
		return WalkType.POST_CHILDREN;
	}
	
	public void visitConditional(SimpleNode node ) throws ShadowException {
		
		if( node.jjtGetNumChildren() == 1 )
			pushUpType(node, true); //includes modifier push up
		else
		{
			Type result = null;
			
			for( int i = 0; i < node.jjtGetNumChildren(); i++ )
			{
				result = node.jjtGetChild(i).getType();
			
				if( result != Type.BOOLEAN )
				{
					addError(node.jjtGetChild(i), Error.INVL_TYP, "Found type " + result + ", but boolean type required for conditional operations");			
					node.setType(Type.UNKNOWN);
					return;
				}					
			}
			
			node.setType(result);
		}
	}
	
	public Object visit(ASTCoalesceExpression node, Boolean secondVisit) throws ShadowException {
		if( secondVisit )
		{
			if( node.jjtGetNumChildren() == 1 )
				pushUpType(node, secondVisit); //includes modifier push up
			else
			{				
				
				Type result = null;
				boolean isNullable = true;
				
				for( int i = 0; i < node.jjtGetNumChildren(); i++ ) //cycle through types, upgrading to broadest legal one
				{
					Node child = node.jjtGetChild(i); 
					Type type = child.getType();
					Modifiers modifiers = child.getModifiers();
					
					if( !modifiers.isNullable() )
					{
						isNullable = false;
						if( i < node.jjtGetNumChildren() - 1 )  //only last child can be nullable
						{	
							addError(child, Error.INVL_TYP, "Only the last term in a coalesce expression can be non-nullable");
							result = Type.UNKNOWN;
							break;
						}
					}
					
					if( result == null )
						result = type;				
					else if( result.isSubtype(type) )
						result = type;
					else if( !type.isSubtype(result) ) //neither is subtype of other, panic!
					{
						addError(node, Error.INVL_TYP, "Types in coalesce expression do not match");
						result = Type.UNKNOWN;
						break;
					}
				}
				
				node.setType(result);
				if( isNullable )
					node.addModifier(Modifiers.NULLABLE);
			}			
		}	
		
		return WalkType.POST_CHILDREN;
	}
	
	public Object visit(ASTConditionalOrExpression node, Boolean secondVisit) throws ShadowException {
		if(secondVisit)
			visitConditional( node );		
		return WalkType.POST_CHILDREN;
	}
	
	public Object visit(ASTConditionalExclusiveOrExpression node, Boolean secondVisit) throws ShadowException {
		if(secondVisit)
			visitConditional( node );		
		return WalkType.POST_CHILDREN;
	}
	
	public Object visit(ASTConditionalAndExpression node, Boolean secondVisit) throws ShadowException {
		if(secondVisit)
			visitConditional( node );
		return WalkType.POST_CHILDREN;
	}	


	public void visitBitwise(SimpleNode node ) throws ShadowException {
				
		if( node.jjtGetNumChildren() == 1 )
			pushUpType(node, true); //includes modifier push up
		else
		{
			Type result = node.jjtGetChild(0).getType();
			if( !result.isIntegral() )
			{
				addError(node.jjtGetChild(0), Error.INVL_TYP, "Found type " + result + ", but integral type required for shift and rotate operations");
				node.setType(Type.UNKNOWN);
				return;
			}
			
			for( int i = 1; i < node.jjtGetNumChildren(); i++ ) //cycle through types, upgrading to broadest legal one
			{
				Type current = node.jjtGetChild(i).getType();
			
				if( !current.isIntegral() )
				{
					addError(node.jjtGetChild(i), Error.INVL_TYP, "Found type " + current + ", but integral type required for shift and rotate operations");
					node.setType(Type.UNKNOWN);
					return;
				}
				
				if( result.isSubtype(current))
					result = current;
			}
			
			node.setType(result);
		}
	}
	
	public Object visit(ASTBitwiseOrExpression node, Boolean secondVisit) throws ShadowException {
		if(!secondVisit)
			return WalkType.POST_CHILDREN;
				
		visitBitwise( node );
		return WalkType.POST_CHILDREN;
	}	
	
	public Object visit(ASTBitwiseExclusiveOrExpression node, Boolean secondVisit) throws ShadowException {
		if(!secondVisit)
			return WalkType.POST_CHILDREN;
				
		visitBitwise( node );
		return WalkType.POST_CHILDREN;
	}	
	
	public Object visit(ASTBitwiseAndExpression node, Boolean secondVisit) throws ShadowException {
		if(!secondVisit)
			return WalkType.POST_CHILDREN;
				
		visitBitwise( node );
		return WalkType.POST_CHILDREN;
	}
	
	private boolean isValidAssignment( Node left, Node right, ASTAssignmentOperator assignment )
	{
		Type leftType = left.getType();		 
		Type rightType = right.getType();
		Modifiers rightModifiers;				
		Modifiers leftModifiers;			
		
		if( rightType instanceof PropertyType )
		{
			PropertyType propertyType = (PropertyType)rightType;
			if( propertyType.isGettable() )
			{
				rightType = propertyType.getGetType().getType();
				rightModifiers = propertyType.getGetType().getModifiers();
			}
			else
			{
				addError(right, Error.TYPE_MIS, "Property " + right + "does not have get access.");
				return false;
			}
		}
		else
			rightModifiers = right.getModifiers();
		
						
		if( leftType instanceof PropertyType )  
		{					
			PropertyType propertyType = (PropertyType)leftType;					
			
			if( propertyType.acceptsAssignment(rightType, assignment.getAssignmentType()) )
				leftModifiers = propertyType.getSetType().getModifiers();
			else
			{
				addError(left, Error.TYPE_MIS, "Property with type " + propertyType + " cannot accept type " + rightType + " in this assignment");
				return false;
			}
		}
		else
		{
			leftModifiers = left.getModifiers();
			
			if( !leftType.acceptsAssignment(rightType, assignment.getAssignmentType())  )
			{
				addError(left, Error.TYPE_MIS, "Found type " + rightType + ", type " + leftType + " required");
				return false;
			}
		}
		
		if( !leftModifiers.isAssignable() )
		{
			addError(left, Error.TYPE_MIS, "Cannot assign a value to expression: " + left);
			return false;
		}
		/*else if( leftModifiers.isFinal() )
		{
			addError(left, Error.INVL_TYP, "Cannot assign a value to variable marked final");
			return false;
		}*/
		else if( leftModifiers.isConstant() )
		{
			addError(left, Error.INVL_TYP, "Cannot assign a value to variable marked constant");
			return false;			
		}
		else if( leftModifiers.isImmutable() && (!leftModifiers.isField() || (!currentMethod.isEmpty() && !currentMethod.getFirst().getMethodSignature().isCreate()))   )
		{
			addError(left, Error.INVL_TYP, "Cannot assign a value to field marked immutable except in a create");
			return false;
		}		
		else if( !leftModifiers.isNullable() && rightModifiers.isNullable() )
		{
			addError(left, Error.TYPE_MIS, "Cannot assign a nullable value to a non-nullable variable");			
			return false;
		}
		
		
		return true;
	}

	/*
	public Object visit(ASTAssignment node, Boolean secondVisit) throws ShadowException {
		if(!secondVisit)
			return WalkType.POST_CHILDREN;
				
		Node left = node.jjtGetChild(0);
		ASTAssignmentOperator assignment = (ASTAssignmentOperator) node.jjtGetChild(1); 
		Node right = node.jjtGetChild(2);
		
		if( isValidAssignment( left, right, assignment ) )
		{
			node.setType(left.getType());
			
			if( !(left.getType() instanceof PropertyType) )
				node.setModifiers(left.getModifiers());
		}
		else
			node.setType(Type.UNKNOWN);
	
		return WalkType.POST_CHILDREN;
	}
	*/


	public Object visit(ASTClassOrInterfaceType node, Boolean secondVisit) throws ShadowException {
		return typeResolution(node, secondVisit);
	}	
	
	
	public Object visit(ASTArgumentList node, Boolean secondVisit) throws ShadowException {
		if(!secondVisit)
			return WalkType.POST_CHILDREN;
		
		SequenceType sequenceType = new SequenceType();
		
		for( int i = 0; i < node.jjtGetNumChildren(); i++ )
			sequenceType.add(node.jjtGetChild(i));
		
		node.setType(sequenceType);
		
		return WalkType.POST_CHILDREN;		
	}
	
	public Object visit(ASTArguments node, Boolean secondVisit) throws ShadowException {
		if(!secondVisit)
			return WalkType.POST_CHILDREN;
		
		if( node.jjtGetNumChildren() == 0 )
			node.setType(new SequenceType());
		else
			node.setType(((ASTArgumentList)(node.jjtGetChild(0))).getType());
		
		return WalkType.POST_CHILDREN; 
	}
	
	public Object visit(ASTStatementExpression node, Boolean secondVisit) throws ShadowException {
			return WalkType.POST_CHILDREN;
	}
	
	public Object visit(ASTSequenceAssignment node, Boolean secondVisit) throws ShadowException {
		if( !secondVisit )
			return WalkType.POST_CHILDREN;
		
			ASTSequence left = (ASTSequence) node.jjtGetChild(0);
			SequenceType leftType = (SequenceType)(left.getType());
					
			if( !leftType.isAssignable() )			
				addError(left, Error.TYPE_MIS, "Cannot assign a value to expression: " + left);
			else
			{			
				
			Node right = node.jjtGetChild(1);				
			if( right.getType() instanceof SequenceType ) //never a property, because a property can only access a single value
			{
				SequenceType rightType = (SequenceType)(right.getType());
				List<String> reasons = new ArrayList<String>();
				
				if( !leftType.canAccept( rightType, reasons ) )				
					addError(left, Error.TYPE_MIS, reasons.get(0));
			}
			else
				addError(left, Error.TYPE_MIS, right.getType() + " must be of sequence type");
		}

		return WalkType.POST_CHILDREN;	
	}

	public Object visit(ASTExpression node, Boolean secondVisit) throws ShadowException {
		if(!secondVisit)
			return WalkType.POST_CHILDREN;
		
		if( node.jjtGetNumChildren() == 3 ) //if there is assignment
		{
			Node left = node.jjtGetChild(0);
			ASTAssignmentOperator assignment = (ASTAssignmentOperator) node.jjtGetChild(1);			
			Node right = node.jjtGetChild(2);
			
			isValidAssignment(left, right, assignment); 
			//will issue appropriate errors
			//since this is an Expression (with nothing to the left), there is no type to set
		}
		else //did something actually happen?
		{
			ASTPrimaryExpression child = (ASTPrimaryExpression) node.jjtGetChild(0);
			if( !child.isAction() )
				addError(node, Error.INVL_TYP, "Statement does not express an action");
		}
		
		return WalkType.POST_CHILDREN;	
	}

	
	public Object visit(ASTIsExpression node, Boolean secondVisit) throws ShadowException {
		if(!secondVisit)
			return WalkType.POST_CHILDREN;
		
		//RelationalExpression() [ "is" Type() ]
		
		if( node.jjtGetNumChildren() == 1 )
			pushUpType(node, secondVisit);
		else
		{
			Type t1 = node.jjtGetChild(0).getType();
			Type t2 = node.jjtGetChild(1).getType();
			
			if( t1.isSubtype(t2) || t2.isSubtype(t1) )
				node.setType(Type.BOOLEAN);
			else
			{
				addError(node, Error.TYPE_MIS, "Type " + t1 + " uncomparable with type " + t2);
				node.setType(Type.UNKNOWN);
			}
		}			

		return WalkType.POST_CHILDREN;
	}
	
	@Override
	public Object visit(ASTTypeParameter node, Boolean secondVisit)	throws ShadowException
	{
		if( secondVisit )
		{
			String symbol = node.getImage();
			TypeParameter typeParameter = new TypeParameter(symbol);					
			Node grandparent = node.jjtGetParent().jjtGetParent(); //parent is always TypeParameters
			Type type;
			
			
			if( grandparent instanceof ASTMethodDeclarator ) //skip class declarations (already done in type updater)
			//this will only be for local methods at this stage
			{
				//if( grandparent instanceof ASTClassOrInterfaceDeclaration )										
				//	type = grandparent.getType();
				//else // grandparent instanceof ASTMethodDeclarator				
					type = grandparent.jjtGetParent().getType(); //method declaration is  three levels up
				
				for( ModifiedType existing : type.getTypeParameters() )
					if( existing.getType().getTypeName().equals( symbol ) )
						addError( node, Error.MULT_SYM, "Multiply defined type parameter " + typeParameter.getTypeName() );
				
				if( node.jjtGetNumChildren() > 0 )
				{
					ASTTypeBound bound = (ASTTypeBound)(node.jjtGetChild(0));				
					for( int i = 0; i < bound.jjtGetNumChildren(); i++ )								
						typeParameter.addBound(bound.jjtGetChild(i).getType());				
				}			
				
				node.setType(typeParameter);
				type.addTypeParameter(node);
			}			
		}
		return WalkType.POST_CHILDREN;
	}
	
	public Object visit(ASTFunctionType node, Boolean secondVisit) throws ShadowException {
		if(!secondVisit)
			return WalkType.POST_CHILDREN;
		
		MethodType methodType = new MethodType();		
		
		int children = node.jjtGetNumChildren(); 
		if( children > 0 ) //fill in parameters
		{		
			for( int i = 0; i < children - 1; i++ )
				methodType.addParameter(node.jjtGetChild(i));
			
			Node last = node.jjtGetChild(children - 1); 
			
			if(  last instanceof ASTResultTypes ) //if last child is results, add those
			{
				ASTResultTypes results = (ASTResultTypes)last;
				for( ModifiedType type : results.getTypes() )
					methodType.addReturn(type);
			}
			else //otherwise everything was a parameter
				methodType.addParameter(last);			
		}
		
		node.setType(methodType);
		
		return WalkType.POST_CHILDREN;
	}
	
	public Object visit(ASTResultTypes node, Boolean secondVisit) throws ShadowException 
	{
		if(!secondVisit)
			return WalkType.POST_CHILDREN;
	
		for( int i = 0; i < node.jjtGetNumChildren(); i++ )
			node.addType(node.jjtGetChild(i));
		
		Node parent = node.jjtGetParent();
		Node grandparent = parent.jjtGetParent();
		if( parent instanceof ASTMethodDeclarator && grandparent instanceof ASTLocalMethodDeclaration )
		{
			MethodSignature signature = ((SignatureNode)grandparent).getMethodSignature();		
	
			for(int i=0; i < node.jjtGetNumChildren(); ++i) 
			{
				Type type = node.jjtGetChild(i).getType();
				
				// make sure the return type is in the type table
				if(type == null)					
					addError(node.jjtGetChild(i), Error.UNDEF_TYP);
				else						
				// add the return type to our signature
					signature.addReturn(node.jjtGetChild(i));
			}
		}

		return WalkType.POST_CHILDREN;
	}
	
	public Object visit(ASTCastExpression node, Boolean secondVisit) throws ShadowException {
		if(!secondVisit)
			return WalkType.POST_CHILDREN;
		
		
		if( node.jjtGetNumChildren() == 1 )
			pushUpType(node, secondVisit);
		else
		{
			Type t1 = node.jjtGetChild(0).getType();  //type
			Type t2 = node.jjtGetChild(1).getType();  //expression
			
			
			if( t1 instanceof MethodType && t2 instanceof UnboundMethodType ) //casting methods
			{
				MethodType method = (MethodType)t1;
				UnboundMethodType unboundMethod = (UnboundMethodType)t2;				
				
				boolean found = false;
				
								
				Type outer = unboundMethod.getOuter();				
				for( MethodSignature signature : outer.getMethods(unboundMethod.getTypeName()) )
					if( signature.getMethodType().matches(method.getParameterTypes()) && signature.getMethodType().canReturn(method.getReturnTypes()))
					{
						node.setType(signature.getMethodType());
						found = true;
						break;
					}
					
				if( !found )
				{
					addError(node, Error.TYPE_MIS, "Cannot cast: No method " + unboundMethod.getTypeName() + " matches signature " + method);
					node.setType(Type.UNKNOWN);
				}				
			}			
			else if( t1.isNumerical() && t2.isNumerical() ) //some numerical types (int and uint) are not superclasses or subclasses of each other
															//for convenience, all numerical types should be castable
				node.setType(t1);
			else if( t1.isSubtype(t2) || t2.isSubtype(t1) )
				node.setType(t1);
			else
			{
				addError(node, Error.TYPE_MIS, "Type " + t2 + " cannot be cast to " + t1);
				node.setType(Type.UNKNOWN);
			}
		}			

		return WalkType.POST_CHILDREN;
	}
	
	public Object visit(ASTSequence node, Boolean secondVisit) throws ShadowException {
		if(!secondVisit)
			return WalkType.POST_CHILDREN;
		
		LinkedList<Boolean> usedItems = node.getUsedItems();
		
		if( node.jjtGetNumChildren() == 1 && usedItems.size() == 1 ) //maybe, or should it be treated like a sequence with one thing in it?
			pushUpType(node, secondVisit);
		else
		{
			SequenceType sequence = new SequenceType();
			int child = 0;		
			
			for( int i = 0; i < usedItems.size(); i++ )
			{
				if( usedItems.get(i))
				{
					sequence.add(node.jjtGetChild(child));
					child++;
				}
				else
					sequence.add(null);
			}
			
			node.setType( sequence );
		}			

		return WalkType.POST_CHILDREN;
	}
	
	

	public Object visit(ASTIfStatement node, Boolean secondVisit) throws ShadowException {
		if(!secondVisit)
			return WalkType.POST_CHILDREN;
		
		Type t = node.jjtGetChild(0).getType(); 
		
		if( !t.equals( Type.BOOLEAN ) )
			addError(node, Error.TYPE_MIS, "conditional of if statement must be boolean, found: " + t);
				
		return WalkType.POST_CHILDREN;
	}
	
	public Object visit(ASTWhileStatement node, Boolean secondVisit) throws ShadowException {
		if(!secondVisit)
			return WalkType.POST_CHILDREN;
		
		Type t = node.jjtGetChild(0).getType(); 
		
		if( !t.equals( Type.BOOLEAN ) )
			addError(node, Error.TYPE_MIS, "conditional of while statement must be boolean, found: " + t);
				
		return WalkType.POST_CHILDREN;
	}

	public Object visit(ASTDoStatement node, Boolean secondVisit) throws ShadowException {
		if(!secondVisit)
			return WalkType.POST_CHILDREN;
		
		Type t = node.jjtGetChild(1).getType(); //second child, not first like if and while 
		
		if( !t.equals( Type.BOOLEAN ) )
			addError(node, Error.TYPE_MIS, "conditional of do statement must be boolean, found: " + t);
				
		return WalkType.POST_CHILDREN;
	}
	
	
	public Object visit(ASTForeachStatement node, Boolean secondVisit) throws ShadowException {
		createScope(secondVisit); //for variables declared in header, right?  Pretty sure that works
		if(!secondVisit)
			return WalkType.POST_CHILDREN;
				
		Type t1 = node.jjtGetChild(0).getType(); //Type declaration
		Type t2 = node.jjtGetChild(1).getType(); //Collection
		
		//
		// TODO: Eventually we'll want the notion of a collection and need to check that here
		//
		if( t2 instanceof ArrayType )
		{
			ArrayType array = (ArrayType)t2;
			if( !array.getBaseType().isSubtype(t1) )
				addError(node, Error.TYPE_MIS, "incompatible foreach variable, found: " + t1 + " expected: " + array.getBaseType());
		}
		else
			addError(node, Error.TYPE_MIS, "foreach loop only works on arrays in this typechecker build, found: " + t2);
			
				
		return WalkType.POST_CHILDREN;
	}

	public Object visit(ASTForStatement node, Boolean secondVisit) throws ShadowException {
		boolean hasInit = false;
		
		if(node.jjtGetChild(0) instanceof ASTForInit) {
			createScope(secondVisit);	// only need the scope if we've created new vars
			hasInit = true;
		}
		
		if(!secondVisit)
			return WalkType.POST_CHILDREN;
		
		// the conditional type might come first or second depending upon if there is an init or not
		Type conditionalType = null;
		
		if(hasInit)
			conditionalType = node.jjtGetChild(1).getType();
			
		else
			conditionalType = node.jjtGetChild(0).getType();
		
		ASTUtils.DEBUG("TYPE: " + conditionalType);
		
		if(conditionalType == null || !conditionalType.equals( Type.BOOLEAN ) )
			addError(node, Error.TYPE_MIS, "conditional of for statement must be boolean, found: " + conditionalType);
		
		return WalkType.POST_CHILDREN;
	}	
	
	
	public Object visit(ASTTryStatement node, Boolean secondVisit) throws ShadowException 
	{		
		if(secondVisit)
		{
			if( node.getBlocks() == 0 )
				addError( node, Error.TYPE_MIS, "try statement must have at least one catch, recover, or finally block" );
			else
			{				
				Node child;
				List<Type> types = new LinkedList<Type>();
				
				for( int i = 0; i < node.getCatches(); i++ )				
				{
					//catch statement
					child = node.jjtGetChild(2*i+1); //formal parameter
					
					Type type = child.getType();
					if( type instanceof ExceptionType )
					{
						for( Type existing : types )
							if( type.isSubtype(existing) )
							{
								addError( child, Error.TYPE_MIS, "unreachable catch: " + type );
								break;
							}						
					}
					else
						addError( child, Error.TYPE_MIS, "found " + type + " but only exception types allowed for catch parameters");
				}
				
				//no checking necessary for recover
				
				//no checking necessary for finally 
				
				tryBlocks.removeFirst();
			}						
		}	
		else
			tryBlocks.addFirst(node);
			
		
		return WalkType.POST_CHILDREN;
	}
	
	public Object visit(ASTCheckExpression node, Boolean secondVisit) throws ShadowException 
	{
		if( secondVisit )
		{
			ModifiedType child = node.jjtGetChild(0);
			
			//only gets make sense for a check expression
			if( child.getType() instanceof PropertyType )
			{
				PropertyType propertyType = (PropertyType) child.getType();
				child = propertyType.getGetType(); 
			}
			
			
			if( child.getModifiers().isNullable() )
			{
				node.setModifiers( child.getModifiers() );
				node.removeModifier(Modifiers.NULLABLE );
			}
			else
				addError( node, Error.TYPE_MIS, "check expression can only be used on an expression with a nullable type");
			
			node.setType(child.getType());
		}
		else
		{
			boolean found = false;
			for( ASTTryStatement statement : tryBlocks )
			{
				if( statement.hasRecover() )
				{
					found = true;
					break;
				}				
			}
			
			if( !found )
				addError( node, Error.TYPE_MIS, "check expression not inside of try statement with recover block");
		}
		
		return WalkType.POST_CHILDREN;
	}
	
	public Object visit(ASTPrimaryExpression node, Boolean secondVisit) throws ShadowException 
	{
		if(!secondVisit)
		{
			curPrefix.addFirst(null);
			return WalkType.POST_CHILDREN;
		}		
		
		if( node.jjtGetNumChildren() > 1 ) 	//has suffixes, pull type from last suffix
		{
			
			Node last = node.jjtGetChild(node.jjtGetNumChildren() - 1);
						
			if(node.jjtGetParent() instanceof ASTExpression ) //this primary expression is the left side of an assignment
			{
				Type type = last.getType(); //if PropertyType, preserve that
				node.setModifiers(last.getModifiers());
				node.setType(type);
			}
			else
			{
				ModifiedType modifiedType = resolveType( last ); //otherwise, strip away the property
				node.setModifiers(modifiedType.getModifiers());
				node.setType(modifiedType.getType());
			}
		}
		else								//just prefix
		{
			Node child = node.jjtGetChild(0);
			node.setType(child.getType());
			pushUpModifiers( node );
		}		
		
		curPrefix.removeFirst();  //pop prefix type off stack
		
		return WalkType.POST_CHILDREN;
	}
	
	public Object visit(ASTPrimaryPrefix node, Boolean secondVisit) throws ShadowException 
	{
		if(!secondVisit)
			return WalkType.POST_CHILDREN;
	
		
		int children = node.jjtGetNumChildren();
		
		if(  children == 0 )
		{
			if( node.getImage().equals("this")  ) //this
			{	
				if( currentType instanceof InterfaceType )
				{
					addError(node, Error.INVL_TYP, "Reference this invalid for interfaces");
					node.setType(Type.UNKNOWN);
				}
				else				
					node.setType(currentType);				
			}
			else if( node.getImage().startsWith("super")) //super case
			{
				if( currentType instanceof InterfaceType )
				{
					addError(node, Error.INVL_TYP, "Reference super invalid for interfaces");
					node.setType(Type.UNKNOWN);
				}
				else		
				{
					ClassType classType = (ClassType) currentType;					
					node.setType(classType.getExtendType());
				}
			}
			else //just a name
			{
				String name = node.getImage();
				if( !setTypeFromName( node, name )) //automatically sets type if can
				{
					addError(node, Error.UNDEC_VAR, name);
					node.setType(Type.UNKNOWN);
					ASTUtils.DEBUG(node, "DIDN'T FIND: " + name);						
				}
			}
		}
		else if( children == 1 )
		{
			Node child = node.jjtGetChild(0); 
			
			if( child instanceof ASTUnqualifiedName )
			{
				node.setImage(child.getImage() + "@" + node.getImage());
				String name = node.getImage();
				if( !setTypeFromName( node, name )) //automatically sets type if can
				{
					addError(node, Error.UNDEC_VAR, name);
					node.setType(Type.UNKNOWN);
					ASTUtils.DEBUG(node, "DIDN'T FIND: " + name);						
				}
			}
			else
			{
				node.setType( child.getType() ); 	//literal, conditional expression, check expression, primitive and function types
				pushUpModifiers( node ); 			
			}
		}
		
		curPrefix.set(0, node); //so that the suffix can figure out where it's at
		
		/*   
		  Literal()
		| "this" { jjtThis.setImage("this"); }
		| "super" { jjtThis.setImage("super"); }
		| CheckExpression()
		| "(" ConditionalExpression() ")"
		| PrimitiveType()
		| FunctionType()
		| [LOOKAHEAD(UnqualifiedName() "@") UnqualifiedName() "@" ] t = <IDENTIFIER> { jjtThis.setImage(t.image); debugPrint(t.image); }
		 */
				
		return WalkType.POST_CHILDREN;
	}
	
	
	public Object visit(ASTClassOrInterfaceTypeSuffix node, Boolean secondVisit) throws ShadowException
	{
		return WalkType.PRE_CHILDREN;		
	}
	
	public Object visit(ASTTypeArguments node, Boolean secondVisit) throws ShadowException
	{
		if( secondVisit )
		{
			SequenceType sequenceType = new SequenceType();
			
			for( int i = 0; i < node.jjtGetNumChildren(); i++ )
				sequenceType.add(node.jjtGetChild(i));
			
			
			node.setType(sequenceType);
		}
		
		return WalkType.POST_CHILDREN;
	}
	
	
	public Object visit(ASTPrimarySuffix node, Boolean secondVisit) throws ShadowException 
	{
		/*
		  QualifiedKeyword()
		| Brackets()
		| Subscript()
		| Method()
		| Allocation()
		| ScopeSpecifier()
		| Property()
		| MethodCall()
		*/
		
		if(secondVisit)
		{
			Node prefixNode = curPrefix.getFirst();		
			
			if( prefixNode.getModifiers().isNullable() )
				addError(node, Error.TYPE_MIS, "cannot dereference nullable variable");
			
			Node child = node.jjtGetChild(0);
			if( (child instanceof ASTProperty) || (child instanceof ASTMethodCall) ||  (child instanceof ASTAllocation))
			{
				ASTPrimaryExpression parent = (ASTPrimaryExpression) node.jjtGetParent();
				parent.setAction(true);
			}			
			
			if( child instanceof ASTMethodCall )
			{
				Type childType = child.getType();
							
				if( childType instanceof MethodType )
				{
					MethodType methodType = (MethodType) childType;
					SequenceType returnTypes = methodType.getReturnTypes();
					returnTypes.setNodeType( node ); //used instead of setType
				}
				else
					node.setType(Type.UNKNOWN);
			}				
			else
				pushUpType(node, secondVisit);
			
			
			curPrefix.set(0, node); //so that a future suffix can figure out where it's at		
		}
		
		return WalkType.POST_CHILDREN;
		
	}

	public Object visit(ASTQualifiedKeyword node, Boolean secondVisit) throws ShadowException 
	{
		if( secondVisit )					
		{	
			node.setType(Type.UNKNOWN); //start with unknown, set if found
			String kind = node.getImage();
						
			if( curPrefix.getFirst().getModifiers().isTypeName())
			{	
				Type prefixType = curPrefix.getFirst().getType();
				
				if( kind.equals("this") )
				{					
					if( currentType.encloses( prefixType )  )				
						node.setType(prefixType);
					else				
						addError(node, Error.INVL_TYP, "Prefix of :" + node.getImage() + " is not the current class or an enclosing class");
				}
				else if( kind.equals("super") )
				{
					if( currentType.encloses( prefixType )  )
					{
						
						if( (prefixType instanceof ClassType) && ((ClassType)prefixType).getExtendType() != null )
							node.setType(((ClassType)prefixType).getExtendType());
						else
							addError(node, Error.INVL_TYP, "Type " + prefixType + " does not have a parent class");
					}
					else				
						addError(node, Error.INVL_TYP, "Prefix of qualified super is not the current class or an enclosing class");
				}
				else if( kind.equals("class"))
				{
					if( (prefixType instanceof ClassType) || (prefixType instanceof TypeParameter)  )
						node.setType( Type.CLASS );
					else					
						addError(node, Error.INVL_TYP, ":class constant only accessible on class, enum, error, exception, singleton types and type parameters"); //may need other cases
				}
			}
			else
				addError(node, Error.INVL_TYP, "Prefix of :" + kind + " is not a type name");
		}
		return WalkType.POST_CHILDREN;
	}
	
	
	public Object visit(ASTBrackets node, Boolean secondVisit) throws ShadowException 
	{
		if( secondVisit )
		{	
			ModifiedType prefixNode = curPrefix.getFirst();
			prefixNode = resolveType( prefixNode );
			Type prefixType = prefixNode.getType();
													
			if( prefixNode.getModifiers().isTypeName() && !(prefixType instanceof ArrayType) )
			{
				node.setType(new ArrayType( prefixType, node.getArrayDimensions() ) );				
			}
			else
			{
				node.setType(Type.UNKNOWN);
				addError(node, Error.INVL_TYP, "Can only apply brackets to a type");
			}
		}
		
		return WalkType.POST_CHILDREN;
	}
	
	public Object visit(ASTSubscript node, Boolean secondVisit) throws ShadowException 
	{
		if( secondVisit )
		{			
			ModifiedType prefixNode = curPrefix.getFirst();
			prefixNode = resolveType( prefixNode );
			Type prefixType = prefixNode.getType();
			
			if( prefixType instanceof ArrayType )
			{
				ArrayType arrayType = (ArrayType)prefixType;
				
				for( int i = 0; i < node.jjtGetNumChildren(); i++ )
				{
					Type childType = node.jjtGetChild(i).getType();
					
					if( !childType.isIntegral() )
					{
						addError(node.jjtGetChild(i), Error.INVL_TYP, "Found type " + childType + ", but integral type required for array subscript");
						node.setType(Type.UNKNOWN);
						return WalkType.POST_CHILDREN;
					}
				}
				
			    //only goes one level deep because of nullability
				int dimension = node.getArrayDimensions().get(0);				
				if( dimension == arrayType.getDimensions() )
				{
					node.setType( arrayType.getBaseType() );
					node.addModifier(Modifiers.ASSIGNABLE);
					
					//primitive arrays are initialized to default values
					//non-primitive array elements could be null
					if( !arrayType.getBaseType().isPrimitive() )
						node.addModifier(Modifiers.NULLABLE);						
				}				
				else
				{
					node.setType(Type.UNKNOWN);
					addError(node, Error.TYPE_MIS, "Needed "  + arrayType.getDimensions() + " indexes into array but found " +  node.jjtGetNumChildren());
				}
				
			}			
			else if( prefixType.hasInterface(Type.CAN_INDEX) )
			{
				if( node.jjtGetNumChildren() == 1)
				{
					SequenceType arguments = new SequenceType();
					Node child = node.jjtGetChild(0);
					arguments.add(child);					
									
					TreeMap<MethodSignature, MethodType> acceptableIndexes = new TreeMap<MethodSignature, MethodType>();
					
					for( MethodSignature signature : prefixType.getMethods("index") ) 
					{		
						MethodType methodType = signature.getMethodType();
						
						if( signature.matches( arguments )) 
						{
							//found it, don't look further!
							acceptableIndexes.clear();
							acceptableIndexes.put(signature, methodType);
							break;
						}
						else if( signature.canAccept( arguments ))
							acceptableIndexes.put(signature, methodType);
					}
					
					if( acceptableIndexes.size() == 0 ) 
					{
						addError(node, Error.TYPE_MIS, "Cannot index into type " + prefixType + " with subscript of type " + child.getType());
						node.setType(Type.UNKNOWN);
					}					
					else if( acceptableIndexes.size() > 1 )
					{
						addError(node, Error.TYPE_MIS, "Ambiguous index into type " + prefixType + " with subscript of type " + child.getType());
						node.setType(Type.UNKNOWN);
					}					
					else
					{
						Entry<MethodSignature, MethodType> entry = acceptableIndexes.firstEntry();
						MethodSignature signature = entry.getKey(); 
						MethodType methodType = entry.getValue();				 
						
						if( !methodIsAccessible( signature, currentType  ))
						{
							node.setType(Type.UNKNOWN);
							addError(node, Error.INVL_MOD, "Index not accessible from current context");
						}
						else
						{
							node.setType(methodType.getReturnTypes().getType(0));
							node.setModifiers(methodType.getReturnTypes().get(0).getModifiers());
							node.addModifier(Modifiers.ASSIGNABLE);
						}
					}	
				}
				else
				{
					node.setType(Type.UNKNOWN);
					addError(node, Error.INVL_TYP, "Cannot subscript into non-array type " + prefixType + " with multiple subscripts");
				}				
			}
			else
			{
				node.setType(Type.UNKNOWN);
				addError(node, Error.INVL_TYP, "Cannot subscript into type " + prefixType + " because it does not implement " + Type.CAN_INDEX);
			}	
			
		}
		
		return WalkType.POST_CHILDREN;
	}
	
	public Object visit(ASTDestroy node, Boolean secondVisit) throws ShadowException 
	{
		if( secondVisit )
		{	
			Node prefixNode = curPrefix.getFirst();
			Type prefixType = prefixNode.getType();			
			
			if( prefixNode.getModifiers().isTypeName()  )
				addError(node, Error.INVL_TYP, "Cannot destroy a type name");
			else if( prefixType instanceof UnboundMethodType )
				addError(node, Error.INVL_TYP, "Cannot destroy a method");

			else if( prefixType instanceof PropertyType )			
				addError(node, Error.INVL_TYP, "Cannot destroy a property");
			
			node.setType(Type.UNKNOWN); //destruction has no type
		}
		
		return WalkType.POST_CHILDREN;
	}
	
	public Object visit(ASTArrayCreate node, Boolean secondVisit) throws ShadowException 
	{
		if( secondVisit )
		{			
			Node prefixNode = curPrefix.getFirst();
			Type prefixType = prefixNode.getType();			
			SequenceType typeArguments = null;
			int start = 0;
			
			if( node.jjtGetChild(0) instanceof ASTTypeArguments )
			{
				typeArguments = (SequenceType) node.jjtGetChild(0).getType();
				start = 1;				
			}
			
			
			for( int i = start; i < node.jjtGetNumChildren(); i++ )
			{
				Type childType = node.jjtGetChild(i).getType();
				
				if( !childType.isIntegral() )
				{
					addError(node.jjtGetChild(i), Error.INVL_TYP, "Found type " + childType + ", but integral type required for array dimension");
					node.setType(Type.UNKNOWN);
					return WalkType.POST_CHILDREN;
				}
			}			
			
			//create array
			if( prefixNode.getModifiers().isTypeName() )
			{
				if( typeArguments != null  )
				{
					if( prefixType.isParameterized() && prefixType.getTypeParameters().canAccept(typeArguments))
					{					
						prefixType = prefixType.replace(prefixType.getTypeParameters(), typeArguments);
						node.setType(new ArrayType( prefixType, node.getArrayDimensions() ) );
					}
					else
					{
						addError(node, Error.TYPE_MIS, "Type " + prefixType + " cannot accept type arguments " + typeArguments);
						node.setType(Type.UNKNOWN);
					}
				}
				else
				{
					if( !prefixType.isParameterized() )
						node.setType(new ArrayType( prefixType, node.getArrayDimensions() ) );
					else
					{
						addError(node, Error.TYPE_MIS, "Type " + prefixType + " requires type arguments");
						node.setType(Type.UNKNOWN);	
					}					
				}				
			}
			else
			{
				node.setType(Type.UNKNOWN);
				addError(node, Error.INVL_TYP, "Can only create an array from a type name");
			}	
			
		}
		
		return WalkType.POST_CHILDREN;
	}

	
	
	public Object visit(ASTMethod node, Boolean secondVisit) throws ShadowException 
	{
		if( secondVisit )
		{			
			//always part of a suffix, thus always has a prefix
			ModifiedType prefixNode = curPrefix.getFirst();
			prefixNode = resolveType( prefixNode );
			Type prefixType = prefixNode.getType();
			String methodName = node.getImage();
			
			if( prefixNode.getModifiers().isTypeName() )
			{
				addError(node, Error.INVL_TYP, "Cannot call method on type name");				
			}
			else
			{				
				List<MethodSignature> methods = prefixType.getMethods(methodName);
				
				//unbound method (it gets bound when you supply arguments)
				if( methods != null && methods.size() > 0 )			
					node.setType( new UnboundMethodType( methodName, prefixType ) );
				else
					addError(node, Error.UNDEC_VAR, "Method " + methodName + " not found");
			}
			
			
			if( node.getType() == null )
				node.setType( Type.UNKNOWN );
		}
		
		return WalkType.POST_CHILDREN;
	}
	
	@Override
	public Object visit(ASTAllocation node, Boolean secondVisit)	throws ShadowException
	{
		if( secondVisit )
		{
			pushUpModifiers( node );
			Node child = node.jjtGetChild(0); 
			
			//create sets differently
			if( !(child instanceof ASTCreate) )			
				node.setType(child.getType());			
		}
		
		return WalkType.POST_CHILDREN;
		
	}
	
	
	@Override
	public Object visit(ASTCreate node, Boolean secondVisit)	throws ShadowException
	{
		if( secondVisit )
		{
			Node prefixNode = curPrefix.getFirst();
			Type prefixType = prefixNode.getType();
			node.setType(Type.UNKNOWN);
			
			if( prefixType instanceof InterfaceType )
			{
				addError(node, Error.INVL_TYP, "Interfaces cannot be created");
							
			}
			else if( prefixType instanceof SingletonType )
			{
				addError(node, Error.INVL_TYP, "Singletons cannot be created");
							
			}		
			else if(!( prefixType instanceof ClassType) )
			{
				addError(node, Error.INVL_TYP, "Type " + prefixType + " cannot be created");
				
			}
			else if( !prefixNode.getModifiers().isTypeName() )				
			{
				addError(node, Error.INVL_TYP, "Only a type can be created");
								
			}
			else
			{
				SequenceType typeArguments = null;
			
				
				int start = 0;
				if( node.jjtGetNumChildren() > 0 && (node.jjtGetChild(0) instanceof ASTTypeArguments) )
				{
					typeArguments = (SequenceType) node.jjtGetChild(0).getType();
					start = 1;
				}
				
				SequenceType arguments = new SequenceType();
				Node parent = node.jjtGetParent();
				
				for( int i = start; i < node.jjtGetNumChildren(); i++ )
					arguments.add(node.jjtGetChild(i));

				
				if( typeArguments != null )
				{					
					if( prefixType.isParameterized() && prefixType.getTypeParameters().canAccept(typeArguments) )
					{
						prefixType = prefixType.replace(prefixType.getTypeParameters(), typeArguments);
						setCreateType( node, prefixType, arguments);
						parent.setType(prefixType);
					}
					else
					{
						addError(node, Error.TYPE_MIS, "Type " + prefixType + " cannot accept type arguments " + typeArguments);
					}					
				}
				else
				{
					if( !prefixType.isParameterized() )
					{
						setCreateType( node, prefixType, arguments);
						parent.setType(prefixType);
					}
					else
						addError(node, Error.TYPE_MIS, "Type " + prefixType + " requires type arguments");
				}
				
			}
			
		}
		
		return WalkType.POST_CHILDREN;
	}
	
	
	
	@Override
	public Object visit(ASTInstance node, Boolean secondVisit)	throws ShadowException
	{
		if(secondVisit)
		{
			Node prefixNode = curPrefix.getFirst();
			Type prefixType = prefixNode.getType();
			
			if( !prefixNode.getModifiers().isTypeName() )
			{
				addError(node, Error.INVL_TYP, "A type is needed to get an instance");
				node.setType(Type.UNKNOWN);
			}
			else if( !(prefixType instanceof SingletonType) )
			{
				addError(node, Error.INVL_TYP, "Cannot get instance of non-singleton type " + prefixType);
				node.setType(Type.UNKNOWN);
			}
			else
				node.setType(prefixType);
			
		}
		
		return WalkType.POST_CHILDREN;
	}
	
	
	public Object visit(ASTScopeSpecifier node, Boolean secondVisit) throws ShadowException	
	{
		if( secondVisit )
		{	
			//always part of a suffix, thus always has a prefix
			ModifiedType prefixNode = curPrefix.getFirst();
			prefixNode = resolveType( prefixNode );
			Type prefixType = prefixNode.getType(); 
			String name = node.getImage();
			boolean isTypeName = prefixNode.getModifiers().isTypeName();
			
			if( prefixType.containsField( name ) )
			{
				Node field = prefixType.getField(name);
				
				if( !fieldIsAccessible( field, currentType ))
				{
					addError(node, Error.INVL_MOD, "Field " + name + " not accessible from current context");
				}					
				else if( field.getModifiers().isConstant() && !isTypeName )
				{
					addError(node, Error.INVL_MOD, "Constant " + name + " requires class name for access");
				}
				else
				{
					node.setType( field.getType());
					node.setModifiers(field.getModifiers());						
					if( !field.getModifiers().isConstant() )
						node.addModifier(Modifiers.ASSIGNABLE);					
				}
			}
			else if( prefixType.containsInnerClass(name) )
			{
				Type innerClass = prefixType.getInnerClass(name);
				
				if( !classIsAccessible( innerClass, currentType ) )
					addError(node, Error.TYPE_MIS, "Class " + innerClass + " is not accessible from current context");
				else
				{
					node.setType( innerClass );						
					node.addModifier(Modifiers.TYPE_NAME);					
				}					
			}
			else
				addError(node, Error.UNDEC_VAR, "Field or inner class " + name + " not found");
	
			
			if( node.getType() == null )
				node.setType( Type.UNKNOWN );
		}
		
		return WalkType.POST_CHILDREN;
	}
	
		
	public Object visit(ASTProperty node, Boolean secondVisit) throws ShadowException	
	{
		if( secondVisit )
		{		
			//always part of a suffix, thus always has a prefix
			ModifiedType prefixNode = curPrefix.getFirst();
			prefixNode = resolveType( prefixNode );
			Type prefixType = prefixNode.getType();
			String propertyName = node.getImage();
			boolean isTypeName = prefixNode.getModifiers().isTypeName();
						
						
			if( isTypeName )
			{
				addError(node, Error.INVL_TYP, "Must access property " + propertyName + " on an object");
				node.setType( Type.UNKNOWN ); //if got here, some error
			}	
			else
			{				
				List<MethodSignature> methods = prefixType.getMethods(propertyName);				

				if( methods != null && methods.size() > 0 )
				{
					MethodType getter = null;
					MethodType setter = null;
					
					for( MethodSignature signature : methods )
					{
						if( signature.getModifiers().isGet() )
							getter = signature.getMethodType();
						else if( signature.getModifiers().isSet() )
						{
							setter = signature.getMethodType();
							node.addModifier(Modifiers.ASSIGNABLE);
						}
					}
					
					node.setType( new PropertyType( getter, setter ) );
				}
				else
				{
					addError(node, Error.UNDEC_VAR, "Property " + propertyName + " not found");
					node.setType( Type.UNKNOWN ); //if got here, some error
				}				
			}
			
		}
	
		return WalkType.POST_CHILDREN;
	}
	
	private ModifiedType resolveType( ModifiedType node ) //dereferences into PropertyType for getter, if needed
	{
		Type type = node.getType();
		
		if( type instanceof PropertyType )
		{
			PropertyType propertyType = (PropertyType) type;
			if( propertyType.isGettable() )
				return propertyType.getGetType();
			else
			{
				addError((Node)node, Error.TYPE_MIS, "Property " + node + "does not have get access.");
				return node;
			}				
		}
		else
			return node;
	}
	
	protected void setCreateType( ASTCreate node, Type prefixType, SequenceType arguments)
	{	
		ClassType type = (ClassType) prefixType;
		
		//examine argument list to find create
		List<MethodSignature> candidateCreates = type.getMethods("create");
					
		// we have no creates
		if( candidateCreates == null || candidateCreates.size() == 0 )
		{
			addError(node, Error.TYPE_MIS, "No create method found with signature " + arguments);
			node.setType(Type.UNKNOWN);				
		}
		else
		{
			//we have a create list
			TreeMap<MethodSignature, MethodType> acceptableCreates = new TreeMap<MethodSignature, MethodType>();
			
			for( MethodSignature signature : candidateCreates ) 
			{		
				MethodType methodType = signature.getMethodType();
				
				if( signature.matches( arguments )) 
				{
					//found it, don't look further!
					acceptableCreates.clear();
					acceptableCreates.put(signature, methodType);
					break;
				}
				else if( signature.canAccept( arguments ))
					acceptableCreates.put(signature, methodType);
			}
			
			if( acceptableCreates.size() == 0 ) 
			{
				addError(node, Error.TYPE_MIS, "No create found with signature " + arguments);
				node.setType(Type.UNKNOWN);
			}					
			else if( acceptableCreates.size() > 1 )
			{
				addError(node, Error.TYPE_MIS, "Ambiguous create call with signature " + arguments);
				node.setType(Type.UNKNOWN);
			}					
			else
			{
				Entry<MethodSignature, MethodType> entry = acceptableCreates.firstEntry();
				MethodSignature signature = entry.getKey(); 
				MethodType methodType = entry.getValue();				 
				
				if( !methodIsAccessible( signature, currentType  ))
					addError(node, Error.INVL_MOD, "Create " + signature + " not accessible from current context");
				else
				{
					node.setType(methodType);
				}
			}				
		}
		
	}
	
	protected void setMethodType( ASTMethodCall node, List<MethodSignature> methods, SequenceType typeArguments, SequenceType arguments )
	{
		TreeMap<MethodSignature, MethodType> acceptableMethods = new TreeMap<MethodSignature, MethodType>();
		boolean hasTypeArguments = typeArguments != null;				
		
		for( MethodSignature signature : methods ) 
		{				
			MethodType methodType = signature.getMethodType();
			
			if( methodType.isParameterized() )
			{
				if( hasTypeArguments )
				{	
					SequenceType parameters = methodType.getTypeParameters();							
					if( parameters.canAccept(typeArguments))
						methodType = methodType.replace(parameters, typeArguments);
					else
						continue;
				}
			}				
			
			if( methodType.matches( arguments ) )
			{						
				//perfect match, forget everything else
				acceptableMethods.clear();
				acceptableMethods.put(signature, methodType);
				break;
			}
			else if( methodType.canAccept( arguments ))
				acceptableMethods.put(signature, methodType);
		}
			
	
		if( acceptableMethods.size() == 0 )	
		{									
			addError(node, Error.TYPE_MIS, "No method found with signature " + arguments);
			node.setType(Type.UNKNOWN);
		}
		else if( acceptableMethods.size() > 1 )
		{									
			addError(node, Error.TYPE_MIS, "Ambiguous method call with signature " + arguments);
			node.setType(Type.UNKNOWN);
		}							
		else //exactly one thing
		{					
			Entry<MethodSignature, MethodType> entry = acceptableMethods.firstEntry();
			MethodSignature signature = entry.getKey(); 
			MethodType methodType = entry.getValue();
			
			if( !methodIsAccessible( signature, currentType  ))					
				addError(node, Error.INVL_MOD, "Method " + signature + " is not accessible from current context");						
				
			if( !methodType.getOuter().getModifiers().isImmutable() && !signature.isCreate() && !currentMethod.isEmpty() && currentMethod.getFirst().getModifiers().isImmutable() && !methodType.getModifiers().isImmutable())
				addError(node, Error.INVL_MOD, "Mutable method " + signature + " cannot be called from an immutable method");
	
			node.setType(methodType);
		}		
	}
	
	public Object visit(ASTMethodCall node, Boolean secondVisit) throws ShadowException	
	{		
		if( secondVisit )
		{			
			//always part of a suffix, thus always has a prefix
			ModifiedType prefixNode = curPrefix.getFirst();
			prefixNode = resolveType( prefixNode );
			Type prefixType = prefixNode.getType();
			
			int start = 0;			
			SequenceType typeArguments = null;
			
			if( node.jjtGetNumChildren() > 0 && (node.jjtGetChild(0) instanceof ASTTypeArguments) )
			{
				start = 1;			
				typeArguments = (SequenceType) node.jjtGetChild(0).getType();
			}
			
			SequenceType arguments = new SequenceType();
			
			for( int i = start; i < node.jjtGetNumChildren(); i++ )
				arguments.add(node.jjtGetChild(i));
			
		
			if( prefixType instanceof UnboundMethodType )
			{
				UnboundMethodType unboundMethod = (UnboundMethodType)(prefixType);
				List<MethodSignature> methods = prefixType.getOuter().getMethods(unboundMethod.getTypeName());
				setMethodType(node, methods, typeArguments, arguments); //type set inside									
			}			
			else if( prefixType instanceof MethodType ) //only happens with method pointers
			{
				MethodType methodType = (MethodType)prefixType;
				
				if( methodType.isParameterized() )
				{
					if( typeArguments != null )
					{						
						SequenceType parameters = methodType.getTypeParameters();
						if( parameters.canAccept(typeArguments))
							methodType = methodType.replace(parameters, typeArguments);
						else
						{
							addError(node, Error.TYPE_MIS, "Type parameters " + parameters + " do not match method given by " + node);
							node.setType(Type.UNKNOWN);
						}							
					}
				}				
				
				if( methodType.canAccept( arguments ) )
					node.setType(methodType);
				else 
				{
					addError(node, Error.TYPE_MIS, "Cannot apply arguments " + arguments + " to method given by " + node);
					node.setType(Type.UNKNOWN);
				}
				
				if( !currentMethod.isEmpty() && currentMethod.getFirst().getModifiers().isImmutable() && !methodType.getModifiers().isImmutable())
					addError(node, Error.INVL_MOD, "Mutable method cannot be called from an immutable method");				
			}			
			else
			{									
				addError(node, Error.TYPE_MIS, "Cannot apply arguments to non-method type " + prefixType);
				node.setType(Type.UNKNOWN);			
			}
		}
		
		return WalkType.POST_CHILDREN;
	}
	
	public static boolean fieldIsAccessible( Node field, Type type )
	{
		//inside class or constant
		if ( field.getModifiers().isConstant() ) 
			return true;		
		
		type = type.getTypeWithoutTypeArguments();
		Type enclosing = field.getEnclosingType().getTypeWithoutTypeArguments();
		
		while( type != null )
		{
			if( enclosing.equals(type) )
				return true;
			
			type = type.getOuter();
		}

		return false;
	}

	
	/*
	public Object visit(ASTLabeledStatement node, Boolean secondVisit) throws ShadowException 
	{ 
		if(!secondVisit)
		{
			String label = node.getImage();
			if(findSymbol(label) != null || labels.contains(label))
				addError(node, Error.MULT_SYM, label);
			else
				labels.push(node);	
		}
		else
			labels.pop();
			
		return WalkType.POST_CHILDREN;
	}
	*/
	
	public Object visit(ASTBreakStatement node, Boolean secondVisit) throws ShadowException 
	{ 
		if( !node.getImage().isEmpty() )
		{
			for( Node label : labels )			
				if( label.equals(node.getImage()) )
					return WalkType.PRE_CHILDREN;
				
			addError(node, Error.UNDEC_VAR, "No matching label for break statement");			
		}
			
		return WalkType.PRE_CHILDREN;
	}
	
	/*
	public Object visit(ASTArrayDimsAndInits node, Boolean secondVisit) throws ShadowException 
	{		
		if( secondVisit )
		{
			Node child = node.jjtGetChild(0);
			if( child instanceof ASTArrayInitializer ) //check this closely
			{
				if( visitArrayInitializer( child )  )
				{					
					ArrayType arrayType = (ArrayType) child.getType();
					if( node.getArrayDimensions().size() != arrayType.getDimensions() )
						addError(child, Error.INVL_TYP, "Dimensions do not match array initializer");
					//do we need more checks here?
				}								
			}
			else
			{
				for( int i = 0; i < node.jjtGetNumChildren(); i++ )
				{
					child = node.jjtGetChild(i); 
					if( !child.getType().isNumerical() )
					{
						addError(child, Error.INVL_TYP, "Numerical type must be specified for array dimensions");				
						break;
					}
				}
			}
		}
			
	
		return WalkType.POST_CHILDREN;
	}
	
	*/
	
	private boolean visitArrayInitializer(Node node)
	{
		Node child = node.jjtGetChild(0);
		Type result = child.getType();
		
		for( int i = 1; i < node.jjtGetNumChildren(); i++ ) //cycle through types, upgrading to broadest legal type
		{
			child = node.jjtGetChild(i);
			Type type = child.getType();
							
			if( result.isSubtype(type) )					
				result = type;				
			else if( !type.isSubtype(result) ) //neither is subtype of other, panic!
			{
				addError(node, Error.INVL_TYP, "Types in array initializer list do not match");
				node.setType(Type.UNKNOWN);
				return false;
			}
		}

			
		ArrayList<Integer> dimensions = new ArrayList<Integer>();
		Type baseType;
		if( result instanceof ArrayType )
		{
			ArrayType arrayType = (ArrayType)result;
			dimensions.add(arrayType.getDimensions() + 1 );
			baseType = arrayType.getBaseType();
		}
		else
		{
			dimensions.add(1);
			baseType = result;
		}		
		ArrayType arrayType = new ArrayType( baseType, dimensions );
		node.setType(arrayType);
		((ClassType)currentType).addReferencedType(arrayType);
		
		return true;		
	}
	
	public Object visit(ASTArrayInitializer node, Boolean secondVisit) throws ShadowException 
	{		
		if( secondVisit )
			visitArrayInitializer(node);		
	
		return WalkType.POST_CHILDREN;
	}
	
	public Object visit(ASTAssertStatement node, Boolean secondVisit) throws ShadowException 
	{
		if(!secondVisit)
			return WalkType.POST_CHILDREN;
	
		Type assertType = node.jjtGetChild(0).getType();
		
		if( !assertType.equals(Type.BOOLEAN))
			addError(node, Error.INVL_TYP, "Found type " + assertType + ", but boolean type required for assert condition");
		
		if( node.jjtGetNumChildren() > 1 )
		{
			Node child = node.jjtGetChild(1);
			Type type = child.getType();
			if( type == null )
				addError(node, Error.INVL_TYP, "Value type required for assert information and no type found");
			else if( child.getModifiers().isTypeName() )
				addError(node, Error.INVL_TYP, "Value type required for assert information but type name used");
		}
		
		return WalkType.POST_CHILDREN;
	}
	
	@Override
	public Object visit(ASTImportDeclaration node, Boolean secondVisit) throws ShadowException {
		currentPackage = node.getPackage();		
		return WalkType.NO_CHILDREN;
	}
	
	@Override
	public Object visit(ASTCompilationUnit node, Boolean secondVisit) throws ShadowException {
		if( !secondVisit )
			currentPackage = packageTree;
		
		return WalkType.POST_CHILDREN;			
	}
	
	@Override
	public Object visit(ASTLiteral node, Boolean secondVisit) throws ShadowException {						
		node.setType(literalToType(node.getLiteral()));		
		return WalkType.NO_CHILDREN;			
	}
	
	@Override
	public Object visit(ASTPrimitiveType node, Boolean secondVisit) throws ShadowException {		
		node.setType(nameToPrimitiveType(node.getImage()));		
		return WalkType.NO_CHILDREN;			
	}

	/*
	@Override
	public Object visit(ASTArrayAllocation node, Boolean secondVisit) throws ShadowException
	{
		if(secondVisit)
		{	
			ASTAllocationExpression parent = (ASTAllocationExpression) node.jjtGetParent();

			Node child = node.jjtGetChild(0);				
			int counter = 1;
			
			if( child instanceof ASTClassOrInterfaceType && node.jjtGetChild(counter) instanceof ASTTypeArguments ) //reference array might have type arguments
			{
				//for now
				addError(node.jjtGetChild(counter), Error.INVL_TYP, "Generics are not yet handled");
				node.setType(Type.UNKNOWN);
				counter++;				
			}
				
			//array dims and inits
			List<Integer> dimensions = ((ASTArrayDimsAndInits)(node.jjtGetChild(counter))).getArrayDimensions();
			node.setType(new ArrayType(child.getType(), dimensions));
		}
		
		
		return WalkType.POST_CHILDREN;
	}
	*/
	

	
	
	
		
	@Override
	public Object visit(ASTReturnStatement node, Boolean secondVisit)	throws ShadowException
	{
		if( secondVisit )
		{
			if( currentMethod.isEmpty() ) //should never happen			
				addError(node, Error.INVL_TYP, "Return statement outside of method body");
			else
			{
				MethodType methodType = (MethodType)(currentMethod.getFirst().getType());
				SequenceType returnTypes  = methodType.getReturnTypes();
				node.setType(returnTypes);
				
				if( returnTypes.size() == 0 )
				{
					if( node.jjtGetNumChildren() > 0 )
						addError(node, Error.INVL_TYP, "Cannot return values from a method that returns nothing");
				}
				else
				{
					Node child = node.jjtGetChild(0);
					Type type = child.getType();
					List<String> reasons = new ArrayList<String>(1);
					
					if( type instanceof SequenceType )
					{					
						SequenceType sequenceType = (SequenceType)type;
						if( !returnTypes.canAccept(sequenceType, reasons) )						
							addError(node, Error.TYPE_MIS, reasons.get(0) );
					}
					else if(!returnTypes.canAccept(child, reasons))
						addError(node, Error.TYPE_MIS, reasons.get(0) );						
				}				
				
				node.setType(returnTypes);
			}
		}	
		
		return WalkType.POST_CHILDREN;
	}
	
	public Object visit(ASTResultType node, Boolean secondVisit) throws ShadowException
	{
		if( secondVisit )			
		{
			Type type = node.jjtGetChild(1).getType(); //child 0 is always Modifiers 
			node.setType(type);
			
			if( node.getModifiers().isNullable() && type.isPrimitive() )
				addError(node, Error.TYPE_MIS, "Cannot mark primitive type " + type + " as nullable");				
		}
		
		return WalkType.POST_CHILDREN;	
	}
	
	public Object visit(ASTExplicitCreateInvocation node, Boolean secondVisit) throws ShadowException {
	
		if( secondVisit )
		{
			SequenceType arguments = (SequenceType) node.jjtGetChild(0).getType();				
			
			if( currentType instanceof ClassType && ((ClassType)currentType).getExtendType() != null )
			{
				ClassType type = (ClassType) currentType; //assumes "this" 
				if( node.getImage().equals("super") )								
					type = type.getExtendType();
								
				boolean found = false;
				for( MethodSignature method : type.getMethods("create") )
				{
					if( method.matches(arguments) )
					{
						found = true;
						break;						
					}
				}
				
				if( !found )
					addError(node, Error.TYPE_MIS, "No create matches arguments " + arguments);
				
			}	
			else
				addError(node, Error.INVL_TYP, "Cannot call explicit create on non-class type");
			
			ASTCreateDeclaration parent = (ASTCreateDeclaration) node.jjtGetParent();
			parent.setExplicitInvocation(true);
		}		
		
		return WalkType.POST_CHILDREN; 
	}
	

	// Everything below here are visitors to push up the type

	public Object visit(ASTType node, Boolean secondVisit) throws ShadowException { return pushUpType(node, secondVisit); }
	
	public Object visit(ASTVariableInitializer node, Boolean secondVisit) throws ShadowException {
		if( !secondVisit )
			return WalkType.POST_CHILDREN;
		
		Node child = node.jjtGetChild(0);
		Type childType = child.getType(); 
		if( childType instanceof PropertyType )
		{
			PropertyType propertyType = (PropertyType) childType;
			if( propertyType.isGettable() )
			{
				node.setType(propertyType.getGetType().getType());
				node.setModifiers(propertyType.getGetType().getModifiers());					
			}
			else
			{
				node.setType(Type.UNKNOWN);
				addError(child, Error.TYPE_MIS, "Property " + child + "does not have get access.");
			}				
		}
		else
		{
			node.setType(child.getType());
			node.setModifiers(child.getModifiers());
		}			

		
		return WalkType.POST_CHILDREN; 
	}
	
	public Object visit(ASTExtendsList node, Boolean secondVisit) throws ShadowException { 
		return WalkType.NO_CHILDREN;
	}
	
	public Object visit(ASTImplementsList node, Boolean secondVisit) throws ShadowException { 
		return WalkType.NO_CHILDREN;
	}
	
	public Object visit(ASTMethodDeclarator node, Boolean secondVisit) throws ShadowException 
	{ 
		if( node.jjtGetParent() instanceof ASTLocalMethodDeclaration ) //non local already handled
			return WalkType.POST_CHILDREN;		
		else
			return WalkType.NO_CHILDREN;		
	}
	
}
