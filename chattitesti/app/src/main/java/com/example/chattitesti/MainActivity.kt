package com.example.chattitesti
import ChatAdapter
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
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
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var messageListener: ListenerRegistration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        applySelectedTheme()
        setContentView(R.layout.activity_main)

        adapter = ChatAdapter()

        // Käytä findViewById-metodia löytääksesi näkymät
        messageList = findViewById(R.id.messageList)
        sendButton = findViewById(R.id.sendButton)
        messageInput = findViewById(R.id.messageInput)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        //Log.d("MainActivity", "Settings Icon Resource ID: $settingsIconResId")
        //setSupportActionBar(toolbar)

        //toolbar.setNavigationIcon(R.drawable.baseline_settings_black_24)

        val settingsIcon: ImageView = findViewById(R.id.settingsIcon)
        settingsIcon.setOnClickListener {
            startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
        }

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
    private fun applySelectedTheme() {
        val isNightModeEnabled = sharedPreferences.getBoolean("night_mode_enabled", false)
        if (isNightModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
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

class SettingsActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var notificationSwitch: Switch
    private lateinit var themeSwitch: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val isNightModeEnabled = sharedPreferences.getBoolean("night_mode_enabled", false)

        applySelectedTheme()

        setContentView(R.layout.settingsactivity)

        // Find the switch in the layout
        notificationSwitch = findViewById(R.id.notificationSwitch)
        themeSwitch = findViewById(R.id.themeSwitch)

        themeSwitch.isChecked = isNightModeEnabled
        updateSwitchTextColors(isNightModeEnabled)

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("night_mode_enabled", isChecked).apply()
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            recreate()
        }

            // Implement the switch's OnCheckedChangeListener to handle notifications
            notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
                // Handle enabling or disabling notifications based on isChecked value
                // Save the notification state in SharedPreferences or relevant storage
            }
        }
    private fun applySelectedTheme() {
        val isNightModeEnabled = sharedPreferences.getBoolean("night_mode_enabled", false)
        if (isNightModeEnabled) {
            setTheme(R.style.SettingsActivityTheme_Dark)
        } else {
            setTheme(R.style.SettingsActivityTheme_Light)
        }
    }
    private fun updateSwitchTextColors(isNightModeEnabled: Boolean) {
        val textColorResId = if (isNightModeEnabled) R.color.white else R.color.black
        val textColor = ContextCompat.getColor(this, textColorResId)
        notificationSwitch.setTextColor(textColor)
        themeSwitch.setTextColor(textColor)
    }
}
