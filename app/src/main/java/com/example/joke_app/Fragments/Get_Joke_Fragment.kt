package com.example.joke_app.Fragments

import android.content.ContentValues
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.joke_app.databinding.FragmentGetJokeBinding
import com.facebook.login.widget.LoginButton
import com.smartherd.globofly.services.APIService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.android.material.snackbar.BaseTransientBottomBar

import com.google.android.material.snackbar.Snackbar

import com.facebook.login.LoginResult

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.joke_app.*
import com.example.joke_app.R
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.share.model.ShareLinkContent
import com.squareup.picasso.Picasso
import org.json.JSONException
import java.util.*
import com.facebook.AccessToken
import com.smartherd.globofly.services.ServiceBuilder


// TODO: Rename parameter arguments, choose names that match

/**
 * A simple [Fragment] subclass.
 * Use the [Get_Joke_Fragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class Get_Joke_Fragment : Fragment() {

    val TAG = "Get_Joke_Fragment"

    lateinit var loginButton: LoginButton
    lateinit var callbackManager: CallbackManager

    // Used to handle UI related tasks
    var uiHandler = Handler()

// Used to handle background related tasks
    var handler = AppExecutors.instance?.td!!


    val EMAIL = "email"
    val USER_GENDER = "user_gender"
    val USER_FRIENDS= "user_friends"

    lateinit var  dbTask: DbTask
    lateinit var viewModel: View_Model
    private var _binding: FragmentGetJokeBinding?=null

    private val binding get() = _binding!!
     var currentJoke: Joke ?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Handle callback for successful login
        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult?> {
            override fun onSuccess(loginResult: LoginResult?) {
                Log.i(TAG, "onSuccess: user logged in ")
            }

            override fun onCancel() {
                Log.i(TAG, "onCancel: logged in cancelled")
            }

            override fun onError(exception: FacebookException) {
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGetJokeBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(View_Model::class.java)


        check_login_status()
        setupObserver()

        binding.jokeImage.setImageResource(R.drawable.funny)

        loginButton = binding.loginButton
        loginButton.setReadPermissions(Arrays.asList(EMAIL, USER_FRIENDS, USER_GENDER))
        loginButton.setFragment(this)

        val view = binding.root

        binding.btnGetJoke.setOnClickListener {
            handler.submit {
                retrieveJoke()
            }
        }

        binding.btnSaveJoke.setOnClickListener{
            if (currentJoke != null) {

                dbTask = DbTask({viewModel.addJoke(currentJoke!!)}, currentJoke!!)
                var result = handler.submit (dbTask)

                var value = result.get() as Int
                if (value < 0) {
                    showSnackbar(it, "joke not saved, something went wrong")
                } else {
                    showSnackbar(it, "joke saved successfully")
                }
            } else{
                showSnackbar(it, "Click on \"GET JOKE\" button first ")
            }
        }
        return view
    }

    // Enable user to share the joke
    private fun enable_share_link(joke: String?) {
        val shareLinkContent = ShareLinkContent.Builder()
            .setQuote(joke).
                setContentUrl(Uri.parse("https://icanhazdadjoke.com")).build()

        binding.shareButton.shareContent = shareLinkContent
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)

        check_login_status()

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun check_login_status(){
        val accessToken = AccessToken.getCurrentAccessToken()
        viewModel.isUserLoggedin.value = accessToken != null && !accessToken.isExpired

        if(viewModel.isUserLoggedin.value!!){
         get_user_info()
        }
    }



    // set up observer to subscribe to update to isUserLoggedin and isJokeFetched
    fun setupObserver(){

        viewModel.isUserLoggedin.observe(viewLifecycleOwner, {
            if(it){
                binding.shareButton.visibility = View.VISIBLE
            }else{
                binding.shareButton.visibility = View.INVISIBLE
            }
        })

        // Only allow sharing of joke after user gets a joke from API
        viewModel.isJokeFetched.observe(viewLifecycleOwner, {
            if(it){
                enable_share_link(currentJoke?.joke)
            }
        })
    }


    // Get logged-in user information
    private fun get_user_info() {
        var graphRequest: GraphRequest = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken() ){
                obj, response->
            Log.i(TAG, "onActivityResult: ${obj.toString()}")

            try{
                val name = obj.getString("name")
                val id = obj.getString("id")
                binding.nameField.setText(name)
                val pic=
                    obj.getJSONObject("picture").getJSONObject("data").getString("url")

                Picasso.with(App.instance).load(pic)
                    .into(binding.profilePic)
            }catch(e: JSONException){
                e.printStackTrace()
            }
        }

        val bundle = Bundle()
        bundle.putString("fields", "gender, name, id, first_name, last_name, picture")

        graphRequest.parameters = bundle
        graphRequest.executeAsync()
    }


    val accessTokenTracker =object: AccessTokenTracker(){
        override fun onCurrentAccessTokenChanged(
            oldAccessToken: AccessToken?,
            currentAccessToken: AccessToken?
        ) {
            if(currentAccessToken== null){
                LoginManager.getInstance().logOut()

                // Clear user information
                viewModel.toggle_user_login(false)
                binding.nameField.setText("")
              binding.profilePic.setImageResource(0)
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()

        accessTokenTracker.stopTracking()
        handler.shutdown()

    }

    fun showSnackbar(view:View, msg :String){

        val snackbar = Snackbar.make(view,
            msg,
            Snackbar.LENGTH_LONG
        )
        snackbar.duration = 5000
        snackbar.setAnchorView(binding.btnSaveJoke)

        snackbar.animationMode = BaseTransientBottomBar.ANIMATION_MODE_SLIDE
        snackbar.setAction("OKAY") {
            snackbar.dismiss()
        }
        snackbar.show()
    }

    // fetch joke from https://icanhazdadjoke.com
    private fun retrieveJoke():Joke?{
        var joke: Joke? = null
        try {
            val client =
                ServiceBuilder.buildService(APIService::class.java)

            val call: Call<Joke> = client.get_random_jokes()
            call.enqueue(object : Callback<Joke> {
                override fun onFailure(call: Call<Joke>, t: Throwable) {
                    Log.i(ContentValues.TAG, "onFailure: ${t.printStackTrace()}")

                    uiHandler.post {
                        Toast.makeText(
                            App.instance, "something went wrong, " +
                                    "please try again", Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onResponse(
                    call: Call<Joke>,
                    response: Response<Joke>
                ) {

                    Log.i(ContentValues.TAG, "onResponse: ${response.body()}")
                    uiHandler.post{

                        currentJoke = response.body()
                       binding.getJokeText.text = currentJoke?.joke

                        viewModel.toggle_fetch_status(true)
                    }
                }
            })
        }catch(e:Exception){
            e.printStackTrace()
        }
        return joke
    }


}