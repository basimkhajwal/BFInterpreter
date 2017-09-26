import scala.scalajs.js.annotation.JSExport
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react._

import org.scalajs.dom.document

object ReactJSApp {

  type TapeProps = BFTape

  class TapeBackend($: BackendScope[TapeProps, Unit])

  case class AppState()

  class AppBackend($: BackendScope[Unit, AppState]) {

    def render(state: AppState): VdomElement = {
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

        <.div(
          <.div(^.id := "input")(
            <.h3("Input"),
            <.textarea(^.rows := 10, ^.cols := 50)
          ),
          <.div(^.id := "output")(
            <.h3("Output"),

            <.textarea(
              ^.rows := 10, ^.cols := 50,
              ^.disabled := true
            )
          )
        )
      )
    }
  }

  val App =
    ScalaComponent
      .builder[AppProps]("App")
      .initialState(AppState())
      .renderBackend[AppBackend]
      .build

  @JSExport
  def main(args: Array[String]): Unit = {
    App(AppProps()).renderIntoDOM(document.getElementById("container"))
  }
}
