/* Generated By:JJTree: Do not edit this line. ASTConditionalAndExpression.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=true,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package shadow.parser.javacc;

public
@SuppressWarnings("all")
class ASTConditionalAndExpression extends SimpleNode {
  public ASTConditionalAndExpression(int id) {
    super(id);
  }

  public ASTConditionalAndExpression(ShadowParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(ShadowParserVisitor visitor, Boolean secondVisit) throws ShadowException {
    return visitor.visit(this, secondVisit);
  }
}
/* JavaCC - OriginalChecksum=b132ee7ead32e8f405291a532c427fc3 (do not edit this line) */
