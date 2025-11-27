// ./src/main/java/com/example/tictactoeapp/presentation/ui/GameItemAdapter.kt
package com.example.tictactoeapp.presentation.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tictactoeapp.R
import com.example.tictactoeapp.presentation.model.GameItemViewData

class GameItemAdapter(
    private val onJoinClick: (GameItemViewData) -> Unit
) : ListAdapter<GameItemViewData, GameItemAdapter.GameItemViewHolder>(GameItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_game, parent, false)
        return GameItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: GameItemViewHolder, position: Int) {
        val gameItem = getItem(position)
        holder.bind(gameItem)
    }

    inner class GameItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewGameId: TextView = itemView.findViewById(R.id.textViewGameId)
        private val textViewCreator: TextView = itemView.findViewById(R.id.textViewCreator)
        private val buttonJoin: Button = itemView.findViewById(R.id.buttonJoin)

        fun bind(gameItem: GameItemViewData) {
            textViewGameId.text = "Game ID: ${gameItem.id}"
            textViewCreator.text = "Created by: ${gameItem.creatorUsername}"
            buttonJoin.setOnClickListener {
                println("DEBUG: Join button clicked for game: ${gameItem.id}")
                onJoinClick(gameItem)
            }
        }
    }
}

class GameItemDiffCallback : DiffUtil.ItemCallback<GameItemViewData>() {
    override fun areItemsTheSame(oldItem: GameItemViewData, newItem: GameItemViewData): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: GameItemViewData, newItem: GameItemViewData): Boolean {
        return oldItem == newItem
    }
}
