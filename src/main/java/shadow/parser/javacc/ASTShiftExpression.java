/* Generated By:JJTree: Do not edit this line. ASTShiftExpression.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=true,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package shadow.parser.javacc;

public
@SuppressWarnings("all")
class ASTShiftExpression extends OperationNode {
  public ASTShiftExpression(int id) {
    super(id);
  }

  public ASTShiftExpression(ShadowParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(ShadowParserVisitor visitor, Boolean secondVisit) throws ShadowException {
    return visitor.visit(this, secondVisit);
  }
}
/* JavaCC - OriginalChecksum=a095ce9cef4e8ae7d3890603eb3cf397 (do not edit this line) */
