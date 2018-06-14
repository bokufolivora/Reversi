package jp.example.hoge.step9

import android.util.Log

class GAME_STATUS {
    // 対戦モード (PLAYER2)
    enum class GAME_MODE(val intVal : Int ) {
        MAN_TO_MAN(0),
        MAN_TO_AUTO(1),
        AUTO_TO_AUTO(2)
    }
    var gameMode : GAME_MODE = GAME_MODE.MAN_TO_MAN

    enum class GAME_TURN(val intVal : Int ) {
        TURN_1(0),
        TURN_2(1)
    }
    var playTurn : GAME_TURN = GAME_TURN.TURN_1

    // フェーズ
    enum class GAME_PHASE(val intVal : Int ) {
        INITIAL(0),
        RESET(1),
        PLAYER(2),
        AUTO(3),
        WINNER_FIX(4),
        GAME_OVER(5),
    }
    var playPhase : GAME_PHASE = GAME_PHASE.INITIAL

    // マスのステータス
    enum class  MASU_STATUS( val indxVal : Int) {
        NORMAL(0),         // 何でもなし
        READY(1),          // 置いていい
        BLACK(2),          // 黒
        WHITE(3),          // 白
        OUT_RANGE(4),
    }
    // マス８×８　NORMAL で 埋めている
    var banStatus  =  Array(8, {Array(8, {MASU_STATUS.NORMAL })})
    // 駒の数
    var playerKomaCount = arrayOf<Int>( 0 , 0 )

    // 勝者
    enum class WINNER {
        NON,
        PLAYER1,
        PLAYER2,
    }
    var  gemeWinner : WINNER = WINNER.NON

    // 方向
    private enum class VECTOR(val intVal : Int ) {
        UP(0),
        RIGHT_UP(1),
        RIGHT(2),
        RIGHT_DOWN(3),
        DOWN(4),
        LEFT_DOWN(5),
        LEFT(6),
        LEFT_UP(7),
    }
    // 方向による 座標の補正データ x+VX[VECTOR.UP]*3 で [xの上方向、距離３]になる
    private val VX = arrayOf<Int>( 0,  1, 1, 1, 0, -1, -1, -1 )
    private val VY = arrayOf<Int>(-1, -1, 0, 1, 1,  1,  0, -1 )

    // プレイヤーと駒の関係
    private  enum class SITUATION( val intVal : Int ) {
        ALLY(0),
        ENEMY(1),
    }
    private  val player1Koma : MASU_STATUS = MASU_STATUS.WHITE
    private  val player2Koma : MASU_STATUS = MASU_STATUS.BLACK
    private fun getKomaStatus( newTurn : GAME_TURN, getMode : SITUATION ) : MASU_STATUS {
        return if ( newTurn == GAME_TURN.TURN_1) {
            if ( getMode == SITUATION.ALLY) { player1Koma } else { player2Koma }
        } else {
            if ( getMode == SITUATION.ALLY) { player2Koma } else { player1Koma }
        }
    }

    private val auto1 : AUTO1 = AUTO1( banStatus )
    private val auto2 : AUTO1 = AUTO1( banStatus )

    // コマ数　数える
    fun getKomaCount( ) {
        playerKomaCount[0] = 0
        playerKomaCount[1] = 0
        for( x:Int in 0..7) {
            for(y:Int in 0..7) {
                when(banStatus[x][y]) {
                    MASU_STATUS.WHITE -> playerKomaCount[GAME_TURN.TURN_1.intVal]++
                    MASU_STATUS.BLACK -> playerKomaCount[GAME_TURN.TURN_2.intVal]++
                    else -> {}
                }
            }
        }
    }

    // ゲームのリセット
    fun gameReset(  ) {
        for (x: Int in 0..7) {
            for (y: Int in 0..7) {
                banStatus[x][y] = MASU_STATUS.NORMAL
            }
        }
        banStatus[3][3] = player1Koma
        banStatus[3][4] = player2Koma
        banStatus[4][3] = player2Koma
        banStatus[4][4] = player1Koma
        playerKomaCount[0] = 2
        playerKomaCount[1] = 2
    }

    // 入力データに応じた処理
    fun actionSelect( selMasu : SELECT_MASU ) {
        when( selMasu.InSw ) {
            SELECT_MASU.IN_STATUS.SURRENDER -> {
                // 降参した場合は相手の勝ち
                gemeWinner = if (playTurn == GAME_TURN.TURN_2) {
                    WINNER.PLAYER1
                } else {
                    WINNER.PLAYER2
                }
                playPhase = GAME_PHASE.WINNER_FIX
            }
            SELECT_MASU.IN_STATUS.PASS -> {
                // パス 回数に今は上限はなし
                playPhase = nextPhase()
            }
            SELECT_MASU.IN_STATUS.MASU_SELECT -> {
                Log.d("x", selMasu.x.toString())
                Log.d("y", selMasu.y.toString())

//                Log.d("st", banStatus[selMasu.x][selMasu.y].toString())
                // マスを選択時
                if (MASU_STATUS.READY == banStatus[selMasu.x][selMasu.y]) {
                    banStatus[selMasu.x][selMasu.y] = getKomaStatus(playTurn,SITUATION.ALLY)
                    ReversKoma(selMasu)
                    playPhase = nextPhase()
                }
            }
            else -> { }
        }
        Log.d("Next", playPhase.toString())
    }

