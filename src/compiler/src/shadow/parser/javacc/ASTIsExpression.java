/* Generated By:JJTree: Do not edit this line. ASTIsExpression.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=true,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package shadow.parser.javacc;

public
@SuppressWarnings("all")
class ASTIsExpression extends SimpleNode {
  public ASTIsExpression(int id) {
    super(id);
  }

  public ASTIsExpression(ShadowParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(ShadowParserVisitor visitor, Object data) throws ShadowException {
    return visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=f151bc50a7b08d3613b0c991f838e4a8 (do not edit this line) */