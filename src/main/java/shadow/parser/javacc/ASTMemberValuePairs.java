/* Generated By:JJTree: Do not edit this line. ASTMemberValuePairs.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=true,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package shadow.parser.javacc;

public
@SuppressWarnings("all")
class ASTMemberValuePairs extends SimpleNode {
  public ASTMemberValuePairs(int id) {
    super(id);
  }

  public ASTMemberValuePairs(ShadowParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(ShadowParserVisitor visitor, Boolean secondVisit) throws ShadowException {
    return visitor.visit(this, secondVisit);
  }
}
/* JavaCC - OriginalChecksum=0ca7aa9ca7ed50abfc7c7cad3a6e9d83 (do not edit this line) */
