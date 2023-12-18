package com.example.chattitesti

import ChatAdapter
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Switch
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity() {

    private lateinit var adapter: ChatAdapter
    private lateinit var messageList: RecyclerView
    private lateinit var sendButton: View
    private lateinit var messageInput: EditText

    private lateinit var messageListener: ListenerRegistration

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var senderId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getPreferences(Context.MODE_PRIVATE)
        senderId = sharedPreferences.getString("senderId", "User") ?: "User"

        adapter = ChatAdapter()

        // Käytä findViewById-metodia löytääksesi näkymät
        messageList = findViewById(R.id.messageList)
        sendButton = findViewById(R.id.sendButton)
        messageInput = findViewById(R.id.messageInput)

        val toolbar: Toolbar = findViewById(R.id.toolbar)

        val settingsIcon: ImageView = findViewById(R.id.settingsIcon)
        settingsIcon.setOnClickListener {
            startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
        }

        messageList.layoutManager = LinearLayoutManager(this)

        messageList.adapter = adapter

        sendButton.setOnClickListener {
            val messageText = messageInput.text.toString().trim()
            if (messageText.isNotEmpty()) {
                val message = Message(sender = senderId, text = messageText)
                FirestoreManager.sendMessage(message, { messageInput.text.clear() }, { /* Handle failure */ })
            }
        }

        setupMessageListener()
        promptUserForSenderId()
    }

    private fun setupMessageListener() {
        messageListener = Firebase.firestore.collection("chat_messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    // Handle error
                    return@addSnapshotListener
                }

                snapshot?.let {
                    val messages = it.toObjects(Message::class.java)
                    adapter.setMessages(messages)
                    //Toiminto jolla automaattisesti scrollataan uusimpaan viestiin
                    messageList.post {
                        adapter.scrollToBottom(messageList)
                    }
                }
            }

    }

    private fun promptUserForSenderId() {
        //tarkista onko sender id asetettu, sitten pyydä käyttäjältä id
        if (senderId == "User") {
            showSenderIdInputDialog()
        }
    }
    //ensimmäistä kertaa sovellustsa käynnistettäessä näytetään nimen asetusboxi
    private fun showSenderIdInputDialog() {
        val inputEditText = EditText(this)

        AlertDialog.Builder(this)
            .setTitle("Set Username")
            .setView(inputEditText)
            .setPositiveButton("OK") { _, _ ->
                val newSenderId = inputEditText.text.toString().trim()
                saveSenderId(newSenderId)
            }
            .setCancelable(false)
            .show()
    }
    //tallennetaan käyttäjän asettama nimi ettei sitä kysytä joka kerta
    private fun saveSenderId(newSenderId: String) {
        senderId = newSenderId
        sharedPreferences.edit().putString("senderId", senderId).apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        messageListener.remove()
    }
}

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settingsactivity)

        // Find the switch in the layout
        val notificationSwitch: Switch = findViewById(R.id.notificationSwitch)
        val themeSwitch: Switch = findViewById(R.id.themeSwitch)

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            recreate()
        }

        // Implement the switch's OnCheckedChangeListener to handle notifications
        // notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Handle enabling or disabling notifications based on isChecked value
            // Save the notification state in SharedPreferences or relevant storage
        //}
    }
}