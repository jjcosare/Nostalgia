package controllers

import engine.board.{Board, Piece}
import engine.movegen.{Location, Move}
import engine.movegen.Location._

/**
  * Created by melvic on 9/14/18.
  */
trait BoardAccessor {
  def apply(location: Location): Option[Piece] = board.at(accessorLocation(location))
  def apply(row: Int, col: Int): Option[Piece] = board.at(accessorLocation(locate(row, col)))
  def board: Board

  def accessorLocation: Location => Location = identity
  def accessorMove: Move[Location] => Move[Location] = Move.transform(accessorLocation)

  def moveBoard(move: Move[Location]): BoardAccessor = {
    val netMove = accessorMove(move)
    board(netMove.source).map { piece =>
      updatedBoard(_.updateByMove(netMove, piece))
    } getOrElse this
  }

  def updatedBoard(f: Board => Board): BoardAccessor = {
    val accessorType = this match {
      case SimpleBoardAccessor(_) => SimpleBoardAccessor
      case RotatedBoardAccessor(_) => RotatedBoardAccessor
    }
    accessorType(f(board))
  }
}

case class SimpleBoardAccessor(board: Board) extends BoardAccessor

case class RotatedBoardAccessor(board: Board) extends BoardAccessor {
  override def accessorLocation = location =>
    Location(Board.Size - 1 - location.file, Board.Size -1 - location.rank)
}

