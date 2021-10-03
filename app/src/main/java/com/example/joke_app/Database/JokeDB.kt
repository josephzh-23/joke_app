package com.example.joke_app.Database

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.joke_app.App
import com.example.joke_app.Joke


object JokeDB :
	SQLiteOpenHelper(App.instance, "jokes-database.db", null, 1) {


	 val TAG = "JokeDB class "

	private const val TABLE_NAME = "joke"

	private const val ID_COL = "id" // Column I
		private const val JOKE_COL = "joke" //Column II
		private const val STATUS_COL = "Password" // Column III


	val db = this.writableDatabase



	override fun onCreate(db: SQLiteDatabase) {
		var create_table ="create Table " + TABLE_NAME +
		" (${ID_COL} text, ${JOKE_COL} text, ${STATUS_COL} text);"

		db.execSQL(create_table)
	}



	/*
	For each upgrade, called when this needs to be done
	 */
	override fun onUpgrade(MyDB: SQLiteDatabase, i: Int, i1: Int) {
		MyDB.execSQL("drop Table if exists $TABLE_NAME")


	}


	fun saveJoke(joke: Joke): Int {
		var result = -1
		val contentValues = ContentValues()

		// We have to make sure we are inserting into the right columns
		contentValues.put(ID_COL, joke.id)
		contentValues.put(JOKE_COL, joke.joke)
		contentValues.put(STATUS_COL, joke.status)


		try {
		 result= db.insert(TABLE_NAME, null, contentValues).toInt()
		} catch (e: Exception) {
			Log.i(TAG, "saveJoke: ${e.printStackTrace()}")
		}
		return result
	}


	// Delete by the id from when user selects it
	fun deleteJoke(id: String): Int {
		val whereArgs = arrayOf(id)
		var res =db.delete(TABLE_NAME, ID_COL.toString() + " = ?", whereArgs)
		return res
	}


	fun fetch_fav_jokes():ArrayList<Joke>{
		val list = ArrayList<Joke>()
		val db = this.readableDatabase

		val columns = arrayOf<String>(ID_COL, JOKE_COL, STATUS_COL)
		try {
			val cursor =db.query(TABLE_NAME, columns, null, null, null, null,null)

			if (cursor.count > 0) {
				cursor.moveToFirst()
				while (!cursor.isAfterLast()) {


					val id = cursor.getString(0)
					val joke = cursor.getString(1)
					val status = cursor.getString(2)


					val recipe = Joke(
						id, joke, status
					)

					list.add(recipe)
					cursor.moveToNext()
				}
			}
			cursor.close()
		}catch (e:Exception){

			e.printStackTrace()
		}
		return list

	}


}