package com.example.joke_app

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.joke_app.Database.JokeDB
import com.facebook.internal.Mutable

class View_Model(): ViewModel() {


    var jokeList: MutableLiveData<ArrayList<Joke>> = MutableLiveData(ArrayList<Joke>())


    var isJokeFetched: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)

    var isUserLoggedin: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)

    fun addJoke(joke: Joke):Int {

        var res = JokeDB.saveJoke(joke)

        // Only add joke to the list if saved successfully
        if (res> 0) {
          jokeList.value!!.add(joke)
            jokeList.postValue(jokeList.value)
        }

        return res
    }


    fun toggle_fetch_status(bool:Boolean){
        isJokeFetched.value = bool
    }

    fun load_jokes_from_db(){
        Log.i(ContentValues.TAG, "call: the string used here is ${Thread.currentThread().name}")
        jokeList.postValue(JokeDB.fetch_fav_jokes())

    }


    // Delete joke from the livedata list as well as database
    fun deleteJoke(joke:Joke):Int{

        var result = -1
        jokeList.value!!.remove(joke)
        jokeList.postValue( jokeList.value)
        if(joke.id!=null) {
            result = JokeDB.deleteJoke(joke.id)
        }
        return result

    }

    fun login() {
        isUserLoggedin.value = true
    }

}