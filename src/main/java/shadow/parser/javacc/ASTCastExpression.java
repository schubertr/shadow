/* Generated By:JJTree: Do not edit this line. ASTCastExpression.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=true,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package shadow.parser.javacc;

public
@SuppressWarnings("all")
class ASTCastExpression extends SimpleNode {
  public ASTCastExpression(int id) {
    super(id);
  }

  public ASTCastExpression(ShadowParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(ShadowParserVisitor visitor, Boolean secondVisit) throws ShadowException {
    return visitor.visit(this, secondVisit);
  }
}
/* JavaCC - OriginalChecksum=af0d19ad4c7166cc5ad32bdc00ff1ba1 (do not edit this line) */