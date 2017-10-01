sealed trait BFCommand
object Add extends BFCommand
object Subtract extends BFCommand
object ShiftLeft extends BFCommand
object ShiftRight extends BFCommand
object Input extends BFCommand
object Output extends BFCommand
case class Loop(xs: List[(BFCommand, Int)]) extends BFCommand

case class BFProgram(private var steps: List[(BFCommand, Int)], private var input: List[Char]) {

  private val tape = new Array[Int](1000)
  private var tapePointer: Int = 500

  def reset(source: String, inp: String): Unit = {
    reset(BFInterpreter.parse(source.toList), inp.toList)
  }

  def reset(s: List[(BFCommand, Int)], i: List[Char]): Unit = {
    steps = s
    input = i
    tapePointer = 500
    for (i <- tape.indices) tape(i) = 0
  }

  def tapeValue(pos: Int) = tape(pos)
  def tapeLocation = tapePointer

  def done = steps.isEmpty

  def hasOutput = !done && steps.head._1 == Output
  def output = tape(tapePointer).toChar

  def currentStep: (BFCommand, Int) = steps.head

  def step: Unit = {
    steps.head._1 match {
      case Add => tape(tapePointer) += 1
      case Subtract => tape(tapePointer) -= 1
      case ShiftLeft => tapePointer -= 1
      case ShiftRight => tapePointer += 1
      case Loop(xs) if tape(tapePointer) != 0 => {
        steps = (Add, -1) :: (xs ++ steps)
      }

      case Input => input match {
        case Nil =>
        case x::xs => {
          tape(tapePointer) = x.toInt
          input = xs
        }
      }

      case _ =>
    }

    steps = steps.tail
  }
}

object BFInterpreter {

  def parse(source: List[Char], loc: Int = 0): List[(BFCommand, Int)] = source match {
    case x::xs => {
      x match {
        case '+' => (Add, loc) :: parse(xs, loc+1)
        case '-' => (Subtract, loc) :: parse(xs, loc+1)
        case '>' => (ShiftRight, loc) :: parse(xs, loc+1)
        case '<' => (ShiftLeft, loc) :: parse(xs, loc+1)
        case '.' => (Output, loc) :: parse(xs, loc+1)
        case ',' => (Input, loc) :: parse(xs, loc+1)
        case '[' => {
          val (inner, rest) = findMatching(1, xs, Nil)
          (Loop(parse(inner, loc+1)), loc) :: parse(rest, loc + inner.length + 2)
        }
        case _ => parse(xs, loc+1)
      }
    }
    case _ => Nil
  }

  def findMatching(depth: Int, source: List[Char], acc: List[Char]): (List[Char], List[Char]) = source match {
    case Nil => (acc.reverse, Nil)
    case '['::xs => findMatching(depth+1, xs, '['::acc)
    case ']'::xs =>
      if (depth == 1) (acc.reverse, xs)
      else            findMatching(depth-1, xs, ']'::acc)
    case x::xs => findMatching(depth, xs, x::acc)
  }
}
