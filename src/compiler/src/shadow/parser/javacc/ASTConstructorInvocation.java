/* Generated By:JJTree: Do not edit this line. ASTConstructorInvocation.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=true,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package shadow.parser.javacc;

public
@SuppressWarnings("all")
class ASTConstructorInvocation extends SimpleNode {
  public ASTConstructorInvocation(int id) {
    super(id);
  }

  public ASTConstructorInvocation(ShadowParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(ShadowParserVisitor visitor, Boolean data) throws ShadowException {
    return visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=95eee80eb761cbc0445cb30eb14b4730 (do not edit this line) */