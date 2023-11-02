package com.example.firebasememo

import java.io.Serializable

/**
 * Memoクラスはメモを表すデータクラスです。
 * データクラスとは、データを保持するだけのクラスを簡単に定義できるKotlinの特性です。
 * 主に、このクラスはFirestoreから取得したデータを格納するために使用されます。
 */
data class Memo(
    var text: String = "",
    var priority: Double = 0.0,  // メモの優先度。数字が大きいほど優先度が高いとされる。初期値は0.0。
    val documentId: String? = null, // FirestoreのドキュメントIDを保持。新しいメモを作成する場合はnullになる可能性がある
) : Serializable

