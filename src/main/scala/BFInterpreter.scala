sealed trait BFCommand
object Add extends BFCommand
object Subtract extends BFCommand
object ShiftLeft extends BFCommand
object ShiftRight extends BFCommand
object Input extends BFCommand
object Output extends BFCommand
case class Loop(xs: List[BFCommand]) extends BFCommand

case class BFTape(value: Int, left: Option[BFTape], right: Option[BFTape]) {
  def shiftLeft = left match {
    case Some(l) => l
    case None    => BFTape(0, None, Some(this))
  }
  def shiftRight = right match {
    case Some(r) => r
    case None    => BFTape(0, Some(this), None)
  }
  def add = BFTape(value+1, left, right)
  def subtract = BFTape(value-1, left, right)
  def set(newValue: Int) = BFTape(newValue, left, right)
}

object BFInterpreter {

  val COMMANDS = "+-><[].,"

  def parse(source: List[Char]): List[BFCommand] = source match {
    case Nil => Nil
    case x::xs => {
      x match {
        case '+' => Add :: parse(xs)
        case '-' => Subtract :: parse(xs)
        case '>' => ShiftLeft :: parse(xs)
        case '<' => ShiftRight :: parse(xs)
        case '.' => Output :: parse(xs)
        case ',' => Input :: parse(xs)
        case '[' => {
          val (inner, rest) = findMatching(1, xs, Nil)
          Loop(parse(inner)) :: parse(rest)
        }
      }
    }
  }

  def findMatching(depth: Int, source: List[Char], acc: List[Char]): (List[Char], List[Char]) = source match {
    case Nil => (acc.reverse, Nil)
    case '['::xs => findMatching(depth+1, xs, '['::acc)
    case ']'::xs =>
      if (depth == 1) (acc.reverse, xs)
      else            findMatching(depth-1, xs, ']'::acc)
    case x::xs => findMatching(depth, xs, x::acc)
  }

  def interpret(source: String, input: String): String = {
    val prog = parse(source.toList)
    val (_, _, out) = interpret(prog, input.toList, Nil, BFTape(0, None, None))
    out.mkString
  }

  def interpret(source: List[BFCommand], input: List[Char], acc: List[Char], tape: BFTape): (BFTape, List[Char], List[Char]) = source match {
    case Nil => (tape, input, acc.reverse)
    case c::cs => c match {
      case Add => interpret(cs, input, acc, tape.add)
      case Subtract => interpret(cs, input, acc, tape.subtract)
      case ShiftLeft => interpret(cs, input, acc, tape.shiftLeft)
      case ShiftRight => interpret(cs, input, acc, tape.shiftRight)

      case Input => input match {
        case Nil => (tape, input, "Error: Not enough input".toList)
        case x::xs => interpret(cs, xs, acc, tape.set(x.toInt))
      }

      case Output => interpret(cs, input, tape.value.toChar :: acc, tape)

      case Loop(xs) => {
        if (tape.value == 0) {
          interpret(cs, input, acc, tape)
        } else {
          val (tape2, input2, acc2) = interpret(xs, input, acc, tape)
          interpret(source, input2, acc2, tape2)
        }
      }
    }
  }
}
