package jp.example.hoge.step9

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import jp.example.hoge.step9.MainActivity.Factory.EXTRA_TEXT
import kotlinx.android.synthetic.main.activity_reversi_game.*
import android.os.Handler
import android.util.Log
import kotlinx.android.synthetic.main.activity_reversi_game.view.*

class reversiGame : AppCompatActivity() {
    val handler = Handler()                         //
    val gameStaus : GAME_STATUS = GAME_STATUS()
    val boardSelect : SELECT_MASU = SELECT_MASU(0, 0, SELECT_MASU.IN_STATUS.NON)
    internal lateinit var boardBan :BOARD_DISP
    var delayCount : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reversi_game)

        // ボードを追加（表示）
        boardBan = BOARD_DISP(this, gameStaus, boardSelect)
        ban.addView(boardBan)

        // 対戦モードの設定をもらう
        gameStaus.gameMode = when(getIntent().getIntExtra(EXTRA_TEXT,GAME_STATUS.GAME_MODE.MAN_TO_MAN.intVal)) {
            GAME_STATUS.GAME_MODE.MAN_TO_AUTO.intVal->{
//                Log.d("GAME_STATUS","MAN-AUTO")
                GAME_STATUS.GAME_MODE.MAN_TO_AUTO
            }
            GAME_STATUS.GAME_MODE.AUTO_TO_AUTO.intVal->{
//                Log.d("GAME_STATUS","AUTO-AUTO")
                GAME_STATUS.GAME_MODE.AUTO_TO_AUTO
            }
            else-> {
//                Log.d("GAME_STATUS","MAN-MAN")
                GAME_STATUS.GAME_MODE.MAN_TO_MAN
            }
        }

        // ボタン押された時の処理
        button01.setOnClickListener { gameActionCheck( SELECT_MASU(0,0, SELECT_MASU.IN_STATUS.PASS)) }
        button02.setOnClickListener { gameActionCheck( SELECT_MASU(0,0, SELECT_MASU.IN_STATUS.SURRENDER)) }
        //
        gameActionCheck(boardSelect)
//        Log.d("Phase", playPhase.toString())

    }

    // 遅延処理用
    fun autoSelectDelay() {
        delayCount = 0
        handler.post(runnable)
    }
    val runnable = object : Runnable {
        override fun run() {
            delayCount++
            if ( 1 < delayCount ) {
                gameActionCheck(gameStaus.autoActionSelect())
            } else {
                handler.postDelayed(this, 1000)     // 1000ｍｓ後に自分にpost
            }
        }
    }

    // タッチされた時
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.getAction()) {
            MotionEvent.ACTION_DOWN -> {
                if (boardSelect.InSw == SELECT_MASU.IN_STATUS.MASU_SELECT) {
                    // 意図したように、ボード上のマスの座用が、ここで取得できなかったので
                    // BOARD_DISPでもonTouchEvent()を使い、マス座標を求め
                    // その後、ここで押された場合の処理 gameActionCheck()を呼ぶ
                    // 面倒なことをしている
                    // もっとまともな方法があると思います
                    gameActionCheck(boardSelect)
                    return true
                }
            }
        }
        return false
    }

    // 動作（マス、パス、降参）が選ばれた時の処理
    fun gameActionCheck( masuSelect : SELECT_MASU ) {
        // ゲームのフェーズによって、判断を変える
        when( gameStaus.playPhase) {
            GAME_STATUS.GAME_PHASE.INITIAL -> {
                // 最初の場合
                txtGameStatus.text = "Welcome reversi"
                gameStaus.playPhase = GAME_STATUS.GAME_PHASE.RESET
            }
            GAME_STATUS.GAME_PHASE.RESET -> {
                // ゲーム終了後
                when(masuSelect.InSw) {
                    SELECT_MASU.IN_STATUS.PASS-> {
                        // [New Game]ボタンを押された -> ゲーム開始
                        gameStaus.gameReset()
                        button01.text = "PASS"
                        button02.text = "降参"
                        gameStaus.playTurn = GAME_STATUS.GAME_TURN.TURN_1
                        gameStaus.playPhase =
                                if ( gameStaus.gameMode ==GAME_STATUS.GAME_MODE.AUTO_TO_AUTO )
                                    { GAME_STATUS.GAME_PHASE.AUTO }
                                    else { GAME_STATUS.GAME_PHASE.PLAYER }
                    }
                    SELECT_MASU.IN_STATUS.SURRENDER -> {
                        finish()
                    }
                }
            }
            GAME_STATUS.GAME_PHASE.PLAYER,GAME_STATUS.GAME_PHASE.AUTO -> {
                // プレイヤー処理選択時
                if (masuSelect.InSw != SELECT_MASU.IN_STATUS.NON ) {
                    // 有効な選択時、入力に応じ処理
                    gameStaus.actionSelect(masuSelect)

                    // 勝敗決定時
                    if (gameStaus.playPhase == GAME_STATUS.GAME_PHASE.WINNER_FIX) {
                        gameStaus.masuRefresh()
                        gameStaus.gemeWinner =
                                if (gameStaus.gemeWinner == GAME_STATUS.WINNER.NON) {
                                    if (gameStaus.playerKomaCount[0] > gameStaus.playerKomaCount[1]) {
                                        GAME_STATUS.WINNER.PLAYER1
                                    } else {
                                        GAME_STATUS.WINNER.PLAYER1
                                    }
                                } else {
                                    gameStaus.gemeWinner // そのまま
                                }
                        txtGameStatus.text = "WINNER : " + gameStaus.gemeWinner.toString()
                        gameStaus.playPhase = GAME_STATUS.GAME_PHASE.RESET
                    }
                }
            }
            else -> {
            }
        }
        ReDisply()
        masuSelect.InSw = SELECT_MASU.IN_STATUS.NON
    }

    // ゲーム盤 再表示
    fun ReDisply() {
        boardBan.invalidate()
        gameStaus.getKomaCount()    // 駒数を数える

        when (gameStaus.playPhase) {
            GAME_STATUS.GAME_PHASE.RESET -> {
                button01.text = "NEW GAME"
                button02.text = "RETURN"
                button01.isEnabled = true
                button02.isEnabled = true
            }
            GAME_STATUS.GAME_PHASE.AUTO -> {
                txtGameStatus.text = if (gameStaus.playTurn == GAME_STATUS.GAME_TURN.TURN_1) {
                    "AUTO-1"
                } else {
                    "AUTO-2"
                }
                button01.isEnabled = false
                button02.isEnabled = false
                gameStaus.checkReayMasu()
                autoSelectDelay()
            }
            GAME_STATUS.GAME_PHASE.PLAYER -> {
                txtGameStatus.text = if (gameStaus.playTurn == GAME_STATUS.GAME_TURN.TURN_1) {
                    if (gameStaus.gameMode == GAME_STATUS.GAME_MODE.MAN_TO_MAN) {
                        "YOU"
                    } else {
                        "PLAYER-1"
                    }
                } else {
                    "PLAYER-2"
                }
                button01.isEnabled = true
                button02.isEnabled = true
                gameStaus.checkReayMasu()
            }
            else -> {}
        }

        txtSubStatus.text = " 白 = " +  gameStaus.playerKomaCount[0].toString() + " , 黒 = "+ gameStaus.playerKomaCount[1].toString()
    }

}
