import scala.scalajs.js.annotation.JSExport
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom.document

object BFInterpreter {

  val app =
    <.div(^.id := "bf-interpreter") (

      <.div(^.id := "source")(
        <.h3("Source"),

        <.textarea(
          ^.rows := 10, ^.cols := 50,
          ^.placeholder := "Enter program..."
        )
      ),

      <.div(^.id := "controls")(
        <.button(^.className := "btn btn-success", ^.id := "play-btn")(
          <.i(^.className := "glyphicon glyphicon-play")
        ),

        <.button(^.className := "btn btn-danger", ^.id := "stop-btn", ^.disabled := true)(
          <.i(^.className := "glyphicon glyphicon-stop")
        )
      ),

      <.div(^.id := "output")(
        <.h3("Output"),

        <.textarea(
          ^.rows := 10, ^.cols := 50,
          ^.disabled := true
        )
      )
    )

  @JSExport
  def main(args: Array[String]): Unit = {
    app.renderIntoDOM(document.getElementById("container"))
  }
}
