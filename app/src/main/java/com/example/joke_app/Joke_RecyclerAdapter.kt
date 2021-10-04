package com.ledsmart.grow3.Syncing

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.joke_app.Joke
import com.example.joke_app.R
import com.example.joke_app.databinding.LayoutJokeListItemBinding


class Joke_RecyclerAdapter(val list: ArrayList<Joke>) : RecyclerView.Adapter<Joke_RecyclerAdapter.ViewHolder>()  {

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Joke_RecyclerAdapter.ViewHolder {

		return ViewHolder(
			LayoutJokeListItemBinding.inflate(
				LayoutInflater.from(parent.context),
				parent,
				false
			)
		)
	}

	override fun onBindViewHolder(holder: Joke_RecyclerAdapter.ViewHolder, pos: Int) {

		val binding = holder.binding
		binding.jokeText.text = list[pos].joke
		binding.statusText.text =  list[pos].status
		binding.defaultImg.setImageResource(R.drawable.funny)
	}

	override fun getItemCount(): Int {
		return list.size
	}


	inner class ViewHolder(val binding: LayoutJokeListItemBinding): RecyclerView.ViewHolder(binding.root)
	{

	}


}








