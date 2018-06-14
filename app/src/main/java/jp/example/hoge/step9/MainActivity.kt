package jp.example.hoge.step9

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class MainActivity : AppCompatActivity() {
    companion object Factory {
        val EXTRA_TEXT = "com.fogu.fuga.SEL_MODE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun gameStart1(v : View) {
        val intent = Intent(this, reversiGame::class.java)
        intent.putExtra(EXTRA_TEXT, GAME_STATUS.GAME_MODE.MAN_TO_MAN.intVal)
        startActivity(intent)
    }
    fun gameStart2(v : View) {
        val intent = Intent(this, reversiGame::class.java)
        intent.putExtra(EXTRA_TEXT, GAME_STATUS.GAME_MODE.MAN_TO_AUTO.intVal );
        startActivity(intent)
    }
    fun gameStart3(v : View) {
        val intent = Intent(this, reversiGame::class.java)
        intent.putExtra(EXTRA_TEXT, GAME_STATUS.GAME_MODE.AUTO_TO_AUTO.intVal );
        startActivity(intent)
    }
}
