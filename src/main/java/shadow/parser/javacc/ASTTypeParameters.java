/* Generated By:JJTree: Do not edit this line. ASTTypeParameters.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=true,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package shadow.parser.javacc;

import java.util.ArrayList;
import java.util.List;

import shadow.typecheck.type.ModifiedType;
import shadow.typecheck.type.SequenceType;


public
@SuppressWarnings("all")
class ASTTypeParameters extends SequenceNode
{
	
  public ASTTypeParameters(int id)
  {
    super(id);
  }

  public ASTTypeParameters(ShadowParser p, int id)
  {
    super(p, id);    
  } 

  /** Accept the visitor. **/
  public Object jjtAccept(ShadowParserVisitor visitor, Boolean secondVisit) throws ShadowException {
    return visitor.visit(this, secondVisit);
  }
}
/* JavaCC - OriginalChecksum=5c2352e94f339de5381aee1d3a50815e (do not edit this line) */
