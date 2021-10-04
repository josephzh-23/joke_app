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
import com.ledsmart.grow3.Syncing.Joke_RecyclerAdapter
import android.R

import com.example.joke_app.MainActivity

import androidx.core.content.ContextCompat
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import java.util.concurrent.Callable





class FavoriteFragment : Fragment(){

    private var _binding: FragmentFavoriteBinding?=null
    private val binding get() = _binding!!
    private lateinit var listAdapter: Joke_RecyclerAdapter

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
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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

        setupObserver()
        (activity as MainActivity).showProgressBar(false)
        val view = binding.root
        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.shutdown()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    val swipeCallback: ItemTouchHelper.SimpleCallback = object :
        ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            // remove item from adapter

            val position = viewHolder.adapterPosition
            when(direction){
                ItemTouchHelper.LEFT-> {
                    val deletedJoke = jokeList.get(position)
                    dbTask = DbTask({ viewModel.deleteJoke(deletedJoke) }, deletedJoke)

                    val result = handler.submit(dbTask)
                    val value = result.get() as Int

                    // If database deletion successful
                    if (value > 0) {

                        jokeList.removeAt(position)
                        listAdapter.notifyItemChanged(position)

                        recyclerView?.let {
                            Snackbar.make(it, "Deleted ", Snackbar.LENGTH_LONG)
                                .setAction("Undo this action", View.OnClickListener {
                                    jokeList.add(deletedJoke)
                                    listAdapter.notifyItemInserted(position)
                                }).show()
                        }
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
        listAdapter = Joke_RecyclerAdapter(jokeList)
        recyclerView = binding.jokeRecyclerView

        recyclerView?.layoutManager = LinearLayoutManager(activity)
        recyclerView?.addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
        recyclerView?.setHasFixedSize(true)
        recyclerView?.adapter = listAdapter
    }

    // Set up observer to subscribe to changes to jokeList
    private fun setupObserver() {
        // Based on update from the complete list
        viewModel.jokeList.observe(viewLifecycleOwner, object : Observer<ArrayList<Joke>> {
            @SuppressLint("NotifyDataSetChanged")

            override fun onChanged(p0: ArrayList<Joke>?) {
                jokeList.clear()
                for (i in 0 until viewModel.jokeList.value?.size!!) {
                    jokeList!!.add(viewModel.jokeList.value!![i])
                }
                listAdapter.notifyDataSetChanged()
            }
        })
    }
}