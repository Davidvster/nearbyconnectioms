package com.nearby.messages.nearbyconnection.ui.hostquiz

import android.content.Context
import android.support.annotation.UiThread
import android.util.Log
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import com.google.gson.Gson
import com.nearby.messages.nearbyconnection.arch.AppModule
import com.nearby.messages.nearbyconnection.arch.BasePresenter
import com.nearby.messages.nearbyconnection.data.model.Guest
import com.nearby.messages.nearbyconnection.data.model.Participant
import com.nearby.messages.nearbyconnection.data.model.QuizQuestion
import com.nearby.messages.nearbyconnection.data.model.QuizResponse
import com.nearby.messages.nearbyconnection.data.model.QuizResult
import java.util.Calendar
import java.util.Date
import java.util.Timer
import kotlin.concurrent.timerTask

class HostQuizPresenter constructor(hostQuizView: HostQuizMvp.View, private val context: Context = AppModule.application) : BasePresenter<HostQuizMvp.View>(hostQuizView), HostQuizMvp.Presenter {

    private lateinit var connectionsClient: ConnectionsClient

//    private var guestsEndpointId = mutableListOf<String>()
//    private var guestNames = HashMap<String, String>()
    private var guests = mutableListOf<Guest>()
    private var currentQuizResponses = mutableListOf<QuizResponse>()
    private var resultList = mutableListOf<QuizResult>()

    private lateinit var packageName: String
    private var username = ""
    private var cardColor = -1
    private var correctAnswer = 0
    private var quizEnded = true

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            Log.v("SOGOVOREC", endpointId+" sent a message: "+ String(payload.asBytes()!!))
            val quizResponse = Gson().fromJson(String(payload.asBytes()!!), QuizResponse::class.java)
            if (quizResponse.timeTaken != null && quizResponse.response != null) {
                quizResponse.endpointId = endpointId
                currentQuizResponses.add(quizResponse)
                if (currentQuizResponses.size == guests.size) {
                    endOfQuiz()
                }
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
        }
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {

        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            view?.showConnectionDialog(connectionInfo.endpointName, endpointId)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            when (result.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    view?.setParticipantsTitle(guests.map { it.username })
                    Log.v("SOGOVOREC1", "We're connected! Can now start sending and receiving data. " + endpointId)
                    sendParticipants()
                }
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    guests.remove(guests.find { it.endpointId == endpointId })
                    Log.v("SOGOVOREC2", "We're connected! Can now start sending and receiving data. " + endpointId)
                }
                ConnectionsStatusCodes.STATUS_ERROR -> {
                    guests.remove(guests.find { it.endpointId == endpointId })
                    Log.v("SOGOVOREC3", "The connection broke before it was able to be accepted. " + endpointId)
                }
                else -> {
                    guests.remove(guests.find { it.endpointId == endpointId })
                }
            }
        }

        override fun onDisconnected(endpointId: String) {
            Log.v("SOGOVOREC", "We've been disconnected from this endpoint. " + endpointId)
//            guestsEndpointId.remove(endpointId)
            guests.remove(guests.find { it.endpointId == endpointId })
            view?.setParticipantsTitle(guests.map { it.username })
            sendParticipants()
        }
    }

    override fun init(username: String, packageName: String, cardColor: Int) {
        this.username = username
        this.packageName = packageName + ".quiz"
        this.cardColor = cardColor
        connectionsClient = Nearby.getConnectionsClient(context)
    }

    override fun stopAllConnections() {
        connectionsClient.stopAllEndpoints()
    }

    override fun startAdvertising() {
        Log.v("POVEZAVA", "started advertising")
        connectionsClient.startAdvertising(
                username, packageName, connectionLifecycleCallback, AdvertisingOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build())
    }

    override fun stopAdvertising() {
        Log.v("POVEZAVA", "stopped advertising")
        connectionsClient.stopAdvertising()
    }

    private fun sendParticipants() {
        for (guest in guests) {
            val tmpGuestNames = guests.toMutableList()
            tmpGuestNames.remove(guest)
//            tmpGuestNames.add(Guest("quizhost", username, 0))
            val message = Gson().toJson(Participant(tmpGuestNames.map { it.username}))
            connectionsClient.sendPayload(guest.endpointId, Payload.fromBytes(message.toByteArray()))
        }
    }

    override fun acceptConnection(user: String, endpointId: String) {
        guests.add(Guest(endpointId, user))
        connectionsClient.acceptConnection(endpointId, payloadCallback)
    }

    override fun rejectConnection(endpointId: String) {
        connectionsClient.rejectConnection(endpointId)
    }

    private fun sendMessage(endpointIds: List<String>, message: String, endpointId: String? = null) {
        for (guest in endpointIds) {
            if (guest != endpointId) {
                connectionsClient.sendPayload(guest, Payload.fromBytes(message.toByteArray()))
            }
        }
    }

    override fun sendQuestion(question: QuizQuestion, correctAnswer: Int) {
        this.correctAnswer = correctAnswer
        quizEnded = false
        for (guest in guests) {
            val message = Gson().toJson(question)
            connectionsClient.sendPayload(guest.endpointId, Payload.fromBytes(message.toByteArray()))
        }
        Timer().schedule(timerTask {
            endOfQuiz()
        }, 60000)
    }

    private fun endOfQuiz() {
        if (quizEnded.not()) {
            quizEnded = true
            var winnerResponse: QuizResponse? = null
            var minTime = Long.MAX_VALUE
            for (response in currentQuizResponses) {
                if (response.response == correctAnswer) {
                    guests.find { it.endpointId == response.endpointId }!!.points += 60000 - response.timeTaken
                    if (response.timeTaken < minTime) {
                        winnerResponse = response
                        minTime = response.timeTaken
                    }
                }
            }
            if (winnerResponse != null) {
                val cal = Calendar.getInstance()
                cal.time = Date(winnerResponse.timeTaken)
                val seconds =  cal.get(Calendar.SECOND)
                currentQuizResponses = mutableListOf()
                val winner = guests.find { it.endpointId == winnerResponse.endpointId }
                var quizResult = QuizResult(winner!!.username + " responded in $seconds second/s!", guests.sortedByDescending { it.points })
                sendMessage(guests.map { it.endpointId }, Gson().toJson(quizResult), winnerResponse.endpointId)
                resultList.add(quizResult)
                view?.updateQuizResult(resultList)

                quizResult = QuizResult("Congratulations you won, you responded in $seconds second/s!", guests.sortedByDescending { it.points })
                sendMessage(listOf(winnerResponse.endpointId), Gson().toJson(quizResult))
            } else {
                var quizResult = QuizResult("There are no winners for this round!", guests.sortedByDescending { it.points })
                sendMessage(guests.map { it.endpointId }, Gson().toJson(quizResult))
                resultList.add(quizResult)
                view?.updateQuizResult(resultList)
            }
        }
    }

}