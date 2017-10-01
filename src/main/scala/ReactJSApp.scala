import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react._
import org.scalajs.dom.{document, html}

import scala.scalajs.js.{JSApp, timers}
import scala.scalajs.js.timers.SetIntervalHandle


object Tape {

  case class Props(program: BFProgram, leftPosition: Int)

  class Backend($: BackendScope[Props, Unit]) {
    def render(props: Props): VdomElement = {
      val p = props.program
      <.div(^.id := "tape")(
        (props.leftPosition to (props.leftPosition + 10)).map { i =>
          <.span(
            ^.className := (if (i == p.tapeLocation) "tape-value selected" else "tape-value"),
          )(
            p.tapeValue(i)
          )
        } toTagMod
      )
    }
  }

  private val component = ScalaComponent.builder[Props]("Tape").renderBackend[Backend].build

  def apply(program: BFProgram, lpos: Int) = component(Props(program, lpos))
}

@JSExportTopLevel("ReactJSApp")
object ReactJSApp {

  case class AppState(
    playing: Boolean,
    program: BFProgram,
    sourceText: String = "",
    outputText: String = "",
    leftPosition: Int = 495
  )

  class AppBackend($: BackendScope[Unit, AppState]) {

    private var sourceRef: html.TextArea = _
    private var inputRef: html.Input = _

    private var timerHandle: SetIntervalHandle = _

    private val startProgram: Callback =
      for {
        input <- CallbackTo { inputRef.value }
        _ <- $.state map (state => state.program.reset(state.sourceText, input))
        _ <- $.modState(_.copy(playing = true, outputText = "", leftPosition = 495))
        _ <- Callback {
          timerHandle = timers.setInterval(100) {
            timerTick.runNow()
          }
        }
      } yield ()

    private val timerTick: Callback =
      for {
        state <- $.state
        p = state.program
        _ <-
          if (p.hasOutput)
            $.setState(state.copy(outputText = state.outputText + p.output))
          else Callback.empty
        _ <- Callback { p.step }
        _ <-
          if (p.tapeLocation < state.leftPosition) {
            $.modState(_.copy(leftPosition = p.tapeLocation))
          } else if (p.tapeLocation > state.leftPosition + 10) {
            $.modState(_.copy(leftPosition = p.tapeLocation - 10))
          } else {
            Callback.empty
          }

        _ <- if (p.done) stopProgram else $.forceUpdate
      } yield ()

    private val stopProgram: Callback =
      for {
        _ <- $.modState(_.copy(playing = false))
        _ <- Callback { timers.clearInterval(timerHandle) }
      } yield ()

    private val sourceChanged: Callback =
      CallbackTo { sourceRef.value } >>= { s => $.modState(_.copy(sourceText = s))}

    def render(state: AppState): VdomElement = {

      <.div(^.id := "bf-interpreter") (

        Tape(state.program, state.leftPosition),

        <.div(^.className := "horizontal")(
          <.div(^.id := "source")(
            <.h3("Source"),

            if (state.playing) {

              val pos = state.program.currentStep._2
              val raw = state.sourceText

              <.div(^.id := "source-play")(
                <.p(
                  ^.dangerouslySetInnerHtml :=
                    raw.substring(0, pos-1) +
                    "<span class=\"selected\">" + raw.charAt(pos) + "</span>" +
                    raw.substring(pos+1)
                )
              )
            } else {

              <.textarea(
                ^.rows := 10, ^.cols := 40, ^.placeholder := "Enter program...",
                ^.value := state.sourceText, ^.onChange --> sourceChanged
              ).ref(sourceRef = _)
            }

          ),

          <.div(^.id := "input")(
            <.h3("Input"),
            <.input(
              ^.`type` := "text", ^.width := "350px", ^.cols := 50,
              ^.disabled := state.playing
            )
              .ref(inputRef = _)
          )
        ),

        <.div(^.id := "controls", ^.className := "horizontal")(
          <.div(^.id := "inner")(

            <.button(
              ^.className := "btn btn-success", ^.id := "play-btn",
              ^.onClick --> startProgram, ^.disabled := state.playing
            ) ( <.i(^.className := "glyphicon glyphicon-play") )
            ,

            <.button(
              ^.className := "btn btn-danger", ^.id := "stop-btn",
              ^.onClick --> stopProgram,
              ^.disabled := !state.playing
            )( <.i(^.className := "glyphicon glyphicon-stop") )
          )
        ),

        <.div(^.id := "output", ^.className := "horizontal")(
          <.h3("Output"),
          <.textarea(
            ^.rows := 10, ^.cols := 40,
            ^.value := state.outputText,
            ^.disabled := true
          )
        )
      )
    }
  }

  val App = ScalaComponent.builder[Unit]("App")
    .initialState(AppState(false, BFProgram(Nil, Nil)))
    .renderBackend[AppBackend].build

  @JSExport
  def launch(): Unit = {
    ReactJSApp.App().renderIntoDOM(document.getElementById("container"))
  }
}
