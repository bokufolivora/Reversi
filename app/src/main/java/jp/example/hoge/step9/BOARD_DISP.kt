package jp.example.hoge.step9

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.view.MotionEvent
import android.view.View


class BOARD_DISP(context : Context, val boardStatus : GAME_STATUS, val masuSelect : SELECT_MASU )  : View(context) {

    private val paint = Paint()
    private val res: Resources = this.getContext().getResources()     // 画像準備
    // 画像のサイズは 
    //  マス=48×48 
    private val IMG_NORMAL: Bitmap = BitmapFactory.decodeResource(res, R.drawable.normal)
    private val IMG_READY: Bitmap = BitmapFactory.decodeResource(res, R.drawable.ready)
    private val IMG_BLACK: Bitmap = BitmapFactory.decodeResource(res, R.drawable.black)
    private val IMG_WHITE: Bitmap = BitmapFactory.decodeResource(res, R.drawable.white)
    // てボードは 48*8=384なので 384×384
    private val IMG_BOARD: Bitmap = BitmapFactory.decodeResource(res, R.drawable.board)
    //
    private val masuSize: Float = (IMG_BOARD.width / 8).toFloat()     // 1マスのサイズ
    private val fukantai: Float = masuSize / 10.0F                    // 反応しない幅
    // 補正用offset
    private var boardLeft: Float = 0.0F
    private var boardTop: Float = 0.0F

    // 盤の表示
    // BOARD_DISP.invalidate()でonDraw()実行されます
    override fun onDraw(c: Canvas) {
        // 盤を中央に表示
        boardLeft = ((c.getWidth() - IMG_BOARD.width) / 2).toFloat()
        c.drawBitmap(IMG_BOARD, boardLeft, boardTop, paint)

        // マス
        for (x: Int in 0..7) {
            for (y: Int in 0..7) {
                val img: Bitmap = when (boardStatus.banStatus[x][y]) {
                    GAME_STATUS.MASU_STATUS.BLACK -> IMG_BLACK
                    GAME_STATUS.MASU_STATUS.WHITE -> IMG_WHITE
                    GAME_STATUS.MASU_STATUS.READY -> IMG_READY
                    else -> IMG_NORMAL
                }
                // ステータスに応じて画像を表示
                val masuLeft = (x * masuSize + boardLeft).toFloat()
                val masuTop = (y * masuSize + boardTop).toFloat()
                c.drawBitmap(img, masuLeft, masuTop, paint)
            }
        }
    }

    // ボード上でのタッチ検出
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.getAction()) {
            MotionEvent.ACTION_DOWN -> {
                // タッチされた、マスの座標を求め
                val x = ((event.getX() - boardLeft) / masuSize).toInt()
                masuSelect.x = if (x < 9) {
                    val xmin = boardLeft + (masuSize * x) + fukantai
                    val xmax = boardLeft + (masuSize * (x + 1)) - fukantai
                    if (event.getX() > xmin && event.getX() < xmax) { x  } else { -1 }
                } else { -1 }
                val y = (event.getY() / masuSize).toInt()
                masuSelect.y = if (y < 9) {
                    val ymin = boardTop + (masuSize * y) + fukantai
                    val ymax = boardTop + (masuSize * (y + 1)) - fukantai
                    if (event.getY() > ymin && event.getY() < ymax) { y } else { -1 }
                } else { -1 }

                // 盤上をタッチされたかを判定
                masuSelect.InSw = if (masuSelect.x != -1 && masuSelect.y != -1) {
                    // 有効な座標
                    SELECT_MASU.IN_STATUS.MASU_SELECT
                } else {
                    SELECT_MASU.IN_STATUS.NON
                }
                // 上位(reversiGame)でタッチを検出してもらうため
                // 有効時 false で返している
                if (SELECT_MASU.IN_STATUS.MASU_SELECT == masuSelect.InSw) return false
            }
        }
        return true
    }

}