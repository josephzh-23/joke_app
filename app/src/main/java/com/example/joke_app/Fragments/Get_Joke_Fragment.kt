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
import com.smartherd.globofly.services.Django_Service
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.android.material.snackbar.BaseTransientBottomBar

import com.google.android.material.snackbar.Snackbar

import com.facebook.login.LoginResult

import android.content.Intent
import android.net.Uri
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
    // TODO: Rename and change types of parameters
    val TAG = "Get_Joke_Fragment"
    // Used to handle UI related tasks


    var isLoggedIn: Boolean = false
    lateinit var loginButton: LoginButton
    lateinit var callbackManager: CallbackManager

    // Used to handle UI related tasks
    var uiHandler = Handler()
// Used to handle background related tasks
    var handler = AppExecutors.instance?.td!!

    private var param1: String? = null
    private var param2: String? = null
    lateinit var  dbTask: DbTask
    lateinit var viewModel: View_Model
    private var _binding: FragmentGetJokeBinding?=null
    private val binding get() = _binding!!
     var currentJoke: Joke ?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGetJokeBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(View_Model::class.java)




        // Check if user logged in

        val EMAIL = "email"
        val USER_GENDER = "user_gender"
        val USER_FRIENDS= "user_friends"

        loginButton = binding.loginButton

        callbackManager = CallbackManager.Factory.create();
        loginButton.setReadPermissions(Arrays.asList(EMAIL, USER_FRIENDS, USER_GENDER))

        loginButton.setFragment(this)

        check_login_status()


        subscribe_fetch_observer()
        loginButton.registerCallback(callbackManager, object : FacebookCallback<LoginResult?> {
            override fun onSuccess(loginResult: LoginResult?) {


            }

            override fun onCancel() {

                Log.i(TAG, "onCancel: logged in cancelled")
            }

            override fun onError(exception: FacebookException) {
                // App code
            }
        })


        callbackManager = CallbackManager.Factory.create()

        binding.jokeImage.setImageResource(R.drawable.funny)


//        binding.shareButton.setOnClickListener {
//            if (binding.getJokeText.text != "") {
//            } else {
//                showSnackbar(it, "Click on the get button to share the joke")
//            }
//        }
// Chekc if the user is logged in


        binding.shareButton.setOnClickListener {

            if(currentJoke!=null) {
                enable_share_link(currentJoke?.joke)
            }else{
                showSnackbar(it, "Make sure you get a joke first")
            }
        }


        val view = binding.root

        binding.btnGetJoke.setOnClickListener {
            handler.submit {

                retrieveJoke()
            }
        }


        binding.btnSaveJoke.setOnClickListener{

            Log.i(TAG, "onCreateView: $currentJoke")
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

    private fun enable_share_link(joke: String?) {


        Log.i(TAG, "enable_share_link: current joke is ${currentJoke?.joke.toString()}")
        val shareLinkContent = ShareLinkContent.Builder()
            .setQuote(joke).
                setContentUrl(Uri.parse("https://icanhazdadjoke.com")).build()

        binding.shareButton.shareContent = shareLinkContent

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)

        viewModel.login()
       get_user_info()

    }



    fun check_login_status(){

        val accessToken = AccessToken.getCurrentAccessToken()
        viewModel.isUserLoggedin.value = accessToken != null && !accessToken.isExpired


    }



    fun subscribe_fetch_observer(){



        viewModel.isUserLoggedin.observe(viewLifecycleOwner, {


            Log.i(TAG, "subscribe_fetch_observer: logged in observer")
            if(it){

                binding.shareButton.visibility = View.VISIBLE
            }else{
                binding.shareButton.visibility = View.INVISIBLE

            }
        })


        viewModel.isJokeFetched.observe(viewLifecycleOwner, {


            Log.i(TAG, "subscribe_fetch_observer: logged in observer")
            if(it){

                enable_share_link(currentJoke?.joke)
            }
        })


    }
    // Get logged-in user information
    private fun get_user_info() {
        var graphRequest: GraphRequest = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken()){
                obj, response->
            Log.i(TAG, "onActivityResult: ${obj.toString()}")



            try{
                val name = obj.getString("name")
                val id = obj.getString("id")
                binding.nameField.setText(name)

                val pic=
                    obj.getJSONObject("picture").getJSONObject("data").getString("url")
                Log.i(TAG, "onActivityResult: $id")
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
            }
        }

    }


    override fun onDestroy() {
        super.onDestroy()

        accessTokenTracker.stopTracking()
        handler.shutdown()
//        binding.nameField.setText("")
//        binding.profilePic.setImageResource(0)
    }

    fun showSnackbar(view:View, msg :String){

        val snackbar = Snackbar.make(view,
            msg,
            Snackbar.LENGTH_LONG
        )
        snackbar.duration = 10000
        snackbar.setAnchorView(binding.btnSaveJoke)
        snackbar.animationMode = BaseTransientBottomBar.ANIMATION_MODE_SLIDE
        snackbar.setAction("OKAY") {
            snackbar.dismiss()
        }
        snackbar.show()
    }

    // fetch joke from https://icanhazdadjoke.com
    fun retrieveJoke():Joke?{
        var joke: Joke? = null
        try {
            val client =
                ServiceBuilder.buildService(Django_Service::class.java)

            // See if this fixes anything
            val call: Call<Joke> = client.get_random_jokes()
            call.enqueue(object : Callback<Joke> {
                override fun onFailure(call: Call<Joke>, t: Throwable) {
                    Log.i(ContentValues.TAG, "onFailure: ${t.printStackTrace()}")


                }

                override fun onResponse(
                    call: Call<Joke>,
                    response: Response<Joke>
                ) {

                    Log.i(ContentValues.TAG, "onResponse: ${response.body()}")

                    uiHandler.post{


                        currentJoke = response.body()
                        Log.i(TAG, "onResponse: ${currentJoke?.joke}")
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