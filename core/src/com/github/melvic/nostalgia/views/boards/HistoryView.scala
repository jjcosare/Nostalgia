package com.github.melvic.nostalgia.views.boards

import com.github.melvic.nostalgia.engine.board.{Board, Notation, Piece, White}
import com.github.melvic.nostalgia.engine.movegen.Move.LocationMove
import javafx.geometry.Orientation
import javafx.scene.control.{ListCell, ListView, Separator}
import javafx.scene.layout.{BorderPane, VBox}
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.{Node, control}
import scalafx.scene.control.Label
import scalafx.scene.layout.{HBox, VBox => SVBox}
import scalafx.scene.paint.Color
import scalafx.scene.text.{Font, TextAlignment}

/**
  * Created by melvic on 1/23/19.
  */
class HistoryView extends VBox {
  type HistoryMove = (Int, String)

  val themeColor = "white"

  class HistoryListView extends ListView[HistoryMove] {
    setFocusTraversable(false)
    getStyleClass.add("history")
    setPadding(Insets(0, 0, 0, 0))

    setCellFactory(_ => new ListCell[HistoryMove] {
      override def updateItem(item: HistoryMove, empty: Boolean): Unit = {
        super.updateItem(item, empty)
        setStyle(s"-fx-background-color: $themeColor")
        setText(null)
        setGraphic(null)

        if (!empty) {
          val (number, notation) = item
          val content: Node = new HBox {
            padding = Insets(4)
            spacing = 3
            children = Vector(
              new Label {
                text = "%3s".format(s"$number.")
                textFill = Color.Gray
              },
              new Label(notation)
            )
          }
          setGraphic(content)
        }
      }
    })
  }

  val listView = new HistoryListView

  val titlePane = new SVBox {
    children = Vector(new Label {
      text = "History"
      style = "-fx-font-size: 16; -fx-font-weight: bold"
    })
    padding = Insets(10, 10, 2, 10)
  }

  getChildren.addAll(titlePane, new Separator(Orientation.HORIZONTAL), listView)

  def addMove(move: LocationMove, board: Board, piece: Piece): Boolean = {
    val moveNotation = Notation.ofMove(move, piece, board)

    // A white move starts on a new row.
    if (piece.side == White) {
      val moveNumber = listView.getItems.size + 1
      listView.getItems.add((moveNumber, moveNotation))
    } else {
      val lastIndex = listView.getItems.size - 1
      val (lastMoveNumber, lastMoveNotation) = listView.getItems.get(lastIndex)
      listView.getItems.set(lastIndex, (lastMoveNumber, s"$lastMoveNotation $moveNotation"))
      true
    }
  }

  def items = listView.getItems

  def size = items.size

  def lastItem: Option[HistoryMove] =
    if (items.isEmpty) None
    else Some(items.get(items.size - 1))
}