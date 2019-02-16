package engine.search

import engine.board.{Board, Piece, Side}
import engine.eval.Evaluator
import engine.movegen.Move.LocationMove

import scala.annotation.tailrec

/**
  * Created by melvic on 1/27/19.
  */
sealed trait AlphaBeta {
  def evaluateBoard: (Board, Side) => Double

  def cutOffBound(score: Double, bound: Double): Boolean

  def isBetterScore(score: Double, bestScore: Double): Boolean

  def opponent: AlphaBeta

  /**
    * Recursively evaluates a given board using the Alpha-Beta Pruning algorithm.
    * @param board board to evaluate
    * @param depth remaining depth in the search tree
    * @return A pair consisting of the evaluation score and the chosen next move.
    */
  def move(board: Board, side: Side, currentScore: Double, bound: Double, depth: Int): (Double, Board) =
    if (depth == 0) (evaluateBoard(board, side), board)
    else {
      @tailrec
      def recurse(bestScore: Double,
          nextBoard: Board, updatedBoards: Stream[Board]): (Double, Board) = updatedBoards match {
        case Stream() => (bestScore, nextBoard)
        case updatedBoard +: nextMoves =>
          val (score, _) = opponent.move(updatedBoard, side.opposite,
            bound, bestScore,   // params positions switched
            depth - 1)

          if (cutOffBound(score, bound)) (bound, updatedBoard)
          else if (isBetterScore(score, bestScore)) recurse(score, updatedBoard, nextMoves)
          else recurse(bestScore, nextBoard, nextMoves)
      }

      recurse(currentScore, board, board.generateMoves(side).map { case (move, piece) =>
        board.updateByMove(move, piece)
      })
    }
}

object AlphaBetaMax extends AlphaBeta {
  override def cutOffBound(score: Double, bound: Double) = score >= bound

  override def evaluateBoard = Evaluator.evaluate

  override def isBetterScore(score: Double, bestScore: Double) = score > bestScore

  override def opponent = AlphaBetaMin
}

object AlphaBetaMin extends AlphaBeta {
  override def evaluateBoard = (board, side) => -Evaluator.evaluate(board, side)

  override def cutOffBound(score: Double, bound: Double) = score <= bound

  override def isBetterScore(score: Double, bestScore: Double) = score < bestScore

  override def opponent = AlphaBetaMax
}
