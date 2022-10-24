import cats.Foldable
import cats.data.Chain
import cats.effect._
import cats.syntax.all._
import fs2.{Chunk, Pipe, Stream}

import scala.concurrent.duration._

object Aboba extends IOApp.Simple {

  case class Result(key: String, value: Int)

  val in: Stream[IO, Result] =
//    Stream
//      .eval(IO.println("here"))
//      .repeat
//      .zipRight(
    Stream(Result("a", 1), Result("b", 2), Result("c", 3), Result("a", 1), Result("a", 1)).repeat
      .zipLeft(Stream.awakeEvery[IO](1.second))
//      .zipLeft(Stream(1).repeat)

  def average[F[_]: Foldable](c: F[Result]): Double = {
    val (size, sum) = c.foldLeft((0, 0)) { case ((size, sum), next) =>
      (size + 1, sum + next.value)
    }
    sum.toDouble / size
  }

  val pipe: Pipe[IO, Result, Double] = {
    type State = Map[String, Chain[Result]]

    @annotation.tailrec
    def loop(
        remaining: Chunk[Result],
        state: State,
        results: Chunk[Double] = Chunk.empty[Double],
    ): (State, Chunk[Double]) = {
      val maybeHead = remaining.head
      if (maybeHead.isEmpty)
        (state, results)
      else {
        val head = maybeHead.get
        val tail = remaining.drop(1)
        val updatedKey = head.key
        val updatedState = state.updatedWith(updatedKey) {
          case Some(results) => Some(results.append(head))
          case None          => Some(Chain.one(head))
        }
        val updatedValue = updatedState(updatedKey)

        if (updatedValue.size == 3) {
          loop(tail, updatedState.removed(updatedKey), results ++ Chunk(average(updatedValue)))
        } else
          loop(tail, updatedState, results)
      }
    }

    in =>
      in.scanChunks(Map.empty[String, Chain[Result]]) { case (state, next) =>
        loop(next, state)
      }
  }

//  val out: Stream[IO, Unit] = in.through(pipe).evalMap(IO.println)

  override def run: IO[Unit] =
    in.through(pipe).evalMap(IO.println).compile.drain
}
