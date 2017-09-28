import scala.scalajs.js.annotation.JSExport
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react._
import org.scalajs.dom.{document, html}

import scala.scalajs.js.timers
import scala.scalajs.js.timers.SetIntervalHandle


object Tape {

  case class Props(tape: BFTape)

  class Backend($: BackendScope[Props, Unit]) {
    def render(props: Props): VdomElement = {
      <.div(^.id := "tape")(
        props.tape.toList.map { tapeValue =>
          <.span(^.className := "tape-value")(tapeValue.toString)
        } toTagMod
      )
    }
  }

  private val component = ScalaComponent.builder[Props]("Tape").renderBackend[Backend].build

  def apply(tape: BFTape) = component(Props(tape))
}

object ReactJSApp {

  case class AppState(playing: Boolean, program: Option[BFProgram])

  class AppBackend($: BackendScope[Unit, AppState]) {

    private var sourceRef: html.TextArea = _

    private var timerHandle: SetIntervalHandle = _

    def getProgram: CallbackTo[BFProgram] = CallbackTo {
      BFProgram(
        BFTape(0, None, None),
        BFInterpreter.parse(sourceRef.value.toList)
      )
    }

    def startProgram: Callback =
      for {
        program <- getProgram
        _ <- $.setState(AppState(true, Some(program)))
        _ <- Callback {
          timerHandle = timers.setInterval(500) {
            timerTick.runNow()
          }
        }
      } yield ()

    def timerTick: Callback =
      $.state >>= (state => {
        val newProg = state.program.get.step

        if (newProg.done) {
          println("ITS A DONE")
          stopProgram
        } else {
          println(newProg.steps.length)
          $.setState(AppState(true, Some(newProg)))
        }
      })

    def stopProgram: Callback =
      $.setState(AppState(false, None)) >>
      Callback { timers.clearInterval(timerHandle) }

    def render(state: AppState): VdomElement = {

      <.div(^.id := "bf-interpreter") (

        state.program.whenDefined(p => Tape(p.tape)),

        <.div(^.id := "source", ^.className := "horizontal")(
          <.h3("Source"),

          <.textarea(
            ^.rows := 10, ^.cols := 50,
            ^.placeholder := "Enter program..."
          ).ref(sourceRef = _)
        ),

        <.div(^.id := "controls", ^.className := "horizontal")(
          <.div(^.id := "inner")(

            <.button(
              ^.className := "btn btn-success", ^.id := "play-btn",
              ^.onClick --> startProgram,
              ^.disabled := state.playing
            ) (
                <.i(^.className := "glyphicon glyphicon-play")
              )
            ,

            <.button(
              ^.className := "btn btn-danger", ^.id := "stop-btn",
              ^.onClick --> stopProgram,
              ^.disabled := !state.playing
            )(
              <.i(^.className := "glyphicon glyphicon-stop"),
            )
          )
        ),

        <.div(^.className := "horizontal")(
          <.div(^.id := "output")(
            <.h3("Output"),

            <.textarea(
              ^.rows := 10, ^.cols := 50,
              ^.disabled := true
            )
          ),
          <.div(^.id := "input")(
            <.h3("Input"),
            <.input(^.`type` := "text", ^.width := "350", ^.cols := 50)
          )
        )
      )
    }
  }

  val App = ScalaComponent.builder[Unit]("App")
    .initialState(AppState(false, None))
    .renderBackend[AppBackend].build

  @JSExport
  def main(args: Array[String]): Unit = {
    App().renderIntoDOM(document.getElementById("container"))
  }
}
