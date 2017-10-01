import com.thoughtworks.binding.Binding.{BindingSeq, Var, Vars}
import com.thoughtworks.binding.{Binding, dom}

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import org.scalajs.dom._
import org.scalajs.dom.html.{Div, Input, TextArea}

import scala.scalajs.js.timers
import scala.scalajs.js.timers.SetIntervalHandle

@JSExportTopLevel("BindingApp")
object BindingApp {

  implicit def makeIntellijHappy[T<:raw.Node](x: scala.xml.Node): Binding[T] =
    throw new AssertionError("This should never execute.")

  case class State(
    program: BFProgram = BFProgram(Nil, Nil),
    isPlaying: Var[Boolean] = Var(false),
    sourceText: Var[String] = Var(""),
    outputText: Var[String] = Var(""),
    tapeLocation: Var[Int] = Var(500),
    sourcePosition: Var[Int] = Var(0),
    leftPosition: Var[Int] = Var(495),
    tapeValues: Vars[(Int, Int)] = Vars((0 to 10) map ((_, 0)) :_*),
    var timerHandle: SetIntervalHandle = null
  )

  def startProgram(state: State): Unit = {
    state.outputText := ""
    state.leftPosition := 495

    state.program.reset(state.sourceText.get, inputField.value)
    updateProgram(state)

    state.timerHandle = timers.setInterval(100) { timerTick(state) }
    state.isPlaying := true
  }

  def updateProgram(state: State): Unit = {
    if (state.program.hasOutput) state.outputText := state.outputText.get + state.program.output
    state.tapeLocation := state.program.tapeLocation

    if (state.program.tapeLocation < state.leftPosition.get) state.leftPosition := state.program.tapeLocation
    if (state.program.tapeLocation > state.leftPosition.get + 10) state.leftPosition := state.program.tapeLocation - 10

    state.sourcePosition := state.program.currentStep._2
    for (i <- 0 to 10) state.tapeValues.get(i) = (i, state.program.tapeValue(state.leftPosition.get + i))
  }

  def timerTick(state: State): Unit = {
    state.program.step
    if (state.program.done) stopProgram(state)
    updateProgram(state)
  }

  def stopProgram(state: State): Unit = {
    state.isPlaying := false
    timers.clearInterval(state.timerHandle)
  }

  def inputField = document.getElementById("inputField").asInstanceOf[Input]

  @dom
  def tape(leftPos: Binding[Int], tapeLoc: Binding[Int], tapeValues: BindingSeq[(Int, Int)]): Binding[Div] = {
    <div id="tape">
      {
        for ((i, t) <- tapeValues) yield {
          <span class={ if (leftPos.bind + i == tapeLoc.bind) "tape-value selected" else "tape-value" }>
            { t.toString }
          </span>
        }
      }
    </div>
  }

  @dom
  def app(state: State): Binding[Div] = {
    <div id="bf-interpreter">

      { tape(state.leftPosition, state.tapeLocation, state.tapeValues).bind }

      <div class="horizontal">
        <div id="source">
          <h3>Source</h3>
          {
            if (state.isPlaying.bind) {
              if (!state.isPlaying.bind) println("???")
              <div id="source-play">
                { state.sourceText.bind.substring(0, state.sourcePosition.bind-1) }
                <span class="selected">{ state.sourceText.bind.charAt(state.sourcePosition.bind).toString }</span>
                { state.sourceText.bind.substring(state.sourcePosition.bind+1) }
              </div>
            } else {
                <textarea rows={10} cols={40} placeholder="Enter program..." value={state.sourceText.bind}
                  onchange={ e:Event => state.sourceText := e.target.asInstanceOf[TextArea].value } />
            }
          }
        </div>

        <div id="input">
          <h3>Input</h3>
          <input id="inputField" type="text" width="350px" />
        </div>
      </div>

      <div id="controls" class="horizontal">
        <div id="inner">
          <button class="btn btn-success" id="play-btn"
            disabled = { state.isPlaying.bind } onclick = { _:Event => startProgram(state) } >
            <i class="glyphicon glyphicon-play"/>
          </button>

          <button class="btn btn-danger" id="stop-btn"
            disabled = { !state.isPlaying.bind } onclick = { _:Event => stopProgram(state) } >
            <i class="glyphicon glyphicon-stop"/>
          </button>
        </div>
      </div>

      <div id="output" class="horizontal">
        <h3>Output</h3>
        <textarea rows={10} cols={40} value={state.outputText.bind} disabled={true}/>
      </div>
    </div>
  }

  @JSExport
  def launch(): Unit = {
    dom.render(document.getElementById("container"), app(State()))
  }
}
