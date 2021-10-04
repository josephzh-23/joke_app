package com.example.joke_app

import java.util.concurrent.Callable


/*
A class used to handle database CRUD operations and
return results to calling thread pool
 */

class DbTask(var lambdaWithParam: ( Joke?)->Int, var joke:Joke) : Callable<Int>{
    var result=0

    override fun call():Int {
        result = lambdaWithParam(joke)
        return result
    }
}