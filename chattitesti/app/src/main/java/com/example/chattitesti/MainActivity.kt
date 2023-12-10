package com.example.chattitesti
import ChatAdapter
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chattitesti.FirestoreManager
import com.example.chattitesti.Message
import com.example.chattitesti.R
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: ChatAdapter
    private lateinit var messageList: RecyclerView
    private lateinit var sendButton: View
    private lateinit var messageInput: EditText

    private lateinit var messageListener: ListenerRegistration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        adapter = ChatAdapter()

        // Käytä findViewById-metodia löytääksesi näkymät
        messageList = findViewById(R.id.messageList)
        sendButton = findViewById(R.id.sendButton)
        messageInput = findViewById(R.id.messageInput)

        messageList.layoutManager = LinearLayoutManager(this)

        messageList.adapter = adapter

        sendButton.setOnClickListener {
            val messageText = messageInput.text.toString().trim()
            if (messageText.isNotEmpty()) {
                val message = Message(sender = "User", text = messageText)
                FirestoreManager.sendMessage(message, { messageInput.text.clear() }, { /* Handle failure */ })
            }
        }

        setupMessageListener()
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
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        messageListener.remove()
    }
}