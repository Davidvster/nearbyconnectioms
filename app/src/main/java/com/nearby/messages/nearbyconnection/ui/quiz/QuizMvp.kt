package com.nearby.messages.nearbyconnection.ui.quiz

import com.nearby.messages.nearbyconnection.arch.BaseMvp
import com.nearby.messages.nearbyconnection.data.model.QuizQuestion
import com.nearby.messages.nearbyconnection.data.model.QuizResult

interface QuizMvp : BaseMvp {
    interface View : BaseMvp.View {
        fun setConnectionRoom()
        fun setQuizRoom()
        fun setToolbarTitle(newTitle: String)
        fun setProgressVisible(visible: Boolean)
        fun updateConnectionList(availableRooms: MutableList<Pair<String, String>>)
        fun setParticipantsList(guestNames: List<String>)
        fun setQuestion(question: QuizQuestion)
        fun updateQuizResult(resultList: MutableList<QuizResult>)
    }

    interface Presenter : BaseMvp.Presenter {
        fun requestConnection(endpointId: String)
        fun isConnected(): Boolean
        fun getAvailableGuests(): HashMap<String, String>
        fun acceptConnection(user: String, endpointId: String)
        fun stopAllConnections()
        fun startDiscovery()
        fun stopDiscovery()
        fun init(username: String, packageName: String, colorCard: Int)
        fun sendAnswer(response: Int)
    }
}