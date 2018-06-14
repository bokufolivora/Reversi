package jp.example.hoge.step9

// マスの情報データクラス
class SELECT_MASU( var x: Int=0 , var y:Int=0 , var InSw:IN_STATUS=IN_STATUS.NON ) {
    // 入力にも使用しているので、下記enumつけてみた
    enum class IN_STATUS(var IntVal :Int ) {
        NON(0),
        MASU_SELECT(1),
        SURRENDER(2),
        PASS(3),
    }
}