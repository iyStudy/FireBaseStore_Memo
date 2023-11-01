package com.example.firebasememo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.firebasememo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    // ActivityMainBindingは、このアクティビティで使用するレイアウトのビューバインディングです。
    // ビューバインディングは、レイアウトXMLのビューに直接アクセスできるようにする機能です。
    private lateinit var binding: ActivityMainBinding

    /**
     * onCreateは、アクティビティが生成されるときに最初に呼び出されるメソッドです。
     * ここで、画面の初期設定やリソースの読み込みなどの初期化処理を行います。
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        // 既存のアクティビティのonCreateメソッドを呼び出すことで、
        // 基本的な初期化処理を行います。
        super.onCreate(savedInstanceState)

        // ビューバインディングのインスタンスを生成し、
        // これを使ってレイアウトリソースからビューを取得します。
        binding = ActivityMainBinding.inflate(layoutInflater)

        // アクティビティのコンテンツビューを、ビューバインディングのルートビューに設定します。
        setContentView(binding.root)

////        // アプリのツールバーを設定します。
//        val toolbar: Toolbar = binding.appToolbar
//        setSupportActionBar(toolbar)
    }
}
