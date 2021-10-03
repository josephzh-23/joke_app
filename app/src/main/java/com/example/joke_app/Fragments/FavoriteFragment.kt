package com.example.joke_app.Fragments

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.joke_app.*
import com.example.joke_app.databinding.FragmentFavoriteBinding
import com.google.android.material.snackbar.Snackbar
import com.ledsmart.grow3.Syncing.Modes_RecyclerAdapter
import android.R

import com.example.joke_app.MainActivity

import androidx.core.content.ContextCompat
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import java.util.concurrent.Callable


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FavoriteFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FavoriteFragment : Fragment(), OnItemClickListener {
    private var _binding: FragmentFavoriteBinding?=null
    private val binding get() = _binding!!
    private lateinit var listAdapter: Modes_RecyclerAdapter

    var recyclerView: RecyclerView?=null
    lateinit var  dbTask: DbTask

    var handler = AppExecutors.instance?.td!!

    var jokeList = ArrayList<Joke>()
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var callable: Callable<Int>
    lateinit var viewModel: View_Model
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }



    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        Log.i(TAG, "onCreateView: favorite fragment ")
        setupRecyclerView()


        val itemTouchHelper = ItemTouchHelper(swipeCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
        viewModel = ViewModelProvider(requireActivity()).get(View_Model::class.java)


        // Load the jokes from database
        (activity as MainActivity).showProgressBar(true)

        handler.submit {
            viewModel.load_jokes_from_db()
        }

        subscribeObservers()
        (activity as MainActivity).showProgressBar(false)
        val view = binding.root
        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.shutdown()
    }

    val swipeCallback: ItemTouchHelper.SimpleCallback = object :
        ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            // remove item from adapter

            val position = viewHolder.adapterPosition
            when(direction){
                ItemTouchHelper.LEFT-> {


                    val deletedJoke = jokeList.get(position)


                    dbTask = DbTask({viewModel.deleteJoke(deletedJoke)},  deletedJoke)
                    var result = handler.submit (dbTask)
                    var value = result.get() as Int

//                        var task = DbTask()
//                        task.call()
//
//
//                                    viewModel.addJoke(deletedJoke)
//
//
//                            }
//
//                        handler.submit(task.call())
////                        var result = handler.submit {
//
////                        }
//
//                        var res = -1
//                        //Delete joke from database
//                        res = handler.submit() {
//                            viewModel.delete_joke_from_db(deletedJoke)
//                        }


                    Log.i(TAG, "onSwiped: the value deleted $value")

                    jokeList.removeAt(position)
                    listAdapter.notifyItemChanged(position)

                    recyclerView?.let {
                        Snackbar.make(it,"Delete this ", Snackbar.LENGTH_SHORT)
                            .setAction("Undo this action", View.OnClickListener {
                                jokeList.add(deletedJoke)
                                listAdapter.notifyItemInserted(position)
                            }).show()
                    }
                }
                ItemTouchHelper.RIGHT-> {

                }

            }
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return false
        }

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {


            RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                .addBackgroundColor(
                    ContextCompat.getColor(
                        App.instance,
                        R.color.holo_blue_bright
                    )
                )
                .addActionIcon(R.drawable.ic_delete)
                .create()
                .decorate()
            super.onChildDraw(
                c,
                recyclerView,
                viewHolder,
                dX,
                dY,
                actionState,
                isCurrentlyActive
            )



        }
    }


    private fun setupRecyclerView() {
        listAdapter = Modes_RecyclerAdapter(jokeList,this)

        recyclerView = binding.jokeRecyclerView

        recyclerView?.layoutManager = LinearLayoutManager(activity)
        recyclerView?.addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
        recyclerView?.setHasFixedSize(true)

        recyclerView?.adapter = listAdapter

    }




    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FavoriteFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FavoriteFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun subscribeObservers() {



        // Based on update from the complete list
        viewModel.jokeList.observe(viewLifecycleOwner, object : Observer<ArrayList<Joke>> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onChanged(p0: ArrayList<Joke>?) {
                Log.i(ContentValues.TAG, "onChanged: recipe coming in ")
                jokeList.clear()
                for (i in 0 until viewModel.jokeList.value?.size!!) {
                    jokeList!!.add(viewModel.jokeList.value!![i])
                }


                listAdapter.notifyDataSetChanged()


            }



        })
    }

    override fun on_item_swiped(position: Int) {
//        viewModel.removeJoke(jokeList[position])
    }


}