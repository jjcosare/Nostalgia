package controllers

import engine.board.{Black, Board, Side, White}
import engine.movegen.Move.LocationMove
import validators.MoveValidator
import views.boards.{BoardView, DefaultBoardView}

/**
  * Created by melvic on 9/11/18.
  */
trait BoardController {
  def sideToMove: Side
  def boardAccessor: BoardAccessor
  def moveValidator: MoveValidator
  def boardView: BoardView

  def newGame(lowerSide: Side): Unit
  def move(move: LocationMove): Unit
  def rotate(): Unit
}

case class DefaultBoardController(initialBoard: Board, moveValidator: MoveValidator) extends BoardController {
  private var _sideToMove: Side = White
  private var _boardAccessor: BoardAccessor = SimpleBoardAccessor(initialBoard)

  override def sideToMove = _sideToMove
  override def boardAccessor = _boardAccessor

  def sideToMove_=(sideToMove: Side): Unit = _sideToMove = sideToMove
  def boardAccessor_=(boardAccessor: BoardAccessor): Unit = _boardAccessor = boardAccessor

  override lazy val boardView = DefaultBoardView(this)

  override
  def rotate(): Unit = {
    boardAccessor = boardAccessor match {
      case SimpleBoardAccessor(b) => RotatedBoardAccessor(b)
      case RotatedBoardAccessor(b) => SimpleBoardAccessor(b)
    }
    boardView.resetBoard()
  }

  override
  def newGame(lowerSide: Side): Unit = {
    sideToMove = lowerSide
    boardAccessor = boardAccessor.updatedBoard(_ => initialBoard)
    lowerSide match {
      case Black => rotate()
      case _ => boardView.resetBoard()
    }
  }

  override
  def move(move: LocationMove): Unit = boardAccessor.board(move.source).foreach { piece =>
    boardAccessor = boardAccessor.moveBoard(move, piece)
    boardView.resetBoard()
    boardView.highlight(move.destination)
    sideToMove = sideToMove.opposite
  }
}