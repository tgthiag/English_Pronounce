package com.tgapps.englishpronounce.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

var TABLE_NAME = "pronounce_app"
var ID = "id"
var NOME = "nome"
var FRASE = "frase"
var COINS = "moedas"
var FIRST_ACESS = "first"
var TIME = "time"
var OPT1 = "opt1"
var OPT2 = "opt2"
var OPT3 = "opt3"

class LocalDatabase(ctx: Context) : SQLiteOpenHelper(ctx, TABLE_NAME, null,1) {
    override fun onCreate(db: SQLiteDatabase?) {
        var CREATE_TABLE = """CREATE TABLE $TABLE_NAME($ID INT PRIMARY KEY, $NOME VARCHAR, $FRASE INT, $COINS,$FIRST_ACESS INT, $TIME VARCHAR, $OPT1 VARCHAR, $OPT2 VARCHAR, $OPT3 VARCHAR)"""
        db?.execSQL(CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val DROP_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME;"
        db?.execSQL(DROP_TABLE)
        onCreate(db)
    }

    fun initializeRow() {
        val db = this.writableDatabase
        var cv = ContentValues()
        cv.put(ID, 1)
        cv.put(NOME, "")
        cv.put(COINS, 4)
        cv.put(FRASE, 0)
        cv.put(FIRST_ACESS, 0)
        cv.put(TIME, "")
        cv.put(OPT1, "")
        cv.put(OPT2, "")
        cv.put(OPT3, "")

        db.insert(TABLE_NAME, null, cv)
    }
}