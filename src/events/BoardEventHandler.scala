package events

import javafx.event.EventHandler
import javafx.scene.input.MouseEvent

import engine.board.Piece
import engine.movegen.{Location, Move}
import views.boards.BoardView

/**
  * Created by melvic on 9/16/18.
  */
trait BoardEventHandler extends EventHandler[MouseEvent] {
  override def handle(event: MouseEvent): Unit = {
    val col = (event.getX / boardView.squareSize).toInt
    val row = (event.getY / boardView.squareSize).toInt

    val selectedLocation = Location.locate(row, col)
    val selectedPiece = boardView.boardController.boardAccessor(selectedLocation)

    performAction(selectedPiece, selectedLocation)
  }

  def boardView: BoardView
  def performAction(selectedPiece: Option[Piece], selectedLocation: Location)

  private var _sourcePiece: Option[Piece] = None
  def sourcePiece = _sourcePiece
  def sourcePiece_=(sourcePiece: Option[Piece]) = _sourcePiece = sourcePiece
}

case class PieceHoverEventHandler(boardView: BoardView) extends BoardEventHandler {
  override def performAction(selectedPiece: Option[Piece], selectedLocation: Location): Unit = {
    boardView.toggleHover(sourcePiece.isDefined || selectedPiece.exists { case Piece(_, side) =>
      side == boardView.boardController.sideToMove
    })
  }
}

case class MoveEventHandler(boardView: BoardView, hoverEventHandler: PieceHoverEventHandler) extends BoardEventHandler {
  private var _sourceLocation: Option[Location] = None

  def sourceLocation = _sourceLocation.get
  def sourceLocation_=(sourceLocation: Location) = _sourceLocation = Some(sourceLocation)

  override def performAction(selectedPiece: Option[Piece], selectedLocation: Location) = {
    (sourcePiece, selectedPiece) match {
      case (None, None) => ()
      case (None, Some(Piece(_, side))) =>
        if (side == boardView.boardController.sideToMove) replaceSourceSquare()
        else ()

      // If the execution has made it this far, we can assume the source piece is present.
      case (_, _) if sourcePiece eq selectedPiece => undoSelection()
      case (Some(Piece(_, sourceSide)), Some(Piece(_, selectedSide))) if sourceSide == selectedSide =>
        replaceSourceSquare()
      case _ =>
        if (boardView.boardController.move(Move[Location](sourceLocation, selectedLocation)))
          resetSourcePiece()
    }

    def replaceSourceSquare(): Unit = {
      this.sourcePiece = selectedPiece
      this.sourceLocation = selectedLocation
      hoverEventHandler.sourcePiece = sourcePiece
      boardView.highlight(selectedLocation)
    }

    def resetSourcePiece(): Unit = {
      sourcePiece = None
      hoverEventHandler.sourcePiece = None
    }

    def undoSelection(): Unit = {
      boardView.resetBoard()
      resetSourcePiece()
    }
  }
}