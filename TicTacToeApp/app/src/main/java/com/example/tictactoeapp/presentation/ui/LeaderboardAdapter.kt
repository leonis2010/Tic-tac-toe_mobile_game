// ./main/java/com/example/tictactoeapp/presentation/ui/LeaderboardAdapter.kt
package com.example.tictactoeapp.presentation.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tictactoeapp.R
import com.example.tictactoeapp.presentation.model.LeaderboardViewData

class LeaderboardAdapter : ListAdapter<LeaderboardViewData, LeaderboardAdapter.LeaderboardViewHolder>(
    LeaderboardDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_leaderboard, parent, false)
        return LeaderboardViewHolder(view)
    }

    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        val leaderboardItem = getItem(position)
        holder.bind(leaderboardItem)
    }

    inner class LeaderboardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewPosition: TextView = itemView.findViewById(R.id.textViewPosition)
        private val textViewUsername: TextView = itemView.findViewById(R.id.textViewUsername)
        private val textViewGamesStats: TextView = itemView.findViewById(R.id.textViewGamesStats)
        private val textViewWinRate: TextView = itemView.findViewById(R.id.textViewWinRate)
        private val textViewRating: TextView = itemView.findViewById(R.id.textViewRating)

        fun bind(leaderboardItem: LeaderboardViewData) {
            // Позиция с медалями для топ-3
            when (leaderboardItem.position) {
                1 -> {
                    textViewPosition.text = "🥇"
                    textViewPosition.setTextColor(ContextCompat.getColor(itemView.context, R.color.gold))
                }
                2 -> {
                    textViewPosition.text = "🥈"
                    textViewPosition.setTextColor(ContextCompat.getColor(itemView.context, R.color.silver))
                }
                3 -> {
                    textViewPosition.text = "🥉"
                    textViewPosition.setTextColor(ContextCompat.getColor(itemView.context, R.color.bronze))
                }
                else -> {
                    textViewPosition.text = leaderboardItem.position.toString()
                    // Используем position_background для остальных позиций
                    textViewPosition.setBackgroundResource(R.drawable.position_background)
                    textViewPosition.setTextColor(Color.WHITE)
                }
            }

            textViewUsername.text = leaderboardItem.username
            textViewGamesStats.text = leaderboardItem.gamesStats
            textViewWinRate.text = leaderboardItem.winRate
            textViewRating.text = leaderboardItem.rating.toString()

            // Подсветка текущего пользователя (можно добавить позже)
            // if (leaderboardItem.username == currentUsername) {
            //     itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.highlight_background))
            // }
        }
    }
}

class LeaderboardDiffCallback : DiffUtil.ItemCallback<LeaderboardViewData>() {
    override fun areItemsTheSame(oldItem: LeaderboardViewData, newItem: LeaderboardViewData): Boolean {
        return oldItem.username == newItem.username
    }

    override fun areContentsTheSame(oldItem: LeaderboardViewData, newItem: LeaderboardViewData): Boolean {
        return oldItem == newItem
    }
}