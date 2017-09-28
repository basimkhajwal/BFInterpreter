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

  def leftList: List[Int] = left match {
    case Some(l) => l.leftList ++ List(l.value)
    case _ => Nil
  }

  def rightList: List[Int] = right match {
    case Some(r) => r.value :: r.rightList
    case _ => Nil
  }

  def toList: List[Int] = leftList ++ List(value) ++ rightList
}

case class BFProgram(tape: BFTape, steps: List[BFCommand]) {
  val done = steps.isEmpty

  def needsInput = steps.head == Input
  def output = if (steps.head == Output) Some(tape.value.toChar) else None

  def step: BFProgram = steps.head match {
    case Add => BFProgram(tape.add, steps.tail)
    case Subtract => BFProgram(tape.subtract, steps.tail)
    case ShiftLeft => BFProgram(tape.shiftLeft, steps.tail)
    case ShiftRight => BFProgram(tape.shiftRight, steps.tail)
    case Loop(xs) if tape.value != 0 => BFProgram(tape, xs ++ steps)
    case _ => BFProgram(tape, steps.tail)
  }

  def stepInput(c: Char) = BFProgram(tape.set(c.toInt), steps.tail)
}

object BFInterpreter {

  val COMMANDS = "+-><[].,"

  def parse(source: List[Char]): List[BFCommand] = source match {
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
