package com.nearby.messages.nearbyconnection.ui.hostquiz

import com.nearby.messages.nearbyconnection.arch.BaseMvp
import com.nearby.messages.nearbyconnection.data.model.QuizGuestRequest
import com.nearby.messages.nearbyconnection.data.model.QuizQuestion
import com.nearby.messages.nearbyconnection.data.model.QuizResult

interface HostQuizMvp : BaseMvp {
    interface View : BaseMvp.View {
        fun showJoinDialog(user: QuizGuestRequest, endpointId: String)
        fun updateQuizResult(resultList: MutableList<QuizResult>)
        fun enableQuizForm(enabled: Boolean)
    }

    interface Presenter : BaseMvp.Presenter {
        fun rejectConnection(endpointId: String)
        fun acceptConnection(user: QuizGuestRequest, endpointId: String)
        fun stopAdvertising()
        fun startAdvertising()
        fun stopAllConnections()
        fun init(username: String, packageName: String, cardColor: Int)
        fun sendQuestion(question: QuizQuestion, correctAnswer: Int)
        fun getGuestList(): List<String>
    }
}