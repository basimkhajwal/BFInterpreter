import scala.scalajs.js.annotation.JSExport
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom.document

object Test {

  @JSExport
  def main(args: Array[String]): Unit = {
    println("Testing...")
    <.div(<.h3("Testing..")).renderIntoDOM(document.getElementById("container"))
  }
}
