// ./main/java/com/example/tictactoeapp/presentation/ui/GameHistoryAdapter.kt
package com.example.tictactoeapp.presentation.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tictactoeapp.R
import com.example.tictactoeapp.presentation.model.GameHistoryViewData

class GameHistoryAdapter : ListAdapter<GameHistoryViewData, GameHistoryAdapter.GameHistoryViewHolder>(
    GameHistoryDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameHistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_game_history, parent, false)
        return GameHistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: GameHistoryViewHolder, position: Int) {
        val historyItem = getItem(position)
        holder.bind(historyItem)
    }

    inner class GameHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewGameId: TextView = itemView.findViewById(R.id.textViewGameId)
        private val textViewPlayers: TextView = itemView.findViewById(R.id.textViewPlayers)
        private val textViewGameType: TextView = itemView.findViewById(R.id.textViewGameType)
        private val textViewResult: TextView = itemView.findViewById(R.id.textViewResult)
        private val textViewDate: TextView = itemView.findViewById(R.id.textViewDate)

        fun bind(historyItem: GameHistoryViewData) {
            textViewGameId.text = "ID: ${historyItem.gameIdShort}"
            textViewPlayers.text = "${historyItem.player1Username} vs ${historyItem.player2Username}"
            textViewGameType.text = historyItem.gameType
            textViewResult.text = historyItem.resultText
            textViewDate.text = historyItem.gameDate

            // Устанавливаем цвет результата
            val color = ContextCompat.getColor(itemView.context, historyItem.resultColorRes)
            textViewResult.setTextColor(color)
        }
    }
}

class GameHistoryDiffCallback : DiffUtil.ItemCallback<GameHistoryViewData>() {
    override fun areItemsTheSame(oldItem: GameHistoryViewData, newItem: GameHistoryViewData): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: GameHistoryViewData, newItem: GameHistoryViewData): Boolean {
        return oldItem == newItem
    }
}