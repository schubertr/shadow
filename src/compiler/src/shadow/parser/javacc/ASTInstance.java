/* Generated By:JJTree: Do not edit this line. ASTInstance.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=true,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package shadow.parser.javacc;

public
@SuppressWarnings("all")
class ASTInstance extends SimpleNode {
  public ASTInstance(int id) {
    super(id);
  }

  public ASTInstance(ShadowParser p, int id) {
    super(p, id);
  }
  
  /** Accept the visitor. **/
  public Object jjtAccept(ShadowParserVisitor visitor, Boolean data) throws ShadowException {
    return visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=54d027fbf28fe90cc9ab5bdd745d3424 (do not edit this line) */
