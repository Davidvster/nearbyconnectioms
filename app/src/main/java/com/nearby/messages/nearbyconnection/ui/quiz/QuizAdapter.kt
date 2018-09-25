package com.nearby.messages.nearbyconnection.ui.quiz

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.nearby.messages.nearbyconnection.R
import com.nearby.messages.nearbyconnection.data.model.QuizResult

class QuizAdapter constructor(val context: Context) : RecyclerView.Adapter<QuizAdapter.ViewHolder>() {

    var resultList = mutableListOf<QuizResult>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizAdapter.ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.element_quiz_result, parent, false))
    }

    override fun onBindViewHolder(holder: QuizAdapter.ViewHolder, position: Int) {
        resultList[holder.adapterPosition].let { result ->
            holder.itemView.findViewById<TextView>(R.id.result_element_name).text = "This round winner is: "+result.winnerName

        }
    }

    override fun getItemCount(): Int = resultList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}