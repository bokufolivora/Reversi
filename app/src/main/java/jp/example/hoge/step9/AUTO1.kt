package jp.example.hoge.step9

// マシン対戦時のマシンの思考
// あまり賢くありません
// ここをAIとかにすると、強くなると思います
class AUTO1( val ban : Array<Array<GAME_STATUS.MASU_STATUS>> )  {

    // マスの重みで判断する思考パターン
    private val weight = arrayOf<Array<Int>>(
            arrayOf<Int>(  30,-12,  0, -1, -1,  0,-12, 30 ),
            arrayOf<Int>( -12,-15, -3, -3, -3, -3,-15,-12 ),
            arrayOf<Int>(   0, -3,  0, -1, -1,  0, -3,  0 ),
            arrayOf<Int>(  -1, -3, -1, -1, -1, -1, -3, -1 ),
            arrayOf<Int>(  -1, -3, -1, -1, -1, -1, -3, -1 ),
            arrayOf<Int>(   0, -3,  0, -1, -1,  0, -3,  0 ),
            arrayOf<Int>( -12,-15, -3, -3, -3, -3,-15,-12 ),
            arrayOf<Int>(  30,-12,  0, -1, -1,  0,-12, 30 )
    )

    fun actSelect() : SELECT_MASU {
        var maxW = -100
        var slectAct : SELECT_MASU = SELECT_MASU(0,0,SELECT_MASU.IN_STATUS.PASS)
        for( x:Int in 0..7) {
            for(y:Int in 0..7) {
                if ( ban[x][y] == GAME_STATUS.MASU_STATUS.READY ) {
                    // 置けるマスの中で
                    if ( weight[x][y] > maxW ) {
                        // 最も「重み」のあるマスを取る
                        maxW = weight[x][y]
                        slectAct.x = x
                        slectAct.y = y
                        slectAct.InSw = SELECT_MASU.IN_STATUS.MASU_SELECT
                        println("->"+x.toString()+","+y.toString())
                    }
                }
            }
        }
        return slectAct
    }
}