    // 置けるマスの確認
    fun checkReayMasu() {
        masuRefresh()
        // 総当りだけど・・
        for( x:Int in 0..7) {
            for(y:Int in 0..7) {
                if (banStatus[x][y] == MASU_STATUS.NORMAL ) {
                    val xy : SELECT_MASU = SELECT_MASU(x,y,SELECT_MASU.IN_STATUS.NON)

                    for(v:Int in VECTOR.UP.intVal..VECTOR.LEFT_UP.intVal ) {
                        if ( true == checkRevers(xy, playTurn, v)) {
                            banStatus[x][y] = MASU_STATUS.READY
                            break
                        }
                    }
                }
            }
        }
    }
    // 前のREADYをクリア
    fun masuRefresh() {
        for(x:Int in 0..7) {
            for(y:Int in 0..7) {
                if (banStatus[x][y] ==MASU_STATUS.READY) {
                    banStatus[x][y] = MASU_STATUS.NORMAL
                }
            }
        }
    }

    //　マシンの行動選択
    fun autoActionSelect() : SELECT_MASU {
        val r = if ( playTurn==GAME_TURN.TURN_1) {
            auto1.actSelect()
        } else {
            auto2.actSelect()
        }
        return r
    }

    // 反転
    private fun ReversKoma(xy:SELECT_MASU) {
        for(v:Int in VECTOR.UP.intVal..VECTOR.LEFT_UP.intVal ) {
            if ( true == checkRevers(xy, playTurn, v)) {
                for ( i:Int in 1..7 ) {
                    val x = xy.x + (VX[v]*i)
                    val y = xy.y + (VY[v]*i)
                    if (x>0 && x<8 && y>0 && y<8) {
                        if (banStatus[x][y]==getKomaStatus(playTurn,SITUATION.ENEMY)) {
                            banStatus[x][y] =getKomaStatus(playTurn,SITUATION.ALLY)
                        } else {
                            break
                        }
                    } else {
                        break
                    }
                }
            }
        }
    }


    // フェーズを進める
    private fun nextPhase() : GAME_PHASE {
        masuRefresh()   // READYをクリア

        // 次のターン
        playTurn =  if (playTurn==GAME_TURN.TURN_1) { GAME_TURN.TURN_2} else { GAME_TURN.TURN_1 }

        return  if ( checkEndGame() == false ) {
            // 両プレイヤー置けなくなったので、ゲーム終了
            GAME_PHASE.WINNER_FIX

        } else {
            when( gameMode ) {
                GAME_MODE.MAN_TO_AUTO->{
                    if (playTurn==GAME_TURN.TURN_1 ) {
                        GAME_PHASE.AUTO
                    } else {
                        GAME_PHASE.PLAYER
                    }
                }
                GAME_MODE.MAN_TO_MAN-> { GAME_PHASE.PLAYER }
                else-> { GAME_PHASE.AUTO }
            }
        }
    }

    // 基準座標の指定方向に、反転可能な駒があるか？
    private fun checkRevers(xy:SELECT_MASU, turn:GAME_TURN, v:Int) : Boolean {
        var cnt:Int = 0
        for ( i:Int in 1..7 ) {
            // 連続して「敵駒」を探す
            if (getKomaStatus(turn,SITUATION.ENEMY)!=( getKoma(xy,v,i))) {
                cnt = i
                break
            }
        }
        return if ( cnt > 1 ) {
            // 間が「敵駒」で、端が「自駒」
            if (getKomaStatus(turn,SITUATION.ALLY)==getKoma(xy,v,cnt)) { true } else { false }
        } else { false }
    }

    // 基準座標、方向、距離を指定し、そのマスの駒を得る
    private fun getKoma( xy:SELECT_MASU, v:Int, distance:Int ) : MASU_STATUS {
        val x = xy.x + (VX[v]*distance)
        val y = xy.y + (VY[v]*distance)
        return if (x>0 && x<8 && y>0 && y<8) {
            banStatus[x][y]
        } else {
            MASU_STATUS.OUT_RANGE
        }
    }

    // 両プレイヤーに置ける場所がない
    private fun checkEndGame() : Boolean {
        for( x:Int in 0..7) {
            for(y:Int in 0..7) {
                if (banStatus[x][y] == MASU_STATUS.NORMAL ) {
                    val xy : SELECT_MASU = SELECT_MASU(x,y,SELECT_MASU.IN_STATUS.NON)
                    for(v:Int in VECTOR.UP.intVal..VECTOR.LEFT_UP.intVal ) {
                        if ( true == checkRevers(xy, playTurn, v)) return true
                    }
                }
            }
        }
        val nextPlayer =  if (playTurn==GAME_TURN.TURN_1) { GAME_TURN.TURN_2} else { GAME_TURN.TURN_1 }
        for( x:Int in 0..7) {
            for(y:Int in 0..7) {
                if (banStatus[x][y] == MASU_STATUS.NORMAL ) {
                    val xy : SELECT_MASU = SELECT_MASU(x,y,SELECT_MASU.IN_STATUS.NON)
                    for(v:Int in VECTOR.UP.intVal..VECTOR.LEFT_UP.intVal ) {
                        if ( true == checkRevers(xy, nextPlayer, v)) return true
                    }
                }
            }
        }
        return false
    }
}
