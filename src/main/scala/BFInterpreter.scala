sealed trait BFCommand
object Add extends BFCommand
object Subtract extends BFCommand
object ShiftLeft extends BFCommand
object ShiftRight extends BFCommand
object Input extends BFCommand
object Output extends BFCommand
case class Loop(xs: List[BFCommand]) extends BFCommand

case class BFProgram(private var steps: List[BFCommand], private var input: List[Char]) {

  private val tape = new Array[Int](1000)
  private var tapePointer: Int = 500

  def reset(s: List[BFCommand], i: List[Char]): Unit = {
    steps = s
    input = i
    tapePointer = 500
    for (i <- tape.indices) tape(i) = 0
  }

  def tapeValue(pos: Int) = tape(pos)
  def tapeLocation = tapePointer

  def done = steps.isEmpty

  def hasOutput = steps.head == Output
  def output = tape(tapePointer).toChar

  def step: Unit = {
    steps.head match {
      case Add => tape(tapePointer) += 1
      case Subtract => tape(tapePointer) -= 1
      case ShiftLeft => tapePointer -= 1
      case ShiftRight => tapePointer += 1
      case Loop(xs) if tape(tapePointer) != 0 => {
        steps = Add :: (xs ++ steps)
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

  def parse(source: List[Char]): List[BFCommand] = source match {
    case x::xs => {
      x match {
        case '+' => Add :: parse(xs)
        case '-' => Subtract :: parse(xs)
        case '>' => ShiftRight :: parse(xs)
        case '<' => ShiftLeft :: parse(xs)
        case '.' => Output :: parse(xs)
        case ',' => Input :: parse(xs)
        case '[' => {
          val (inner, rest) = findMatching(1, xs, Nil)
          Loop(parse(inner)) :: parse(rest)
        }
        case _ => Nil
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
