/* Generated By:JJTree: Do not edit this line. ASTBitwiseOrExpression.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=true,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package shadow.parser.javacc;

public
@SuppressWarnings("all")
class ASTBitwiseOrExpression extends OperationNode {
  public ASTBitwiseOrExpression(int id) {
    super(id);
  }

  public ASTBitwiseOrExpression(ShadowParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(ShadowParserVisitor visitor, Boolean secondVisit) throws ShadowException {
    return visitor.visit(this, secondVisit);
  }
}
/* JavaCC - OriginalChecksum=63e8ac04a353b2213a34fa071a34a0b6 (do not edit this line) */
